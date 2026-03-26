package com.suman.smartbudgeter.domain.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.suman.smartbudgeter.R

class BudgetNotificationManager(
    private val context: Context,
) {

    fun createChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Budget alerts",
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply {
                description = "Overspend and recurring expense alerts"
            }

            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    fun showOverspendAlert(
        amount: Double,
        totalBudget: Double,
        categoryName: String,
    ) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_budget)
            .setContentTitle("Large spend detected")
            .setContentText(
                "${amount.asCurrency()} on $categoryName passed 20% of your ${totalBudget.asCurrency()} budget.",
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(OVERSPEND_NOTIFICATION_ID, notification)
    }

    fun showRecurringApplied(count: Int) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_budget)
            .setContentTitle("Recurring expenses posted")
            .setContentText("$count scheduled expense${if (count == 1) "" else "s"} added to this month.")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(RECURRING_NOTIFICATION_ID, notification)
    }

    companion object {
        private const val CHANNEL_ID = "budget_alerts"
        private const val OVERSPEND_NOTIFICATION_ID = 1001
        private const val RECURRING_NOTIFICATION_ID = 1002
    }
}
