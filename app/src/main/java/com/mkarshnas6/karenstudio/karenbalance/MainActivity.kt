package com.mkarshnas6.karenstudio.karenbalance


import PersianDate
import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.mkarshnas6.karenstudio.karenbalance.databinding.ActivityMainBinding
import com.mkarshnas6.karenstudio.karenbalance.db.DBHandler
import com.mkarshnas6.karenstudio.karenbalance.db.model.DailyEntity
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.schedulers.Schedulers
import org.w3c.dom.Text
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val SMS_PERMISSION_REQUEST_CODE = 1
    private val PERMISSIONS_RECEIVE_SMS = 101

    private val persian_date_today: String
        get() = PersianDate.getPersianDateToday()


    private val appPermission =
        arrayOf(Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_MMS)

    @SuppressLint("ObsoleteSdkInt", "CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        send information daily to monthly db
        val db = DBHandler.getDatabase(this)

        db.dailyDao().getLatestDate()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ latestDate ->

                if (latestDate.isNotEmpty() && latestDate != persian_date_today) {
                    BroadcastEndDayReceiver.handleEndOfDayTasks(this)
                }
            }, { error ->

                Toast.makeText(
                    this,
                    "Error fetching latest date: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            })

        db.dailyDao().getDailys
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ expenses ->

                expenses.forEach { expense ->
                    if (expense.description == "NULL") {
                        show_alert_descriptions_null(
                            expense.id,
                            expense.price,
                            expense.date,
                            expense.time
                        )
                    }
                }

            }, { error ->
                Toast.makeText(
                    this,
                    "Error fetching data: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            })

        // تغییر رنگ نوار وضعیت و نوار ناوبری
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.statusBarColor = ContextCompat.getColor(this, R.color.caramel)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = ContextCompat.getColor(this, R.color.caramel)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.navigationBarColor = ContextCompat.getColor(this, R.color.caramel)
        }

//        do Setting for All fragments .............
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragment_container) as NavHostFragment
        val navController = navHostFragment.navController
        binding.bottomNav.setupWithNavController(navController)

//        set action work btn add reports
        binding.actionBtnAddSpent.setOnClickListener { show_alert_add_report() }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.nav_money)
                binding.actionBtnAddSpent.visibility = View.VISIBLE
            else
                binding.actionBtnAddSpent.visibility = View.GONE
        }

//        call function for check permissions ........
        checkSmsPermission()
