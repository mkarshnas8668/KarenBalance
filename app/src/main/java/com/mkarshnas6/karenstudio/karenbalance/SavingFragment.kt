package com.mkarshnas6.karenstudio.karenbalance

import android.annotation.SuppressLint
import android.graphics.PorterDuff
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import app.rive.runtime.kotlin.core.Rive
import com.mkarshnas6.karenstudio.karenbalance.databinding.ActivitySavingBinding


class SavingFragment : Fragment(R.layout.activity_saving) {

    private lateinit var binding: ActivitySavingBinding
//    @OptIn(ExperimentalAssetLoader::class)
//    private lateinit var animation_daily: RiveAnimationView

    //    @OptIn(ExperimentalAssetLoader::class)
    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ActivitySavingBinding.inflate(inflater)

        Rive.init(requireContext())


//        show animation write save today and monthly ............
        AnimationWrite.showAnimTitle(binding.txtSaveDaily, "Amount saved today")
        AnimationWrite.showAnimTitle(binding.txtSaveMonthly, "Amount saved this month")

//      ...........set value state matchins ........................
        Handler(Looper.getMainLooper()).postDelayed({
            binding.progressBarDaily.visibility = View.INVISIBLE
            binding.progressBarMontly.visibility = View.INVISIBLE

            binding.animSavingDaily.visibility = View.VISIBLE
            binding.animSavingMonthly.visibility = View.VISIBLE

            binding.animSavingDaily.setNumberState("anim saving", "save", 100f)
            binding.animSavingMonthly.setNumberState("anim saving", "save", 10f)
        }, 1000)

        return binding.root
    }

}