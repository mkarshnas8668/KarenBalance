package com.mkarshnas6.karenstudio.karenbalance

import ExpensesRecyclerAdapter
import PersianDate
import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.mkarshnas6.karenstudio.karenbalance.databinding.ActivityMoneyBinding
import com.mkarshnas6.karenstudio.karenbalance.db.DBHandler
import com.mkarshnas6.karenstudio.karenbalance.db.model.DailyEntity
import com.mkarshnas6.karenstudio.karenbalance.db.model.ReportEntity
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlin.math.exp

class MoneyFragment : Fragment(R.layout.activity_money), OnExpenseClickListener {

    private lateinit var binding: ActivityMoneyBinding

    private val persian_date_today: String
        get() = PersianDate.getPersianDateToday()

    private var edit_daily_or_report = "daily"

    @SuppressLint("CheckResult")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {


        binding = ActivityMoneyBinding.inflate(inflater)

        // set infromations ...........
        setDataOnRecyclerDaily()

        binding.btnListDailyExpense.setOnClickListener {
            setDataOnRecyclerDaily()
            edit_daily_or_report = "daily"
        }
        binding.btnListMonthlyExpense.setOnClickListener {
            setDataOnRecyclerMonthly()
            edit_daily_or_report = "monthly"
        }

        return binding.root
    }

