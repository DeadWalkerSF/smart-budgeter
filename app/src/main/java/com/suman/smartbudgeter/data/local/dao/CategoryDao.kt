package com.suman.smartbudgeter.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.suman.smartbudgeter.data.local.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {

    @Query("SELECT * FROM CategoryTable ORDER BY Name ASC")
    fun observeCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM CategoryTable ORDER BY Name ASC")
    suspend fun getCategories(): List<CategoryEntity>

    @Query("SELECT COUNT(*) FROM CategoryTable")
    suspend fun getCount(): Int

    @Query("SELECT * FROM CategoryTable WHERE ID = :id LIMIT 1")
    suspend fun getCategoryById(id: Long): CategoryEntity?

    @Query("SELECT COALESCE(SUM(MonthlyLimit), 0) FROM CategoryTable")
    suspend fun getTotalBudgetLimit(): Double

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(categories: List<CategoryEntity>)

    @Upsert
    suspend fun upsert(category: CategoryEntity)

    @Delete
    suspend fun delete(category: CategoryEntity)
}
