package com.suman.smartbudgeter.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.suman.smartbudgeter.data.local.BudgetDatabase
import com.suman.smartbudgeter.data.repository.BudgetRepository
import com.suman.smartbudgeter.domain.util.BudgetNotificationManager

class RecurringExpenseWorker(
    appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return runCatching {
            val repository = BudgetRepository(BudgetDatabase.getInstance(applicationContext))
            val appliedCount = repository.applyDueRecurringExpenses()
            if (appliedCount > 0) {
                BudgetNotificationManager(applicationContext).showRecurringApplied(appliedCount)
            }
            Result.success()
        }.getOrElse {
            Result.retry()
        }
    }
}
