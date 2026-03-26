package com.deadwalkersf.smartbudgeter.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.deadwalkersf.smartbudgeter.data.local.dao.CategoryDao
import com.deadwalkersf.smartbudgeter.data.local.dao.RecurringExpenseDao
import com.deadwalkersf.smartbudgeter.data.local.dao.TransactionDao
import com.deadwalkersf.smartbudgeter.data.local.dao.VaultGoalDao
import com.deadwalkersf.smartbudgeter.data.local.entity.CategoryEntity
import com.deadwalkersf.smartbudgeter.data.local.entity.RecurringExpenseEntity
import com.deadwalkersf.smartbudgeter.data.local.entity.TransactionEntity
import com.deadwalkersf.smartbudgeter.data.local.entity.VaultGoalEntity

@Database(
    entities = [
        CategoryEntity::class,
        TransactionEntity::class,
        RecurringExpenseEntity::class,
        VaultGoalEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class BudgetDatabase : RoomDatabase() {

    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao
    abstract fun recurringExpenseDao(): RecurringExpenseDao
    abstract fun vaultGoalDao(): VaultGoalDao

    companion object {
        @Volatile
        private var instance: BudgetDatabase? = null

        fun getInstance(context: Context): BudgetDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    BudgetDatabase::class.java,
                    "smart_budgeter.db",
                ).build().also { instance = it }
            }
        }
    }
}
