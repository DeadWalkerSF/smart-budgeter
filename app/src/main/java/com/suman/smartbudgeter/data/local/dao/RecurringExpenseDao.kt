package com.suman.smartbudgeter.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.suman.smartbudgeter.data.local.entity.RecurringExpenseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecurringExpenseDao {

    @Query("SELECT * FROM RecurringExpenseTable ORDER BY DayOfMonth ASC, Name ASC")
    fun observeRecurringExpenses(): Flow<List<RecurringExpenseEntity>>

    @Query("SELECT * FROM RecurringExpenseTable WHERE IsActive = 1")
    suspend fun getActiveRecurringExpenses(): List<RecurringExpenseEntity>

    @Query("SELECT COUNT(*) FROM RecurringExpenseTable")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(expenses: List<RecurringExpenseEntity>)

    @Upsert
    suspend fun upsert(expense: RecurringExpenseEntity)

    @Query("UPDATE RecurringExpenseTable SET LastAppliedMonth = :monthKey WHERE ID = :id")
    suspend fun updateLastAppliedMonth(id: Long, monthKey: String)
}
