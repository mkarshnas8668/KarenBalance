package com.mkarshnas6.karenstudio.karenbalance

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat

class MyReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("MyReceiver", "Alarm triggered at 10 PM!")

        // بررسی وضعیت ذخیره پول و اجرای عملیات مورد نظر
        checkAndSaveUserBalance(context)

        // تنظیم مجدد آلارم برای فردا
        AlarmScheduler.scheduleAlarm(context)
    }

    @SuppressLint("ObsoleteSdkInt")
    private fun checkAndSaveUserBalance(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notificationChannelId = "com.mkarshnas6.karenstudio.karenbalance"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(notificationChannelId, "Daily Reminder", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, notificationChannelId)
            .setSmallIcon(R.drawable.target)
            .setContentTitle("KarenBalance Reminder")
            .setContentText("Check your daily balance!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        notificationManager.notify(1, notification)
    }

}
