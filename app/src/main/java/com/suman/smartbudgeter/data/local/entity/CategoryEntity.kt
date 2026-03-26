package com.suman.smartbudgeter.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "CategoryTable")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "ID")
    val id: Long = 0,
    @ColumnInfo(name = "Name")
    val name: String,
    @ColumnInfo(name = "Icon")
    val icon: String,
    @ColumnInfo(name = "MonthlyLimit")
    val monthlyLimit: Double,
    @ColumnInfo(name = "Color")
    val colorHex: String,
)
