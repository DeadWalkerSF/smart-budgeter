package com.suman.smartbudgeter.domain.util

import com.suman.smartbudgeter.data.local.entity.CategoryEntity
import com.suman.smartbudgeter.data.local.entity.TransactionEntity
import com.suman.smartbudgeter.domain.BudgetSnapshot
import com.suman.smartbudgeter.domain.CategoryBreakdownSlice
import com.suman.smartbudgeter.domain.CategoryBudgetStatus
import com.suman.smartbudgeter.domain.DateFilterPreset
import com.suman.smartbudgeter.domain.DateWindow
import com.suman.smartbudgeter.domain.SpendingTrendPoint
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.ZoneId
import kotlin.math.max

object BudgetCalculator {

    private val zoneId: ZoneId = ZoneId.systemDefault()

    fun currentMonthWindow(today: LocalDate = LocalDate.now()): DateWindow {
        val firstDay = today.withDayOfMonth(1)
        val lastDay = today.withDayOfMonth(today.lengthOfMonth())
        return DateWindow(
            startInclusive = firstDay.atStartOfDay(zoneId).toInstant().toEpochMilli(),
            endInclusive = lastDay.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli() - 1,
        )
    }

    fun windowForPreset(
        preset: DateFilterPreset,
        today: LocalDate = LocalDate.now(),
    ): DateWindow {
        return when (preset) {
            DateFilterPreset.LAST_7_DAYS -> {
                val start = today.minusDays(6)
                DateWindow(
                    startInclusive = start.atStartOfDay(zoneId).toInstant().toEpochMilli(),
                    endInclusive = today.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli() - 1,
                )
            }

            DateFilterPreset.LAST_30_DAYS -> {
                val start = today.minusDays(29)
                DateWindow(
                    startInclusive = start.atStartOfDay(zoneId).toInstant().toEpochMilli(),
                    endInclusive = today.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli() - 1,
                )
            }

            DateFilterPreset.THIS_MONTH -> currentMonthWindow(today)
            DateFilterPreset.ALL -> DateWindow(0L, Long.MAX_VALUE)
        }
    }

    fun buildSnapshot(
        categories: List<CategoryEntity>,
        transactions: List<TransactionEntity>,
        today: LocalDate = LocalDate.now(),
    ): BudgetSnapshot {
        val totalBudget = categories.sumOf { it.monthlyLimit }
        val totalSpent = transactions.sumOf { it.amount }
        val remainingBudget = totalBudget - totalSpent
        val daysLeftInMonth = max(1, today.lengthOfMonth() - today.dayOfMonth + 1)
        val dailyAllowance = remainingBudget / daysLeftInMonth
        val budgetProgress = if (totalBudget > 0.0) {
            (totalSpent / totalBudget).toFloat().coerceIn(0f, 1f)
        } else {
            0f
        }
        val spendingByCategory = transactions.groupBy { it.categoryId }.mapValues { (_, value) ->
            value.sumOf { transaction -> transaction.amount }
        }

        val categoryStatuses = categories.map { category ->
            val spent = spendingByCategory[category.id] ?: 0.0
            val progress = if (category.monthlyLimit > 0.0) {
                (spent / category.monthlyLimit).toFloat().coerceAtLeast(0f)
            } else {
                0f
            }

            CategoryBudgetStatus(
                categoryId = category.id,
                categoryName = category.name,
                iconKey = category.icon,
                colorHex = category.colorHex,
                monthlyLimit = category.monthlyLimit,
                spent = spent,
                remaining = category.monthlyLimit - spent,
                progress = progress,
            )
        }.sortedByDescending { it.spent }

        val trendStartDate = today.minusDays(6)
        val trend = (0L..6L).map { offset ->
            val currentDay = trendStartDate.plusDays(offset)
            val amount = transactions
                .filter { transaction -> transaction.timestamp.toLocalDate() == currentDay }
                .sumOf { it.amount }
            SpendingTrendPoint(
                label = currentDay.dayOfWeek.name.take(3),
                amount = amount,
            )
        }

        val categoryBreakdown = categoryStatuses
            .filter { it.spent > 0.0 }
            .map {
                CategoryBreakdownSlice(
                    label = it.categoryName,
                    amount = it.spent,
                    colorHex = it.colorHex,
                )
            }

        return BudgetSnapshot(
            totalBudget = totalBudget,
            totalSpent = totalSpent,
            remainingBudget = remainingBudget,
            dailyAllowance = dailyAllowance,
            budgetProgress = budgetProgress,
            burnRateTriggered = today.dayOfMonth < 15 && totalBudget > 0.0 && totalSpent > totalBudget * 0.5,
            categoryStatuses = categoryStatuses,
            trend = trend,
            categoryBreakdown = categoryBreakdown,
        )
    }

    fun quickAddSuggestions(
        categories: List<CategoryEntity>,
        time: LocalTime = LocalTime.now(),
    ): List<CategoryEntity> {
        val preferredNames = when (time.hour) {
            in 5..10 -> listOf("Coffee", "Groceries", "Transport")
            in 11..15 -> listOf("Dining", "Transport", "Groceries")
            in 16..21 -> listOf("Dining", "Entertainment", "Shopping")
            else -> listOf("Subscriptions", "Coffee", "Entertainment")
        }
        val timeMatched = preferredNames.mapNotNull { preferredName ->
            categories.firstOrNull { it.name.equals(preferredName, ignoreCase = true) }
        }

        val fallback = categories
            .filterNot { category -> timeMatched.any { it.id == category.id } }
            .sortedByDescending { it.monthlyLimit }
            .take(3)

        return (timeMatched + fallback).distinctBy { it.id }.take(3)
    }

    fun monthLabel(today: LocalDate = LocalDate.now()): String {
        val yearMonth = YearMonth.from(today)
        return "${yearMonth.month.name.lowercase().replaceFirstChar(Char::titlecase)} ${yearMonth.year}"
    }

    private fun Long.toLocalDate(): LocalDate {
        return Instant.ofEpochMilli(this).atZone(zoneId).toLocalDate()
    }
}
