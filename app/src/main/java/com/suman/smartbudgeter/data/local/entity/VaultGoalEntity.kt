package com.suman.smartbudgeter.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "VaultGoalTable")
data class VaultGoalEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "ID")
    val id: Long = 0,
    @ColumnInfo(name = "Name")
    val name: String,
    @ColumnInfo(name = "TargetAmount")
    val targetAmount: Double,
    @ColumnInfo(name = "SavedAmount")
    val savedAmount: Double,
    @ColumnInfo(name = "Color")
    val colorHex: String,
)
