package com.suman.smartbudgeter.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import com.suman.smartbudgeter.data.local.BudgetDatabase
import com.suman.smartbudgeter.data.local.entity.CategoryEntity
import com.suman.smartbudgeter.data.local.entity.RecurringExpenseEntity
import com.suman.smartbudgeter.data.local.entity.TransactionEntity
import com.suman.smartbudgeter.data.local.entity.VaultGoalEntity
import com.suman.smartbudgeter.data.local.model.TransactionRecord
import com.suman.smartbudgeter.domain.DateWindow
import com.suman.smartbudgeter.domain.MonthlyBudgetReport
import com.suman.smartbudgeter.domain.util.BudgetCalculator
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import kotlin.math.min

class BudgetRepository(
    database: BudgetDatabase,
) {

    private val categoryDao = database.categoryDao()
    private val transactionDao = database.transactionDao()
    private val recurringExpenseDao = database.recurringExpenseDao()
    private val vaultGoalDao = database.vaultGoalDao()

    fun observeCategories(): Flow<List<CategoryEntity>> = categoryDao.observeCategories()

    fun observeTransactions(window: DateWindow): Flow<List<TransactionEntity>> {
        return transactionDao.observeTransactionsBetween(window.startInclusive, window.endInclusive)
    }

    fun observeRecurringExpenses(): Flow<List<RecurringExpenseEntity>> = recurringExpenseDao.observeRecurringExpenses()

    fun observeVaultGoals(): Flow<List<VaultGoalEntity>> = vaultGoalDao.observeVaultGoals()

    fun historyPager(
        window: DateWindow,
        categoryId: Long?,
    ): Flow<PagingData<TransactionRecord>> {
        return Pager(
            config = PagingConfig(pageSize = 20, enablePlaceholders = false),
            pagingSourceFactory = {
                transactionDao.historyPagingSource(
                    startInclusive = window.startInclusive,
                    endInclusive = window.endInclusive,
                    categoryId = categoryId,
                )
            },
        ).flow
    }

    suspend fun addTransaction(transaction: TransactionEntity) {
        transactionDao.insertTransaction(transaction)
    }

    suspend fun upsertCategory(category: CategoryEntity) {
        categoryDao.upsert(category)
    }

    suspend fun deleteCategory(category: CategoryEntity) {
        categoryDao.delete(category)
    }

    suspend fun getTotalBudgetLimit(): Double = categoryDao.getTotalBudgetLimit()

    suspend fun getCategoryById(id: Long): CategoryEntity? = categoryDao.getCategoryById(id)

    suspend fun buildMonthlyReport(today: LocalDate = LocalDate.now()): MonthlyBudgetReport {
        val window = BudgetCalculator.currentMonthWindow(today)
        val categories = categoryDao.getCategories()
        val transactions = transactionDao.getTransactionsBetween(window.startInclusive, window.endInclusive)
        val snapshot = BudgetCalculator.buildSnapshot(categories, transactions, today)
        val page = transactionDao.historyPagingSource(window.startInclusive, window.endInclusive, null).load(
            PagingSource.LoadParams.Refresh(
                key = null,
                loadSize = 60,
                placeholdersEnabled = false,
            ),
        )

        val records = when (page) {
            is PagingSource.LoadResult.Page -> page.data
            else -> emptyList()
        }

        return MonthlyBudgetReport(
            monthLabel = BudgetCalculator.monthLabel(today),
            generatedAt = LocalDateTime.now(),
            snapshot = snapshot,
            transactions = records,
        )
    }

    suspend fun applyDueRecurringExpenses(today: LocalDate = LocalDate.now()): Int {
        val monthKey = YearMonth.from(today).toString()
        val dueExpenses = recurringExpenseDao.getActiveRecurringExpenses().filter { expense ->
            val effectiveDay = min(expense.dayOfMonth, today.lengthOfMonth())
            effectiveDay == today.dayOfMonth && expense.lastAppliedMonth != monthKey
        }

        dueExpenses.forEach { expense ->
            transactionDao.insertTransaction(
                TransactionEntity(
                    amount = expense.amount,
                    categoryId = expense.categoryId,
                    timestamp = System.currentTimeMillis(),
                    note = expense.note.ifBlank { expense.name },
                ),
            )
            recurringExpenseDao.updateLastAppliedMonth(expense.id, monthKey)
        }

        return dueExpenses.size
    }

}
