package com.mkarshnas6.karenstudio.karenbalance

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.mkarshnas6.karenstudio.karenbalance.db.DBHandler
import com.mkarshnas6.karenstudio.karenbalance.db.model.ReportEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.Calendar

class BroadcastEndDayReceiver() {

    companion object {

        @SuppressLint("CheckResult")
        fun handleEndOfDayTasks(context: Context) {
            val dailyDAO = DBHandler.getDatabase(context).dailyDao()
            val reportDAO = DBHandler.getDatabase(context).reportDao()

            GlobalScope.launch(Dispatchers.IO) {
                dailyDAO.getDailys.subscribe { expenses ->
                    expenses.forEach { expense ->
                        val monthlyExpense = ReportEntity(
                            price = expense.price,
                            description = expense.description,
                            date = expense.date,
                            time = expense.time
                        )
                        reportDAO.insertReport(monthlyExpense)
                    }
                    dailyDAO.deleteAllDailys() // حذف اطلاعات روزانه
                }
                sendNotification(context) // ارسال نوتیفیکیشن
            }
        }

        private fun sendNotification(context: Context) {
            val notificationIntent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(context, "channel")
                .setSmallIcon(R.drawable.ic_money_navigation)
                .setContentTitle("Daily expenses reset completed.")
                .setContentText("Good night! 😊")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()

            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                NotificationManagerCompat.from(context).notify(1, notification)
            }
        }

        private fun getTodayDate(): String {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH) + 1
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            return "$year-$month-$day"
        }
    }
}
