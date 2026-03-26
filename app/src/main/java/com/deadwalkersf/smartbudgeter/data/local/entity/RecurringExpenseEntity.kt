package com.deadwalkersf.smartbudgeter.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "RecurringExpenseTable",
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
        Index(value = ["DayOfMonth", "IsActive"]),
    ],
)
data class RecurringExpenseEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "ID")
    val id: Long = 0,
    @ColumnInfo(name = "Name")
    val name: String,
    @ColumnInfo(name = "Amount")
    val amount: Double,
    @ColumnInfo(name = "CategoryID")
    val categoryId: Long,
    @ColumnInfo(name = "DayOfMonth")
    val dayOfMonth: Int,
    @ColumnInfo(name = "Note")
    val note: String = "",
    @ColumnInfo(name = "IsActive")
    val isActive: Boolean = true,
    @ColumnInfo(name = "LastAppliedMonth")
    val lastAppliedMonth: String? = null,
)
