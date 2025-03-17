package com.mkarshnas6.karenstudio.karenbalance

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class MyReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("MyReceiver", "Alarm triggered at 10 PM!")

        // بررسی وضعیت ذخیره پول و اجرای عملیات مورد نظر
        checkAndSaveUserBalance(context)

        // تنظیم مجدد آلارم برای فردا
        AlarmScheduler.scheduleAlarm(context)
    }

    private fun checkAndSaveUserBalance(context: Context) {
        Log.d("MyReceiver", "Checking user balance and updating records...")
    }
}
