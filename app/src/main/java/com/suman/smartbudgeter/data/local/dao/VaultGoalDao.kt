package com.suman.smartbudgeter.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.suman.smartbudgeter.data.local.entity.VaultGoalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VaultGoalDao {

    @Query("SELECT * FROM VaultGoalTable ORDER BY Name ASC")
    fun observeVaultGoals(): Flow<List<VaultGoalEntity>>

    @Query("SELECT COUNT(*) FROM VaultGoalTable")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(goals: List<VaultGoalEntity>)
}
