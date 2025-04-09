package com.mkarshnas6.karenstudio.karenbalance

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity.MODE_PRIVATE
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

        lateinit var pref: SharedPreferences
        private var totalExpenseToday = 0
        private var dailySpendingLimit = 0
        private val persian_date_today: String
            get() = PersianDate.getPersianDateToday()

        @SuppressLint("CheckResult", "CommitPrefEdits")
        fun handleEndOfDayTasks(context: Context) {
            val dailyDAO = DBHandler.getDatabase(context).dailyDao()
            val reportDAO = DBHandler.getDatabase(context).reportDao()
            pref = context.getSharedPreferences("Prefs_KarenBalance", MODE_PRIVATE)

            var saving_income = pref.getLong("saving_income", 0)
            val monthly_income = pref.getLong("monthly_income", 0)
            dailySpendingLimit = (monthly_income / 31).toInt()

            GlobalScope.launch(Dispatchers.IO) {
                dailyDAO.getDailys.subscribe { expenses ->
                    totalExpenseToday = expenses.sumOf { it.price }

                    expenses.forEach { expense ->
                        val monthlyExpense = ReportEntity(
                            price = expense.price,
                            description = expense.description,
                            date = expense.date,
                            time = expense.time
                        )
                        reportDAO.insertReport(monthlyExpense)
                    }

                    val savingDifference: Int
                    val isOverspent: Boolean

                    if (totalExpenseToday <= dailySpendingLimit) {
                        savingDifference = dailySpendingLimit - totalExpenseToday
                        saving_income += savingDifference
                        isOverspent = false
                    } else {
                        savingDifference = totalExpenseToday - dailySpendingLimit
                        saving_income -= savingDifference
                        isOverspent = true
                    }

                    pref.edit().putLong("saving_income", saving_income).apply()

                    dailyDAO.deleteAllDailys()
                    sendNotification(context, savingDifference, isOverspent)
                }
            }

//            check the monthly if month is pass
            clearTableIfNewMonthStarted(context)

        }

        private fun sendNotification(
            context: Context,
            savingDifference: Int,
            isOverspent: Boolean
        ) {
            val notificationIntent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notificationText = if (isOverspent) {
                context.getString(R.string.notification_overspent, savingDifference.toLong().format_number().toString())
            } else {
                context.getString(R.string.notification_saved, savingDifference.toLong().format_number().toString())
            }


            val notification = NotificationCompat.Builder(context, "channel")
                .setSmallIcon(R.drawable.ic_money_navigation)
                .setContentTitle(context.getString(R.string.daily_financial_report))
                .setContentText(notificationText)
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

        fun clearTableIfNewMonthStarted(context: Context) {
            val db = DBHandler.getDatabase(context)
            val reportDao = db.reportDao()

            GlobalScope.launch(Dispatchers.IO) {
                val latestRecord = reportDao.getOldestRecord()

                latestRecord?.let {
                    val persianDateToday = PersianDate.getPersianDateToday()
                    val currentMonth = persianDateToday.split("/")[1].toInt()
                    val lastPersianDate = it.date
                    val lastRecordMonth = lastPersianDate.split("/")[1].toInt()

                    if (lastRecordMonth != currentMonth) {
                        reportDao.deleteAllMonthlys()

                        val notificationIntent = Intent(context, MainActivity::class.java)
                        val pendingIntent = PendingIntent.getActivity(
                            context,
                            0,
                            notificationIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )

                        val notification = NotificationCompat.Builder(context, "channel")
                            .setSmallIcon(R.drawable.ic_money_navigation)
                            .setContentTitle(context.getString(R.string.new_month_started))
                            .setContentText(context.getString(R.string.new_month_description))
                            .setPriority(NotificationCompat.PRIORITY_HIGH)
                            .setContentIntent(pendingIntent)
                            .setAutoCancel(true)
                            .build()

                        if (ActivityCompat.checkSelfPermission(
                                context,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {
                            NotificationManagerCompat.from(context).notify(2, notification)
                        }
                    }
                }
            }
        }

        
    }
}
