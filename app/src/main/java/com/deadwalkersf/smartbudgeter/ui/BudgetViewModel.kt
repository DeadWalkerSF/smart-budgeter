package com.deadwalkersf.smartbudgeter.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.deadwalkersf.smartbudgeter.BudgeterApp
import com.deadwalkersf.smartbudgeter.data.local.entity.CategoryEntity
import com.deadwalkersf.smartbudgeter.data.local.entity.RecurringExpenseEntity
import com.deadwalkersf.smartbudgeter.data.local.entity.TransactionEntity
import com.deadwalkersf.smartbudgeter.data.local.entity.VaultGoalEntity
import com.deadwalkersf.smartbudgeter.data.local.model.TransactionRecord
import com.deadwalkersf.smartbudgeter.domain.BudgetSnapshot
import com.deadwalkersf.smartbudgeter.domain.CurrencyOption
import com.deadwalkersf.smartbudgeter.domain.DateFilterPreset
import com.deadwalkersf.smartbudgeter.domain.ExportFormat
import com.deadwalkersf.smartbudgeter.domain.HistoryFilter
import com.deadwalkersf.smartbudgeter.domain.util.BudgetCalculator
import kotlinx.coroutines.delay
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDateTime

data class HomeUiState(
    val monthLabel: String = BudgetCalculator.monthLabel(),
    val snapshot: BudgetSnapshot = BudgetSnapshot(),
    val categories: List<CategoryEntity> = emptyList(),
    val recurringExpenses: List<RecurringExpenseEntity> = emptyList(),
    val quickSuggestions: List<CategoryEntity> = emptyList(),
    val vaultGoals: List<VaultGoalEntity> = emptyList(),
    val selectedCurrencyCode: String = "USD",
    val currencyOptions: List<CurrencyOption> = emptyList(),
)

data class HistoryUiState(
    val categories: List<CategoryEntity> = emptyList(),
    val selectedPreset: DateFilterPreset = DateFilterPreset.THIS_MONTH,
    val selectedCategoryId: Long? = null,
)

private data class HomeContentState(
    val monthLabel: String,
    val snapshot: BudgetSnapshot,
    val categories: List<CategoryEntity>,
    val recurringExpenses: List<RecurringExpenseEntity>,
    val quickSuggestions: List<CategoryEntity>,
    val vaultGoals: List<VaultGoalEntity>,
)

sealed interface UiEvent {
    data class Message(val value: String) : UiEvent
    data class Share(val uri: Uri, val mimeType: String, val title: String) : UiEvent
}

