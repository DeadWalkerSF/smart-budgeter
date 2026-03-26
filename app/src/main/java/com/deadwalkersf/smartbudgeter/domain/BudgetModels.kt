package com.deadwalkersf.smartbudgeter.domain

import com.deadwalkersf.smartbudgeter.data.local.model.TransactionRecord
import java.time.LocalDateTime

enum class DateFilterPreset(val label: String) {
    LAST_7_DAYS("7D"),
    LAST_30_DAYS("30D"),
    THIS_MONTH("Month"),
    ALL("All"),
}

enum class ExportFormat {
    CSV,
    PDF,
}

data class DateWindow(
    val startInclusive: Long,
    val endInclusive: Long,
)

data class HistoryFilter(
    val preset: DateFilterPreset,
    val categoryId: Long?,
    val window: DateWindow,
)

data class CategoryBudgetStatus(
    val categoryId: Long,
    val categoryName: String,
    val iconKey: String,
    val colorHex: String,
    val monthlyLimit: Double,
    val spent: Double,
    val remaining: Double,
    val progress: Float,
)

data class SpendingTrendPoint(
    val label: String,
    val amount: Double,
)

data class CategoryBreakdownSlice(
    val label: String,
    val amount: Double,
    val colorHex: String,
)

data class BudgetSnapshot(
    val totalBudget: Double = 0.0,
    val totalSpent: Double = 0.0,
    val remainingBudget: Double = 0.0,
    val dailyAllowance: Double = 0.0,
    val budgetProgress: Float = 0f,
    val burnRateTriggered: Boolean = false,
    val categoryStatuses: List<CategoryBudgetStatus> = emptyList(),
    val trend: List<SpendingTrendPoint> = emptyList(),
    val categoryBreakdown: List<CategoryBreakdownSlice> = emptyList(),
)

data class MonthlyBudgetReport(
    val monthLabel: String,
    val generatedAt: LocalDateTime,
    val snapshot: BudgetSnapshot,
    val transactions: List<TransactionRecord>,
)
