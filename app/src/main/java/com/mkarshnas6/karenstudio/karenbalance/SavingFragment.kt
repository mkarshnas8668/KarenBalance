package com.mkarshnas6.karenstudio.karenbalance

import android.annotation.SuppressLint
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.ShapeDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import app.rive.runtime.kotlin.core.Direction
import app.rive.runtime.kotlin.core.Loop
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

        binding.txtSaveDaily.visibility = View.INVISIBLE

//        show animation write save today and monthly ............
        AnimationWrite.showAnimTitle(binding.txtSaveDaily, "Amount saved today")
        AnimationWrite.showAnimTitle(binding.txtSaveMonthly, "Amount saved this month")

//      ...........set value state matchins ........................
        Handler(Looper.getMainLooper()).postDelayed({
            binding.animSavingDaily.setNumberState("anim saving", "save", 100f)
            binding.animSavingMonthly.setNumberState("anim saving", "save", 10f)
        }, 3000)

        return binding.root
    }

}