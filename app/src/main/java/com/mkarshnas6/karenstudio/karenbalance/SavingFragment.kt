package com.mkarshnas6.karenstudio.karenbalance

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity.MODE_PRIVATE
import androidx.fragment.app.Fragment
import app.rive.runtime.kotlin.core.Rive
import com.mkarshnas6.karenstudio.karenbalance.databinding.ActivitySavingBinding
import com.mkarshnas6.karenstudio.karenbalance.db.DBHandler
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers

class SavingFragment : Fragment(R.layout.activity_saving) {

    private lateinit var binding: ActivitySavingBinding
    private var totalExpenseToday = 0
    private var totalExpenseMonthly = 0
    private var dailySpendingLimit = 0
    lateinit var pref: SharedPreferences

    @SuppressLint("UseCompatLoadingForDrawables", "CheckResult", "SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ActivitySavingBinding.inflate(inflater)
        Rive.init(requireContext())

        pref = requireContext().getSharedPreferences("Prefs_KarenBalance", MODE_PRIVATE)
        val monthly_income = pref.getLong("monthly_income", 1111111111)
        dailySpendingLimit = (monthly_income / 31).toInt()

//        show limie prescent daily .......................
        val db = DBHandler.getDatabase(requireContext())
        db.dailyDao().getDailys
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ expenses ->

                totalExpenseToday = expenses.sumOf { it.price }
                val totalExpenseTodayPercent = (totalExpenseToday.toFloat() / dailySpendingLimit) * 100

//                check if get up of limit dont show mines
                if (100-totalExpenseTodayPercent.toInt() >= 0)
                    binding.txtSaveDaily.text = "${getString(R.string.amount_saved_today)} :\n ${100-totalExpenseTodayPercent.toInt()}%"
                else
                    binding.txtSaveDaily.text = "${getString(R.string.amount_saved_today)} :\n 0%"

                binding.animSavingDaily.setNumberState("anim saving", "expen", totalExpenseTodayPercent)
                AnimationWrite.showAnimTitle(binding.txtSaveDaily, binding.txtSaveDaily.text.toString())

            }, { error -> error.printStackTrace() })

        //        show limie prescent monthly .......................
        db.reportDao().getReports
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ expenses ->

                totalExpenseMonthly = expenses.sumOf { it.price }
                val totalExpenseMonthlyPercent = (totalExpenseMonthly.toFloat() / monthly_income) * 100

                if (100-totalExpenseMonthlyPercent.toInt() >= 0)
                    binding.txtSaveMonthly.text = "${getString(R.string.amount_saved_this_month)} :\n ${100 - totalExpenseMonthlyPercent.toInt()}%"
                else
                    binding.txtSaveMonthly.text = "${getString(R.string.amount_saved_this_month)} :\n 0%"

                binding.animSavingMonthly.setNumberState("anim saving", "expen", totalExpenseMonthlyPercent)

                AnimationWrite.showAnimTitle(binding.txtSaveMonthly, binding.txtSaveMonthly.text.toString())

            }, { error -> error.printStackTrace() })


        Handler(Looper.getMainLooper()).postDelayed({
            binding.progressBarDaily.visibility = View.GONE
            binding.progressBarMonthly.visibility = View.GONE
            binding.animSavingDaily.visibility = View.VISIBLE
            binding.animSavingMonthly.visibility = View.VISIBLE
        }, 1000)

        return binding.root
    }
}