//        call function for delete DB today on 12PM ........
//        scheduleMidnightAlarm(this)
// ........     end call funcations ..............

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECEIVE_SMS
            )
            != PackageManager.PERMISSION_GRANTED
        ) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.RECEIVE_SMS
                )
            ) {

            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.RECEIVE_SMS),
                    PERMISSIONS_RECEIVE_SMS
                )


            }
        } else {
            // Permission has already been granted
        }

    }

    @SuppressLint("CheckResult")
    fun show_alert_descriptions_null(
        id: Int,
        price: Int,
        date: String,
        time: String
    ) {
        val builderAlert = AlertDialog.Builder(this, R.style.Base_Theme_KarenBalance)
        builderAlert.setTitle("Reports Expense")
        builderAlert.setMessage("Enter your Description :")

        val layoutAlertDialog = LinearLayout(this)
        layoutAlertDialog.orientation = LinearLayout.VERTICAL
        layoutAlertDialog.gravity = Gravity.CENTER
        layoutAlertDialog.setPadding(16, 16, 16, 16)

        val txtPrice = TextView(this)
        txtPrice.setTextColor(ContextCompat.getColor(this, R.color.chocolate_brown))
        txtPrice.setBackgroundResource(R.drawable.back_view_border)
        txtPrice.textSize = 25f
        txtPrice.setText(price.toString())

        val editTxtDescription = EditText(this)
        editTxtDescription.setTextColor(ContextCompat.getColor(this, R.color.chocolate_brown))
        editTxtDescription.hint = "enter your Description for expene"
        editTxtDescription.setBackgroundResource(R.drawable.back_view_border)
        editTxtDescription.textSize = 25f
        editTxtDescription.minHeight = 350
        editTxtDescription.maxHeight = 550

        val layoutParamsEdittxt = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParamsEdittxt.setMargins(5, 5, 5, 30) // Add margin to create spacing
        txtPrice.layoutParams = layoutParamsEdittxt
        editTxtDescription.layoutParams = layoutParamsEdittxt

        layoutAlertDialog.addView(txtPrice)
        layoutAlertDialog.addView(editTxtDescription)

        builderAlert.setView(layoutAlertDialog)
        builderAlert.setPositiveButton("Save", null)

        val dialogMonthlyIncome = builderAlert.create()
        dialogMonthlyIncome.setCancelable(false)
        dialogMonthlyIncome.setCanceledOnTouchOutside(false)

        dialogMonthlyIncome.show()

        val positiveButton = dialogMonthlyIncome.getButton(AlertDialog.BUTTON_POSITIVE)
        val layoutParamsButton = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParamsButton.gravity = Gravity.CENTER // Center the button
        layoutParamsButton.setMargins(0, 20, 0, 0) // Add top margin for spacing
        positiveButton.layoutParams = layoutParamsButton
        positiveButton.setTextColor(
            ContextCompat.getColor(
                this,
                R.color.chocolate_brown
            )
        )
        positiveButton.setBackgroundColor(ContextCompat.getColor(this, R.color.caramel))
        positiveButton.textSize = 18f // Increase text size

        positiveButton.setOnClickListener {
            // update descriptions db
            if (editTxtDescription.text.toString() != "NULL") {
                val db = DBHandler.getDatabase(context = this)

                val updatedDaily = DailyEntity(
                    id = id,
                    price = price,
                    description = editTxtDescription.text.toString(),
                    date = date,
                    time = time
                )

                Completable.fromAction {
                    db.dailyDao().updateDaily(updatedDaily)
                }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        print("update successfully !!")
                    }, { error ->
                        Toast.makeText(
                            this,
                            "Error updating record: ${error.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    })

                Toast.makeText(this, "Descriptions saved !!", Toast.LENGTH_SHORT).show()
                dialogMonthlyIncome.dismiss()

            } else
                Toast.makeText(this, "Description can't be NULL !!! ❌❌", Toast.LENGTH_SHORT)
                    .show()

        }

    }

    @SuppressLint("CheckResult")
    fun show_alert_add_report() {
        val builderAlert = AlertDialog.Builder(this, R.style.Base_Theme_KarenBalance)
        builderAlert.setTitle("Reports Expense")
        builderAlert.setMessage("Enter your Description :")

        val layoutAlertDialog = LinearLayout(this)
        layoutAlertDialog.orientation = LinearLayout.VERTICAL
        layoutAlertDialog.gravity = Gravity.CENTER
        layoutAlertDialog.setPadding(16, 16, 16, 16)

        val txtPrice = EditText(this)
        txtPrice.setTextColor(ContextCompat.getColor(this, R.color.chocolate_brown))
        txtPrice.hint = "enter Price "
        txtPrice.setBackgroundResource(R.drawable.back_view_border)
        txtPrice.textSize = 25f
        txtPrice.inputType = InputType.TYPE_CLASS_NUMBER

        val editTxtDescription = EditText(this)
        editTxtDescription.setTextColor(ContextCompat.getColor(this, R.color.chocolate_brown))
        editTxtDescription.hint = "enter your Description for expene"
        editTxtDescription.setBackgroundResource(R.drawable.back_view_border)
        editTxtDescription.textSize = 25f
        editTxtDescription.minHeight = 350
        editTxtDescription.maxHeight = 550

        val layoutParamsEdittxt = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParamsEdittxt.setMargins(5, 5, 5, 30) // Add margin to create spacing
        txtPrice.layoutParams = layoutParamsEdittxt
        editTxtDescription.layoutParams = layoutParamsEdittxt

        layoutAlertDialog.addView(txtPrice)
        layoutAlertDialog.addView(editTxtDescription)

        builderAlert.setView(layoutAlertDialog)
        builderAlert.setPositiveButton("Save", null)

        val dialogMonthlyIncome = builderAlert.create()
        dialogMonthlyIncome.setCancelable(false)
        dialogMonthlyIncome.setCanceledOnTouchOutside(false)

        dialogMonthlyIncome.show()

        val positiveButton = dialogMonthlyIncome.getButton(AlertDialog.BUTTON_POSITIVE)
        val layoutParamsButton = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParamsButton.gravity = Gravity.CENTER // Center the button
        layoutParamsButton.setMargins(0, 20, 0, 0) // Add top margin for spacing
        positiveButton.layoutParams = layoutParamsButton
        positiveButton.setTextColor(
            ContextCompat.getColor(
                this,
                R.color.chocolate_brown
            )
        )
        positiveButton.setBackgroundColor(ContextCompat.getColor(this, R.color.caramel))
        positiveButton.textSize = 18f // Increase text size

        positiveButton.setOnClickListener {
            // update descriptions db
            if (editTxtDescription.text.toString() != "NULL" && txtPrice.text.toString().isNotEmpty()) {
                val db = DBHandler.getDatabase(context = this)
                val price = txtPrice.text.toString().toInt()
                val date = persian_date_today
                val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

                val insertDaily = DailyEntity(
                    price = price,
                    description = editTxtDescription.text.toString(),
                    date = date,
                    time = time
                )

                Completable.fromAction {
                    db.dailyDao().insertDaily(insertDaily)
                }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        print("update successfully !!")
                    }, { error ->
                        Toast.makeText(
                            this,
                            "Error updating record: ${error.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    })

                Toast.makeText(this, "Descriptions saved !!", Toast.LENGTH_SHORT).show()
                dialogMonthlyIncome.dismiss()

            } else
                Toast.makeText(this, "Description can't be NULL or empty !!! ❌❌", Toast.LENGTH_SHORT)
                    .show()

        }

    }

    private fun checkSmsPermission() {
        // بررسی اینکه دسترسی READ_SMS داده شده است یا نه
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_SMS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // اگر کاربر دسترسی نداده بود
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.READ_SMS
                )
            ) {
                Toast.makeText(
                    this,
                    "SMS permission is required to use this feature.",
                    Toast.LENGTH_LONG
                ).show()
            }
            // درخواست مجدد دسترسی از کاربر
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_SMS),
                SMS_PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == SMS_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // اگر دسترسی داده شده باشد، هیچ کاری انجام نمی‌شود
            } else {
                // اگر کاربر دسترسی نداد، برنامه بسته می‌شود
                Toast.makeText(
                    this,
                    "To use the Karen Balance app, you must grant permission to access messages.",
                    Toast.LENGTH_LONG
                ).show()
                finish() // بستن Activity
            }
        }
    }

    //    Settings for fragments and activitys............
    override fun attachBaseContext(newBase: Context?) {
        val locale = Locale("en")
        val config = Configuration(newBase?.resources?.configuration)

        Locale.setDefault(locale)
        config.setLocale(locale)

        config.fontScale = 1.0f
        applyOverrideConfiguration(config)

        super.attachBaseContext(newBase?.createConfigurationContext(config))
    }


}
