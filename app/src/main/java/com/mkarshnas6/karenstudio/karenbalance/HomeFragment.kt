package com.mkarshnas6.karenstudio.karenbalance

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity.MODE_PRIVATE
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.mkarshnas6.karenstudio.karenbalance.databinding.ActivityHomeBinding
import com.mkarshnas6.karenstudio.karenbalance.db.DBHandler
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers

class HomeFragment : Fragment(R.layout.activity_home) {

    private lateinit var binding: ActivityHomeBinding
    lateinit var pieChart_daily: PieChart
    lateinit var pieChart_monthly: PieChart

    private var totalExpenseToday = 0
    private var totalExpenseMonthly = 0
    private var dailySpendingLimit = 0

    //    val disposable = CompositeDisposable()
    lateinit var pref: SharedPreferences

    @SuppressLint("CheckResult", "SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ActivityHomeBinding.inflate(inflater)
//      set view 3/4 of screen to show mony ..
        val screenWidth = resources.displayMetrics.widthPixels
        val minWidth = (screenWidth * 3) / 4
        binding.txtShowMonthlyIncome.minWidth = minWidth
        // end show screen to screen .........
        // get the shared preferences .........
        pref = requireContext().getSharedPreferences("Prefs_KarenBalance", MODE_PRIVATE)
//      ...........  Set Name Bank from shared pref ...........................
        binding.txtShowNameBank.text = pref.getString("name_bank", "Error to get name bank !!")

//        get monthly income .........
        val monthly_income = pref.getLong("monthly_income", 1111111111)
        dailySpendingLimit = (monthly_income / 31).toInt()

//        get is the first one open the app or not ? .......
        var is_first_run = pref.getBoolean("isFirstRun", true)

        if (is_first_run) {
//            show_alert_bank_name("btn_create")
            show_alert_bank_name("btn_create")
            show_alert_montyle_income("btn_create")

            val editor = pref.edit()
            editor.putBoolean("isFirstRun", false)
            editor.apply()
        }

        binding.btnEditMonthlyIncome.setOnClickListener { show_alert_montyle_income("btn_edit") }
        binding.btnEditNameBank.setOnClickListener { show_alert_bank_name("btn_edit") }

//        get all informations daily ..............
        val db = DBHandler.getDatabase(requireContext())
        db.dailyDao().getDailys
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ expenses ->

                totalExpenseToday = expenses.sumOf { it.price }

                pieChart_daily = binding.pieChartDaily
                pieChart_daily.setUsePercentValues(true)
                pieChart_daily.getDescription().setEnabled(false)
                pieChart_daily.setExtraOffsets(10f, 5f, 10f, 5f)
                pieChart_daily.setDragDecelerationFrictionCoef(0.95f)
                pieChart_daily.setDrawHoleEnabled(false)
                pieChart_daily.setTransparentCircleColor(Color.WHITE)
                pieChart_daily.setRotationAngle(-90f)
                pieChart_daily.setRotationEnabled(true)
                pieChart_daily.setHighlightPerTapEnabled(true)
                pieChart_daily.animateY(1500, Easing.EaseInOutCirc)
//        set color and font for chart ...........................
                pieChart_daily.setEntryLabelColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.chocolate_brown
                    )
                )
                pieChart_daily.setEntryLabelTypeface(
                    ResourcesCompat.getFont(
                        requireContext(),
                        R.font.pacifico_egular
                    )
                )
                pieChart_daily.setEntryLabelTextSize(20f)


                val entries_daily: ArrayList<PieEntry> = ArrayList()
                val chart_colors_daily: ArrayList<Int> = ArrayList()


                val totalExpenseTodayplus = kotlin.math.abs(totalExpenseToday)
                val totalExpenseTodayPercent = (totalExpenseTodayplus / dailySpendingLimit.toFloat()) * 100

                val remaining_today = dailySpendingLimit - totalExpenseTodayplus
                if (totalExpenseTodayplus > dailySpendingLimit) {
                    binding.txtDailySpentIncome.text =
                        "Daily Spent Income - Unspent: \n 0 - over limited !!"
                    binding.txtDailySpentIncome.setTextColor(requireContext().getColor(R.color.red))
                } else {
                    binding.txtDailySpentIncome.text = "Daily Spent Income - Unspent: \n ${
                        remaining_today.toLong().format_number()
                    }"
                    binding.txtDailySpentIncome.setTextColor(requireContext().getColor(R.color.chocolate_brown))
                }

                if (totalExpenseTodayplus <= dailySpendingLimit.toInt()) {
                    entries_daily.add(PieEntry(totalExpenseTodayPercent, "used"))
                    entries_daily.add(PieEntry(100f - totalExpenseTodayPercent, "save"))
                    chart_colors_daily.add(ContextCompat.getColor(requireContext(), R.color.blue))
                    chart_colors_daily.add(ContextCompat.getColor(requireContext(), R.color.green_200))
                } else if (totalExpenseTodayplus > dailySpendingLimit) {
                    entries_daily.add(PieEntry(100f, "over limit"))
                    chart_colors_daily.add(ContextCompat.getColor(requireContext(), R.color.blue))
                } else {
                    entries_daily.add(PieEntry(dailySpendingLimit.toFloat(), "save"))
                    chart_colors_daily.add(ContextCompat.getColor(requireContext(), R.color.green_200))
                }

                val dataSet_daily = PieDataSet(entries_daily, "")

                dataSet_daily.colors = chart_colors_daily

                dataSet_daily.setDrawIcons(false)

                dataSet_daily.sliceSpace = 5f
                dataSet_daily.selectionShift = 6f


                val chart_data_daily = PieData(dataSet_daily)
                chart_data_daily.setValueFormatter(PercentFormatter())
                chart_data_daily.setValueTextSize(20f)
                // set color and font ..........................
                chart_data_daily.setValueTypeface(
                    ResourcesCompat.getFont(
                        requireContext(),
                        R.font.pacifico_egular
                    )
                )
                chart_data_daily.setValueTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.chocolate_brown
                    )
                )
                // end set ............................

