package com.mkarshnas6.karenstudio.karenbalance

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("BootReceiver", "Device rebooted, rescheduling alarm...")
            AlarmScheduler.scheduleAlarm(context) // تنظیم دوباره آلارم بعد از ریست
        }
    }
}
