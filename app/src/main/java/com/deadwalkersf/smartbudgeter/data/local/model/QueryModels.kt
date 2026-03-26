package com.deadwalkersf.smartbudgeter.data.local.model

import androidx.room.ColumnInfo

data class CategorySpendingRow(
    @ColumnInfo(name = "categoryId")
    val categoryId: Long,
    @ColumnInfo(name = "spent")
    val spent: Double,
)

data class TransactionRecord(
    @ColumnInfo(name = "id")
    val id: Long,
    @ColumnInfo(name = "amount")
    val amount: Double,
    @ColumnInfo(name = "timestamp")
    val timestamp: Long,
    @ColumnInfo(name = "note")
    val note: String,
    @ColumnInfo(name = "categoryId")
    val categoryId: Long,
    @ColumnInfo(name = "categoryName")
    val categoryName: String,
    @ColumnInfo(name = "categoryIcon")
    val categoryIcon: String,
    @ColumnInfo(name = "categoryColor")
    val categoryColor: String,
)