// ..... chage helper (legend) chart ...............
                pieChart_daily.legend.isEnabled = false
//        end helper ....................

                pieChart_daily.setData(chart_data_daily)

                pieChart_daily.highlightValues(null)

                pieChart_daily.invalidate()
// ....   end work for config Pie chart daily ................................

            }, { error ->
                Toast.makeText(
                    requireContext(),
                    "Error fetching data: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            })


        //        get information from monthly .
        db.reportDao().getReports
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ expenses ->

                totalExpenseMonthly = expenses.sumOf { it.price }

                //  ............   config for Pie chart monthly  .......................
                pieChart_monthly = binding.pieChartMonthly
                pieChart_monthly.setUsePercentValues(true)
                pieChart_monthly.getDescription().setEnabled(false)
                pieChart_monthly.setExtraOffsets(10f, 5f, 10f, 5f)
                pieChart_monthly.setDragDecelerationFrictionCoef(0.95f)
                pieChart_monthly.setDrawHoleEnabled(false)
                pieChart_monthly.setTransparentCircleColor(Color.WHITE)
                pieChart_monthly.setRotationAngle(-90f)
                pieChart_monthly.setRotationEnabled(true)
                pieChart_monthly.setHighlightPerTapEnabled(true)
                pieChart_monthly.animateY(1500, Easing.EaseInOutCirc)
//        set color and font for chart ...........................
                pieChart_monthly.setEntryLabelColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.chocolate_brown
                    )
                )
                pieChart_monthly.setEntryLabelTypeface(
                    ResourcesCompat.getFont(
                        requireContext(),
                        R.font.pacifico_egular
                    )
                )
                pieChart_monthly.setEntryLabelTextSize(20f)


                val entries_monthly: ArrayList<PieEntry> = ArrayList()
                val MonthlySpendingLimit = pref.getLong("monthly_income", 1111111111)
                val chart_colors_monthly: ArrayList<Int> = ArrayList()

                val totalExpenseMonthlyplus = kotlin.math.abs(totalExpenseMonthly)
                val totalExpenseMonthlyPercent = (totalExpenseMonthlyplus / MonthlySpendingLimit.toFloat()) * 100

                if (totalExpenseMonthlyplus <= MonthlySpendingLimit.toInt()) {
                    entries_monthly.add(PieEntry(totalExpenseMonthlyPercent, "used"))
                    entries_monthly.add(PieEntry(100f - totalExpenseMonthlyPercent, "save"))
                    chart_colors_monthly.add(ContextCompat.getColor(requireContext(), R.color.blue))
                    chart_colors_monthly.add(ContextCompat.getColor(requireContext(), R.color.green_200))
                } else if (totalExpenseMonthlyplus > MonthlySpendingLimit) {
                    entries_monthly.add(PieEntry(100f, "over limit"))
                    chart_colors_monthly.add(ContextCompat.getColor(requireContext(), R.color.blue))
                } else {
                    entries_monthly.add(PieEntry(MonthlySpendingLimit.toFloat(), "save"))
                    chart_colors_monthly.add(ContextCompat.getColor(requireContext(), R.color.green_200))
                }

                val remaining_monthly = MonthlySpendingLimit - totalExpenseMonthlyplus
                if (totalExpenseMonthlyplus > MonthlySpendingLimit) {
                    binding.txtMonthlySpentIncome.text =
                        "Daily Spent Income - Unspent: \n 0 - over limited !!"
                    binding.txtMonthlySpentIncome.setTextColor(requireContext().getColor(R.color.red))
                } else {
                    binding.txtMonthlySpentIncome.text = "Daily Spent Income - Unspent: \n ${
                        remaining_monthly.format_number()
                    }"
                    binding.txtMonthlySpentIncome.setTextColor(requireContext().getColor(R.color.chocolate_brown))
                }


                val dataSet_monthly = PieDataSet(entries_monthly, "")

                dataSet_monthly.setDrawIcons(false)

                dataSet_monthly.sliceSpace = 5f
                dataSet_monthly.selectionShift = 6f

                dataSet_monthly.colors = chart_colors_monthly

                val chart_data_monthly = PieData(dataSet_monthly)
                chart_data_monthly.setValueFormatter(PercentFormatter())
                chart_data_monthly.setValueTextSize(20f)
                // set color and font ..........................
                chart_data_monthly.setValueTypeface(
                    ResourcesCompat.getFont(
                        requireContext(),
                        R.font.pacifico_egular
                    )
                )
                chart_data_monthly.setValueTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.chocolate_brown
                    )
                )
                // end set ............................