    @SuppressLint("CheckResult")
    fun setDataOnRecyclerDaily() {
        val db = DBHandler.getDatabase(requireContext())
        val adapter = ExpensesRecyclerAdapter(requireActivity(), this)

        binding.reyclerExpenses.adapter = adapter
        binding.reyclerExpenses.layoutManager = LinearLayoutManager(context)

        db.dailyDao().getDailys
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ expenses ->
                if (expenses.isNotEmpty()) {
                    val latestExpense = expenses[0].date

                    if (latestExpense != persian_date_today) {
                        BroadcastEndDayReceiver.handleEndOfDayTasks(requireContext())
                    }
                }

                if (expenses.isEmpty()) {
                    binding.txtListEmpty.visibility = View.VISIBLE
                } else {
                    binding.txtListEmpty.visibility = View.GONE
                    adapter.setExpenses(expenses)
                }

            }, { error ->
                // مدیریت خطا در صورت بروز مشکل در واکشی داده‌ها
                Toast.makeText(
                    requireContext(),
                    resources.getString(R.string.error_fetching_data,error.message),
                    Toast.LENGTH_LONG
                ).show()
            })
    }

    override fun onExpenseClick(expense: ExpenseEntity) {
        // تبدیل ExpenseEntity به DailyEntity
        if (edit_daily_or_report == "daily") {
            val dailyExpense = expense as? DailyEntity
            show_alert_edit_price_description(dailyExpense, null)
        } else {
            val reportExpense = expense as? ReportEntity
            show_alert_edit_price_description(null, reportExpense)
        }
    }

    @SuppressLint("CheckResult")
    fun show_alert_edit_price_description(
        expense_daily: DailyEntity?,
        expense_report: ReportEntity?
    ) {
        if (edit_daily_or_report == "daily") {
            expense_daily?.let { daily ->
                if (daily.date == persian_date_today) {
                    val builderAlert =
                        AlertDialog.Builder(requireContext(), R.style.Base_Theme_KarenBalance)
                    builderAlert.setTitle(resources.getString(R.string.report_expense))
                    builderAlert.setMessage(resources.getString(R.string.enter_description))

                    val layoutAlertDialog = LinearLayout(context).apply {
                        orientation = LinearLayout.VERTICAL
                        gravity = Gravity.CENTER
                        setPadding(16, 16, 16, 16)
                    }

                    val edittxtPrice = EditText(context).apply {
                        setTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.chocolate_brown
                            )
                        )
                        hint = resources.getString(R.string.enter_price)
                        setBackgroundResource(R.drawable.back_view_border)
                        textSize = 25f
                        inputType = InputType.TYPE_CLASS_NUMBER
                        setText(daily.price.toString().toLong().format_number())
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).apply {
                            setMargins(5, 5, 5, 30)
                        }
                    }

                    edittxtPrice.addTextChangedListener(object : TextWatcher {
                        override fun afterTextChanged(s: Editable?) {
                            val price_edit_txt = edittxtPrice.text.toString()

                            if (price_edit_txt.isNotEmpty()) {
                                val cleanString = price_edit_txt.replace(",", "")
                                try {
                                    val priceLong = cleanString.toLong()
                                    val formattedPrice = String.format("%,d", priceLong)
                                    edittxtPrice.removeTextChangedListener(this)
                                    edittxtPrice.setText(formattedPrice)

                                    edittxtPrice.setSelection(formattedPrice.length)
                                    edittxtPrice.addTextChangedListener(this)
                                } catch (e: NumberFormatException) {
                                    Toast.makeText(
                                        context,
                                        resources.getString(R.string.Enter_valid_num),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }

                        override fun beforeTextChanged(
                            s: CharSequence?,
                            start: Int,
                            count: Int,
                            after: Int
                        ) {
                        }

                        override fun onTextChanged(
                            s: CharSequence?,
                            start: Int,
                            before: Int,
                            count: Int
                        ) {
                        }
                    })

                    val editTxtDescription = EditText(context).apply {
                        setTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.chocolate_brown
                            )
                        )
                        hint = resources.getString(R.string.enter_description)
                        setBackgroundResource(R.drawable.back_view_border)
                        textSize = 25f
                        minHeight = 350
                        maxHeight = 550
                        setText(daily.description)
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).apply {
                            setMargins(5, 5, 5, 30)
                        }
                    }

                    layoutAlertDialog.apply {
                        addView(edittxtPrice)
                        addView(editTxtDescription)
                    }

                    builderAlert.setView(layoutAlertDialog)
                    builderAlert.setPositiveButton(resources.getString(R.string.Save), null)

                    val dialogMonthlyIncome = builderAlert.create().apply {
                        setCancelable(false)
                        setCanceledOnTouchOutside(false)
                    }

                    dialogMonthlyIncome.show()

                    val positiveButton = dialogMonthlyIncome.getButton(AlertDialog.BUTTON_POSITIVE)
                    positiveButton.setOnClickListener {
                        val incomeText = editTxtDescription.text.toString()
                        val priceText = edittxtPrice.text.toString().replace(",", "")
                        if (priceText.isNotEmpty()) {
                            val db = DBHandler.getDatabase(requireContext())

                            val updatedaily = DailyEntity(
                                id = daily.id,
                                price = priceText.toInt(),
                                description = incomeText,
                                date = daily.date,
                                time = daily.time
                            )

                            Completable.fromAction { db.dailyDao().updateDaily(updatedaily) }
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe({
                                    print("Update successful!")
                                }, { error ->
                                    Toast.makeText(
                                        context,
                                        resources.getString(R.string.error_updating_record,error.message),
                                        Toast.LENGTH_LONG
                                    ).show()
                                })

                            Toast.makeText(context, resources.getString(R.string.message_save_description), Toast.LENGTH_SHORT).show()
                            dialogMonthlyIncome.dismiss()
                        } else {
                            Toast.makeText(context, resources.getString(R.string.filed_can_not_empty), Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                }
            }
        } else {
            expense_report?.let { report ->
                val builderAlert =
                    AlertDialog.Builder(requireContext(), R.style.Base_Theme_KarenBalance)
                builderAlert.setTitle(resources.getString(R.string.report_expense))
                builderAlert.setMessage(resources.getString(R.string.enter_description))

                val layoutAlertDialog = LinearLayout(context).apply {
                    orientation = LinearLayout.VERTICAL
                    gravity = Gravity.CENTER
                    setPadding(16, 16, 16, 16)
                }

                val edittxtPrice = EditText(context).apply {
                    setTextColor(ContextCompat.getColor(requireContext(), R.color.chocolate_brown))
                    hint = resources.getString(R.string.enter_price)
                    setBackgroundResource(R.drawable.back_view_border)
                    textSize = 25f
                    setText(report.price.toString())
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        setMargins(5, 5, 5, 30)
                    }
                }

                val editTxtDescription = EditText(context).apply {
                    setTextColor(ContextCompat.getColor(requireContext(), R.color.chocolate_brown))
                    hint = resources.getString(R.string.enter_description)
                    setBackgroundResource(R.drawable.back_view_border)
                    textSize = 25f
                    minHeight = 350
                    maxHeight = 550
                    setText(report.description)
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        setMargins(5, 5, 5, 30)
                    }
                }

                layoutAlertDialog.apply {
                    addView(edittxtPrice)
                    addView(editTxtDescription)
                }

                builderAlert.setView(layoutAlertDialog)
                builderAlert.setPositiveButton(resources.getString(R.string.Save), null)

                val dialogMonthlyIncome = builderAlert.create().apply {
                    setCancelable(false)
                    setCanceledOnTouchOutside(false)
                }

                dialogMonthlyIncome.show()

                val positiveButton = dialogMonthlyIncome.getButton(AlertDialog.BUTTON_POSITIVE)
                positiveButton.setOnClickListener {
                    val incomeText = editTxtDescription.text.toString()
                    val priceText = edittxtPrice.text.toString()
                    if (priceText.isNotEmpty()) {
                        val db = DBHandler.getDatabase(requireContext())
                        val updatedReport = ReportEntity(
                            id = report.id,
                            price = priceText.toInt(),
                            description = incomeText,
                            date = report.date,
                            time = report.time
                        )

                        Completable.fromAction { db.reportDao().updateMonthly(updatedReport) }
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({
                                print("Update successful!")
                            }, { error ->
                                Toast.makeText(
                                    context,
                                    resources.getString(R.string.error_updating_record),
                                    Toast.LENGTH_LONG
                                ).show()
                            })

                        Toast.makeText(context, resources.getString(R.string.message_save_description), Toast.LENGTH_SHORT).show()
                        dialogMonthlyIncome.dismiss()
                    } else {
                        Toast.makeText(context, resources.getString(R.string.filed_can_not_empty), Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        }
    }


// end function alert edit price and description

    @SuppressLint("CheckResult")
    fun setDataOnRecyclerMonthly() {
        val db = DBHandler.getDatabase(requireContext())
        val adapter = ExpensesRecyclerAdapter(requireActivity(), this)
        val allExpenses = mutableListOf<ExpenseEntity>()

        binding.reyclerExpenses.adapter = adapter
        binding.reyclerExpenses.layoutManager = LinearLayoutManager(context)

        db.reportDao().getReports
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ expenses ->
                if (expenses.isEmpty()) {
                    binding.txtListEmpty.visibility = View.VISIBLE
                } else {
                    binding.txtListEmpty.visibility = View.GONE
                    allExpenses.addAll(expenses)
                    adapter.setExpenses(allExpenses)
                }
            }, { error ->
                Toast.makeText(
                    requireContext(),
                    resources.getString(R.string.error_fetching_data,error.message),
                    Toast.LENGTH_LONG
                ).show()
            })
    }
}
