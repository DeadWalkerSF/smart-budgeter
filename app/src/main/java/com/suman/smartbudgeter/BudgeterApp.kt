package com.suman.smartbudgeter

import android.app.Application
import com.suman.smartbudgeter.data.local.BudgetDatabase
import com.suman.smartbudgeter.data.preferences.CurrencyPreferenceStore
import com.suman.smartbudgeter.data.repository.BudgetRepository
import com.suman.smartbudgeter.domain.util.BudgetNotificationManager
import com.suman.smartbudgeter.domain.util.ExportManager
import com.suman.smartbudgeter.domain.util.RecurringExpenseScheduler

class BudgeterApp : Application() {

    val database: BudgetDatabase by lazy { BudgetDatabase.getInstance(this) }
    val repository: BudgetRepository by lazy { BudgetRepository(database) }
    val currencyPreferenceStore: CurrencyPreferenceStore by lazy { CurrencyPreferenceStore(this) }
    val exportManager: ExportManager by lazy { ExportManager(this) }
    val notificationManager: BudgetNotificationManager by lazy { BudgetNotificationManager(this) }

    override fun onCreate() {
        super.onCreate()
        currencyPreferenceStore
        notificationManager.createChannels()
        RecurringExpenseScheduler.schedule(this)
    }
}
