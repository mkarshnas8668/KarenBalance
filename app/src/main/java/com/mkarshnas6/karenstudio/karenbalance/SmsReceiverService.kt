package com.mkarshnas6.karenstudio.karenbalance

import PersianDate
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.provider.Telephony
import android.telephony.SmsMessage
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.mkarshnas6.karenstudio.karenbalance.db.DBHandler
import com.mkarshnas6.karenstudio.karenbalance.db.model.DailyEntity
import java.sql.Date
import java.text.SimpleDateFormat
import java.util.Locale

class SmsReceiverService : Service() {
    private val CHANNEL_ID = "channel"
    private val NOTIFICATION_ID = 1
    lateinit var pref: SharedPreferences

    // ................... get data of pershian date algoritm ...........
//    private val persian_date_today = PersianDate.getCurrentShamsiDate()

    private val persian_date_today: String
        get() = PersianDate.getPersianDateToday()


    private val smsReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
                val bundle: Bundle? = intent.extras
                if (bundle != null) {
                    val pdus = bundle.get("pdus") as? Array<*>
                    if (!pdus.isNullOrEmpty()) {
                        for (pdu in pdus) {
                            val message = SmsMessage.createFromPdu(pdu as ByteArray)
                            val sender = message.originatingAddress
                            val body = message.messageBody
                            val money_balance_change = extract_Spent_SMS(body)
                            val name_bank = pref.getString("name_bank", "Error to get name bank !!")

//                            ...... get Date & time in SMS .........
                            val timestamp = message.timestampMillis
//                            Format time ..............
                            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                            val formattedTime = timeFormat.format(Date(timestamp))



//                            Toast.makeText(
//                                this@SmsReceiverService,
//                                "ساعت : $formattedTime  || تاریخ : $persian_date_today ",
//                                Toast.LENGTH_LONG
//                            ).show()
//
//                            Toast.makeText(
//                                this@SmsReceiverService,
//                                "Money = $money_balance_change",
//                                Toast.LENGTH_LONG
//                            ).show()
// ....                           check name bank and sender with lower and space ..................
                            if (sender!!.lowercase().replace(" ", "") == name_bank!!.lowercase()
                                    .replace(" ", "")
                            ) {
//                                Toast.makeText(
//                                    this@SmsReceiverService,
//                                    "$sender : $body ",
//                                    Toast.LENGTH_LONG
//                                ).show()

//                                Save Information daily spend in DB .....................................
                                if (money_balance_change.replace(",", "").toInt() != 0) {
                                    val daily = DailyEntity(
                                        price = money_balance_change.replace(",", "").replace("-","").toInt(),
                                        description = "NULL",
                                        date = persian_date_today,
                                        time = formattedTime
                                    )
                                    Thread {
                                        val db = DBHandler.getDatabase(context)
                                        db.dailyDao().insertDaily(daily)
                                    }.start()
                                }
//                              ............. end Save information ..................

                                // ارسال نوتیفیکیشن
                                val notificationIntent = Intent(context, MainActivity::class.java)
                                val pendingIntent = PendingIntent.getActivity(
                                    context,
                                    0,
                                    notificationIntent,
                                    PendingIntent.FLAG_UPDATE_CURRENT
                                )

                                val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                                    .setSmallIcon(R.drawable.ic_money_navigation)
                                    .setContentTitle("new Sms from $sender in date : $persian_date_today & in time : $formattedTime")
                                    .setContentText(body)
                                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                                    .setContentIntent(pendingIntent)
                                    .setAutoCancel(true)
                                    .build()

                                NotificationManagerCompat.from(context)
                                    .notify(NOTIFICATION_ID, notification)
                            }

                        }
                    }
                }
            }
        }
    }

    fun extract_Spent_SMS(text: String): String {
        val regex = Regex("""[-+]?\d{1,3}(,\d{3})+[-+]?""")
        val moneys = regex.findAll(text).map { it.value.trim() }.toList()
        var price = "0"
        for (money in moneys) {
            if (money.contains("+") || money.contains("-")) {
                var updatedMoney = money

                if (updatedMoney[0] == '+' || updatedMoney[0] == '-') {
                    price = updatedMoney
                } else {
                    if (updatedMoney.last() == '+') {
                        updatedMoney = updatedMoney.dropLast(1)
                        price = "+$updatedMoney"
                    } else if (updatedMoney.last() == '-') {
                        updatedMoney = updatedMoney.dropLast(1)
                        price = "-$updatedMoney"
                    }
                }
            }
        }
        return price
    }


    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Karen Balance Notifications"
            val descriptionText = "Notifications for Karen Balance"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Karen Balance Service")
            .setContentText("Listening for incoming SMS messages...")
            .setSmallIcon(R.drawable.target)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        startForeground(NOTIFICATION_ID, notification)

        val filter = IntentFilter(Telephony.Sms.Intents.SMS_RECEIVED_ACTION)
        registerReceiver(smsReceiver, filter)

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(smsReceiver)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        pref = getSharedPreferences("Prefs_KarenBalance", Context.MODE_PRIVATE)
    }
}