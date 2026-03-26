package com.suman.smartbudgeter.domain.util

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.suman.smartbudgeter.worker.RecurringExpenseWorker
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

object RecurringExpenseScheduler {

    fun schedule(context: Context) {
        val now = LocalDateTime.now()
        val nextRun = now.withHour(2).withMinute(0).withSecond(0).withNano(0)
            .let { if (it.isAfter(now)) it else it.plusDays(1) }
        val delayMinutes = Duration.between(now, nextRun).toMinutes().coerceAtLeast(15)

        val workRequest = PeriodicWorkRequestBuilder<RecurringExpenseWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(delayMinutes, TimeUnit.MINUTES)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                    .build(),
            )
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            UNIQUE_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest,
        )
    }

    private const val UNIQUE_WORK_NAME = "recurring-expense-check"
}
