package com.mkarshnas6.karenstudio.karenbalance

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.mkarshnas6.karenstudio.karenbalance.databinding.ActivityChartBinding
import com.mkarshnas6.karenstudio.karenbalance.db.DBHandler
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlin.math.absoluteValue

class ChartFragment : Fragment(R.layout.activity_chart) {

    private lateinit var binding: ActivityChartBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ActivityChartBinding.inflate(inflater, container, false)

        setupChart()

        return binding.root
    }

    @SuppressLint("CheckResult")
    private fun setupChart() {
        val reportDao = DBHandler.getDatabase(requireContext()).reportDao()

        reportDao.getReports
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe({ dailyExpenses ->
                val groupedExpenses = dailyExpenses.groupBy { it.date.substring(5) }
                    .mapValues { entry -> entry.value.sumOf { it.price.absoluteValue } }

                val dateLabels = groupedExpenses.keys.toList()

                val entries = groupedExpenses.entries.mapIndexed { index, (_, totalPrice) ->
                    Entry(index.toFloat(), totalPrice.toFloat())
                }

                val dataSet = LineDataSet(entries, resources.getString(R.string.title_chart_expences)).apply {
                    color = Color.RED
                    valueTextColor = Color.BLACK
                    setCircleColor(Color.RED)
                    setDrawCircleHole(false)
                    setDrawValues(true)
                    setValueTextSize(12f)
                }

                binding.chart.apply {
                    data = LineData(dataSet)
                    description.isEnabled = false

                    // set date on X
                    xAxis.apply {
                        valueFormatter = IndexAxisValueFormatter(dateLabels)
                        position = XAxis.XAxisPosition.TOP
                        granularity = 1f
                        textSize = 12f
                        textColor = Color.BLACK
                    }

                    legend.apply {
                        textColor = Color.BLACK
                        textSize = 13f
                        verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
                        horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
                    }
                    animateXY(500, 1500)
                    invalidate()
                }
            }, { error ->
                error.printStackTrace()
            })
    }



    override fun onStart() {
        super.onStart()

        AnimationWrite.showAnimTitle(
            binding.txtTitleCharts,
            getString(R.string.average_daily_spending_amount_on_the_chart)
        )

    }


}