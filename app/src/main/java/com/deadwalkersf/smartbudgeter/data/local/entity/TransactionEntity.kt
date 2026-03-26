package com.deadwalkersf.smartbudgeter.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "TransactionTable",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["ID"],
            childColumns = ["CategoryID"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["CategoryID"]),
        Index(value = ["Timestamp"]),
    ],
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "ID")
    val id: Long = 0,
    @ColumnInfo(name = "Amount")
    val amount: Double,
    @ColumnInfo(name = "CategoryID")
    val categoryId: Long,
    @ColumnInfo(name = "Timestamp")
    val timestamp: Long,
    @ColumnInfo(name = "Note")
    val note: String = "",
)