// ..... chage helper (legend) chart ...............
                pieChart_monthly.legend.isEnabled = false
//        end helper ....................

                pieChart_monthly.setData(chart_data_monthly)

                pieChart_monthly.highlightValues(null)

                pieChart_monthly.invalidate()

//   ........... end config PIechart monthly .........................

            }, { error ->
                Toast.makeText(
                    requireContext(),
                    "Error fetching data: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            })


//        end get all money today ................

        binding.txtShowMonthlyIncome.setOnClickListener {

            binding.txtShowMonthlyIncome.text = monthly_income.toString().format_number()


        }

//  ............   config for Pie chart Daily  .......................

//..................................................................................................
        return binding.root
    }

    fun show_alert_bank_name(btn_clicked: String) {
        val builderAlert = AlertDialog.Builder(requireContext(), R.style.Base_Theme_KarenBalance)
        builderAlert.setTitle("Bank Name")
        builderAlert.setMessage("Enter the name of the bank where your income will be deposited :")

        val layoutAlertDialog = LinearLayout(requireContext())
        layoutAlertDialog.orientation = LinearLayout.VERTICAL
        layoutAlertDialog.gravity = Gravity.CENTER
        layoutAlertDialog.setPadding(16, 16, 16, 16)

        val editTxtMonthlyIncome = EditText(requireContext())
        editTxtMonthlyIncome.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.chocolate_brown
            )
        )
        editTxtMonthlyIncome.hint = "Alike : +9891********* | ResalatBank"
        editTxtMonthlyIncome.setBackgroundResource(R.drawable.back_view_border)
        editTxtMonthlyIncome.filters = arrayOf(InputFilter.LengthFilter(19))
        editTxtMonthlyIncome.textSize = 25f
        editTxtMonthlyIncome.inputType = InputType.TYPE_CLASS_TEXT

        if (btn_clicked == "btn_edit")
            editTxtMonthlyIncome.setText(
                pref.getString("name_bank", "Error to get name bank !!").toString()
            )

        val layoutParamsEdittxt = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParamsEdittxt.setMargins(5, 5, 5, 30) // Add margin to create spacing
        editTxtMonthlyIncome.layoutParams = layoutParamsEdittxt

        layoutAlertDialog.addView(editTxtMonthlyIncome)

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
                requireContext(),
                R.color.chocolate_brown
            )
        )
        positiveButton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.caramel))
        positiveButton.textSize = 18f // Increase text size

        positiveButton.setOnClickListener {
            val name_bank_text = editTxtMonthlyIncome.text.toString()
            if (name_bank_text.isNotEmpty()) {

                pref.edit().putString("name_bank", name_bank_text).apply()
                Toast.makeText(
                    requireContext(),
                    "Name bank : $name_bank_text saved!",
                    Toast.LENGTH_SHORT
                ).show()
                dialogMonthlyIncome.dismiss()
                binding.txtShowNameBank.text = name_bank_text
            } else {
                Toast.makeText(requireContext(), "Field cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }

    }

    fun show_alert_montyle_income(btn_clicked: String) {
        val builderAlert = AlertDialog.Builder(requireContext(), R.style.Base_Theme_KarenBalance)
        builderAlert.setTitle("Monthly Income")
        builderAlert.setMessage("Enter your monthly Income:")

        val layoutAlertDialog = LinearLayout(requireContext())
        layoutAlertDialog.orientation = LinearLayout.VERTICAL
        layoutAlertDialog.gravity = Gravity.CENTER
        layoutAlertDialog.setPadding(16, 16, 16, 16)

        val editTxtMonthlyIncome = EditText(requireContext())
        editTxtMonthlyIncome.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.chocolate_brown
            )
        )
        editTxtMonthlyIncome.hint = "Enter Monthly Income"
        editTxtMonthlyIncome.setBackgroundResource(R.drawable.back_view_border)
        editTxtMonthlyIncome.filters = arrayOf(InputFilter.LengthFilter(19))
        editTxtMonthlyIncome.textSize = 25f
        editTxtMonthlyIncome.inputType = InputType.TYPE_CLASS_NUMBER

        if (btn_clicked == "btn_edit")
            editTxtMonthlyIncome.setText(
                pref.getLong("monthly_income", 1111111111111111111).toString()
            )

        val layoutParamsEdittxt = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParamsEdittxt.setMargins(5, 5, 5, 30) // Add margin to create spacing
        editTxtMonthlyIncome.layoutParams = layoutParamsEdittxt

        layoutAlertDialog.addView(editTxtMonthlyIncome)

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
                requireContext(),
                R.color.chocolate_brown
            )
        )
        positiveButton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.caramel))
        positiveButton.textSize = 18f // Increase text size

        positiveButton.setOnClickListener {
            val incomeText = editTxtMonthlyIncome.text.toString()
            if (incomeText.isNotEmpty()) {

                pref.edit().putLong("monthly_income", incomeText.toLong()).apply()
                pref.edit().putBoolean("isFirstRun", false).apply()
                Toast.makeText(requireContext(), "Monthly income saved!", Toast.LENGTH_SHORT).show()
                dialogMonthlyIncome.dismiss()
                binding.txtShowMonthlyIncome.text = incomeText.format_number()

            } else {
                Toast.makeText(requireContext(), "Field cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }

    }

    override fun onStart() {
        super.onStart()

        AnimationWrite.showAnimTitle(binding.txtTitleHome, "Home Page")
        AnimationWrite.showAnimTitle(
            binding.txtShowMonthlyIncome,
            binding.txtShowMonthlyIncome.text.toString()
        )
        val intent = Intent(requireContext(), SmsReceiverService::class.java)
        requireContext().startService(intent)

    }


}