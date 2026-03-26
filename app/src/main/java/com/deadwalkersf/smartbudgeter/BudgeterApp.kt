package com.deadwalkersf.smartbudgeter

import android.app.Application
import com.deadwalkersf.smartbudgeter.data.local.BudgetDatabase
import com.deadwalkersf.smartbudgeter.data.preferences.CurrencyPreferenceStore
import com.deadwalkersf.smartbudgeter.data.repository.BudgetRepository
import com.deadwalkersf.smartbudgeter.domain.util.BudgetNotificationManager
import com.deadwalkersf.smartbudgeter.domain.util.ExportManager
import com.deadwalkersf.smartbudgeter.domain.util.RecurringExpenseScheduler

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
