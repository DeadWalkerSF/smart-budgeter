package com.suman.smartbudgeter.domain.util

import androidx.compose.ui.graphics.Color
import java.text.NumberFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Currency
import java.util.Locale
import kotlin.math.abs

private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM, hh:mm a", Locale.getDefault())
private val fallbackCurrencyCode = "USD"

@Volatile
private var activeCurrencyCode: String = defaultCurrencyCode()

fun defaultCurrencyCode(): String {
    return runCatching {
        Currency.getInstance(Locale.getDefault()).currencyCode
    }.getOrDefault(fallbackCurrencyCode)
}

fun selectedCurrencyCode(): String = activeCurrencyCode

fun updateSelectedCurrencyCode(code: String) {
    activeCurrencyCode = runCatching {
        Currency.getInstance(code).currencyCode
    }.getOrDefault(defaultCurrencyCode())
}

fun currentCurrencySymbol(): String {
    return runCatching {
        Currency.getInstance(activeCurrencyCode).getSymbol(Locale.getDefault())
    }.getOrDefault("$")
}

fun Double.asCurrency(): String {
    return NumberFormat.getCurrencyInstance(Locale.getDefault()).apply {
        currency = runCatching {
            Currency.getInstance(activeCurrencyCode)
        }.getOrDefault(Currency.getInstance(defaultCurrencyCode()))
    }.format(this)
}

fun Double.asCompactCurrency(): String {
    val absoluteValue = abs(this)
    val symbol = currentCurrencySymbol()
    return when {
        absoluteValue >= 1_000_000 -> symbol + compactCurrencyNumber(this / 1_000_000) + "M"
        absoluteValue >= 1_000 -> symbol + compactCurrencyNumber(this / 1_000) + "K"
        absoluteValue >= 100 -> symbol + String.format(Locale.getDefault(), "%.0f", this)
        absoluteValue > 0.0 -> asCurrency()
        else -> symbol + "0"
    }
}

fun Double.asPercent(decimals: Int = 2): String =
    String.format(Locale.getDefault(), "%.${decimals}f%%", this)

fun Long.toDisplayDateTime(): String {
    return Instant.ofEpochMilli(this)
        .atZone(ZoneId.systemDefault())
        .format(dateFormatter)
}

fun String.toComposeColor(): Color {
    return Color(android.graphics.Color.parseColor(this))
}

private fun compactCurrencyNumber(value: Double): String {
    return if (abs(value) >= 10) {
        String.format(Locale.getDefault(), "%.0f", value)
    } else {
        String.format(Locale.getDefault(), "%.1f", value)
    }
}
