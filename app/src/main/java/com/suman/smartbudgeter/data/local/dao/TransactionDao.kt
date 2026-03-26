package com.suman.smartbudgeter.data.local.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.suman.smartbudgeter.data.local.entity.TransactionEntity
import com.suman.smartbudgeter.data.local.model.CategorySpendingRow
import com.suman.smartbudgeter.data.local.model.TransactionRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Query(
        """
        SELECT * FROM TransactionTable
        WHERE Timestamp BETWEEN :startInclusive AND :endInclusive
        ORDER BY Timestamp DESC
        """,
    )
    fun observeTransactionsBetween(
        startInclusive: Long,
        endInclusive: Long,
    ): Flow<List<TransactionEntity>>

    @Query(
        """
        SELECT * FROM TransactionTable
        WHERE Timestamp BETWEEN :startInclusive AND :endInclusive
        ORDER BY Timestamp DESC
        """,
    )
    suspend fun getTransactionsBetween(
        startInclusive: Long,
        endInclusive: Long,
    ): List<TransactionEntity>

    @Query(
        """
        SELECT
            CategoryID AS categoryId,
            COALESCE(SUM(Amount), 0) AS spent
        FROM TransactionTable
        WHERE Timestamp BETWEEN :startInclusive AND :endInclusive
        GROUP BY CategoryID
        """,
    )
    suspend fun getCategorySpendingBetween(
        startInclusive: Long,
        endInclusive: Long,
    ): List<CategorySpendingRow>

    @Query(
        """
        SELECT
            t.ID AS id,
            t.Amount AS amount,
            t.Timestamp AS timestamp,
            t.Note AS note,
            c.ID AS categoryId,
            c.Name AS categoryName,
            c.Icon AS categoryIcon,
            c.Color AS categoryColor
        FROM TransactionTable t
        INNER JOIN CategoryTable c ON c.ID = t.CategoryID
        WHERE t.Timestamp BETWEEN :startInclusive AND :endInclusive
            AND (:categoryId IS NULL OR t.CategoryID = :categoryId)
        ORDER BY t.Timestamp DESC
        """,
    )
    fun historyPagingSource(
        startInclusive: Long,
        endInclusive: Long,
        categoryId: Long?,
    ): PagingSource<Int, TransactionRecord>
}