@OptIn(ExperimentalCoroutinesApi::class)
class BudgetViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as BudgeterApp
    private val repository = app.repository
    private val currencyPreferenceStore = app.currencyPreferenceStore
    private val exportManager = app.exportManager
    private val notificationManager = app.notificationManager

    private val selectedHistoryPreset = MutableStateFlow(DateFilterPreset.THIS_MONTH)
    private val selectedHistoryCategoryId = MutableStateFlow<Long?>(null)
    private val currentDateTime = MutableStateFlow(LocalDateTime.now())
    private val events = MutableSharedFlow<UiEvent>()

    private val categoriesFlow = repository.observeCategories()
    private val recurringExpensesFlow = repository.observeRecurringExpenses()
    private val vaultGoalsFlow = repository.observeVaultGoals()
    private val selectedCurrencyCodeFlow = currencyPreferenceStore.selectedCurrencyCode

    private val currentMonthWindowFlow = currentDateTime
        .map { BudgetCalculator.currentMonthWindow(it.toLocalDate()) }
        .distinctUntilChanged()

    private val currentMonthTransactionsFlow = currentMonthWindowFlow
        .flatMapLatest { repository.observeTransactions(it) }

    private val homeContentStateFlow = combine(
        categoriesFlow,
        currentMonthTransactionsFlow,
        recurringExpensesFlow,
        vaultGoalsFlow,
        currentDateTime,
    ) { categories, transactions, recurringExpenses, vaultGoals, now ->
        HomeContentState(
            monthLabel = BudgetCalculator.monthLabel(now.toLocalDate()),
            snapshot = BudgetCalculator.buildSnapshot(categories, transactions, now.toLocalDate()),
            categories = categories,
            recurringExpenses = recurringExpenses,
            quickSuggestions = BudgetCalculator.quickAddSuggestions(categories, now.toLocalTime()),
            vaultGoals = vaultGoals,
        )
    }

    val homeUiState = combine(
        homeContentStateFlow,
        selectedCurrencyCodeFlow,
    ) { homeContent, currencyCode ->
        HomeUiState(
            monthLabel = homeContent.monthLabel,
            snapshot = homeContent.snapshot,
            categories = homeContent.categories,
            recurringExpenses = homeContent.recurringExpenses,
            quickSuggestions = homeContent.quickSuggestions,
            vaultGoals = homeContent.vaultGoals,
            selectedCurrencyCode = currencyCode,
            currencyOptions = currencyPreferenceStore.supportedCurrencies,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(
            selectedCurrencyCode = currencyPreferenceStore.selectedCurrencyCode.value,
            currencyOptions = currencyPreferenceStore.supportedCurrencies,
        ),
    )

    val historyUiState = combine(
        categoriesFlow,
        selectedHistoryPreset,
        selectedHistoryCategoryId,
    ) { categories, preset, categoryId ->
        HistoryUiState(
            categories = categories,
            selectedPreset = preset,
            selectedCategoryId = categoryId,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HistoryUiState(),
    )

    val pagedTransactions: Flow<PagingData<TransactionRecord>> = combine(
        selectedHistoryPreset,
        selectedHistoryCategoryId,
        currentDateTime.map { it.toLocalDate() }.distinctUntilChanged(),
    ) { preset, categoryId, today ->
        HistoryFilter(
            preset = preset,
            categoryId = categoryId,
            window = BudgetCalculator.windowForPreset(preset, today),
        )
    }.flatMapLatest { filter ->
        repository.historyPager(filter.window, filter.categoryId)
    }.cachedIn(viewModelScope)

    val uiEvents = events.asSharedFlow()

    init {
        viewModelScope.launch {
            while (true) {
                currentDateTime.value = LocalDateTime.now()
                delay(60_000)
            }
        }
    }

    fun updateHistoryPreset(preset: DateFilterPreset) {
        selectedHistoryPreset.value = preset
    }

    fun updateHistoryCategory(categoryId: Long?) {
        selectedHistoryCategoryId.value = categoryId
    }

    fun updateCurrency(currencyCode: String) {
        viewModelScope.launch {
            currencyPreferenceStore.updateCurrency(currencyCode)
            val selected = currencyPreferenceStore.supportedCurrencies
                .firstOrNull { it.code == currencyCode }
                ?.name
                ?: currencyCode
            events.emit(UiEvent.Message("Currency changed to $selected."))
        }
    }

    fun addTransaction(
        amountInput: String,
        categoryId: Long?,
        note: String,
    ) {
        viewModelScope.launch {
            val amount = amountInput.toDoubleOrNull()
            if (amount == null || amount <= 0.0) {
                events.emit(UiEvent.Message("Enter a valid amount to save this transaction."))
                return@launch
            }

            if (categoryId == null) {
                events.emit(UiEvent.Message("Pick a category before adding the transaction."))
                return@launch
            }

            repository.addTransaction(
                TransactionEntity(
                    amount = amount,
                    categoryId = categoryId,
                    timestamp = System.currentTimeMillis(),
                    note = note.trim(),
                ),
            )

            val totalBudget = repository.getTotalBudgetLimit()
            val categoryName = repository.getCategoryById(categoryId)?.name ?: "this category"
            if (totalBudget > 0.0 && amount > totalBudget * 0.2) {
                notificationManager.showOverspendAlert(
                    amount = amount,
                    totalBudget = totalBudget,
                    categoryName = categoryName,
                )
            }

            events.emit(UiEvent.Message("Transaction saved."))
        }
    }

    fun saveCategory(
        categoryId: Long?,
        nameInput: String,
        limitInput: String,
        iconKey: String,
        colorHex: String,
    ) {
        viewModelScope.launch {
            val name = nameInput.trim()
            if (name.isBlank()) {
                events.emit(UiEvent.Message("Give the category a name first."))
                return@launch
            }

            val monthlyLimit = limitInput.toDoubleOrNull()
            if (monthlyLimit == null || monthlyLimit < 0.0) {
                events.emit(UiEvent.Message("Enter a valid monthly limit."))
                return@launch
            }

            repository.upsertCategory(
                CategoryEntity(
                    id = categoryId ?: 0L,
                    name = name,
                    icon = iconKey,
                    monthlyLimit = monthlyLimit,
                    colorHex = colorHex,
                ),
            )

            events.emit(
                UiEvent.Message(
                    if (categoryId == null) {
                        "Category created."
                    } else {
                        "Category updated."
                    },
                ),
            )
        }
    }

    fun deleteCategory(category: CategoryEntity) {
        viewModelScope.launch {
            repository.deleteCategory(category)
            if (selectedHistoryCategoryId.value == category.id) {
                selectedHistoryCategoryId.value = null
            }
            events.emit(UiEvent.Message("Category deleted with its linked history."))
        }
    }

    fun exportMonthlySummary(format: ExportFormat) {
        viewModelScope.launch {
            val report = repository.buildMonthlyReport(currentDateTime.value.toLocalDate())
            val uri = when (format) {
                ExportFormat.CSV -> exportManager.exportCsv(report)
                ExportFormat.PDF -> exportManager.exportPdf(report)
            }
            events.emit(
                UiEvent.Share(
                    uri = uri,
                    mimeType = if (format == ExportFormat.CSV) "text/csv" else "application/pdf",
                    title = "${report.monthLabel} budget summary",
                ),
            )
        }
    }
}
