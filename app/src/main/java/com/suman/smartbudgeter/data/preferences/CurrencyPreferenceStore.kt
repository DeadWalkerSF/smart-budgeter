package com.suman.smartbudgeter.data.preferences

import android.content.Context
import com.suman.smartbudgeter.domain.CurrencyOption
import com.suman.smartbudgeter.domain.util.defaultCurrencyCode
import com.suman.smartbudgeter.domain.util.updateSelectedCurrencyCode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Currency
import java.util.Locale

class CurrencyPreferenceStore(
    context: Context,
) {
    private val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val _selectedCurrencyCode = MutableStateFlow(
        preferences.getString(KEY_SELECTED_CURRENCY, defaultCurrencyCode()) ?: defaultCurrencyCode(),
    )

    val selectedCurrencyCode: StateFlow<String> = _selectedCurrencyCode.asStateFlow()
    val supportedCurrencies: List<CurrencyOption> = supportedCurrencyOptions

    init {
        updateSelectedCurrencyCode(_selectedCurrencyCode.value)
    }

    fun updateCurrency(code: String) {
        if (_selectedCurrencyCode.value == code) return

        preferences.edit().putString(KEY_SELECTED_CURRENCY, code).apply()
        _selectedCurrencyCode.value = code
        updateSelectedCurrencyCode(code)
    }

    companion object {
        private const val PREFS_NAME = "budgeter_preferences"
        private const val KEY_SELECTED_CURRENCY = "selected_currency_code"

        private val supportedCurrencyOptions = listOf(
            currencyOption("USD", "US Dollar"),
            currencyOption("INR", "Indian Rupee"),
            currencyOption("EUR", "Euro"),
            currencyOption("GBP", "British Pound"),
            currencyOption("JPY", "Japanese Yen"),
            currencyOption("AED", "UAE Dirham"),
            currencyOption("CAD", "Canadian Dollar"),
            currencyOption("AUD", "Australian Dollar"),
            currencyOption("SGD", "Singapore Dollar"),
        )

        private fun currencyOption(code: String, name: String): CurrencyOption {
            val symbol = runCatching {
                Currency.getInstance(code).getSymbol(Locale.getDefault())
            }.getOrDefault(code)
            return CurrencyOption(code = code, name = name, symbol = symbol)
        }
    }
}
