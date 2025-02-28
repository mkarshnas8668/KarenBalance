package com.mkarshnas6.karenstudio.karenbalance

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.mkarshnas6.karenstudio.karenbalance.databinding.ActivitySavingBinding

class SavingFragment : Fragment(R.layout.activity_saving) {

    private lateinit var binding: ActivitySavingBinding
//    @OptIn(ExperimentalAssetLoader::class)
//    private lateinit var animation_daily: RiveAnimationView

    //    @OptIn(ExperimentalAssetLoader::class)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ActivitySavingBinding.inflate(inflater)

//        Rive.init(requireContext())
//      set animation daily .....................
//        animation_daily = binding.animationDailySaving
//        animation_daily.setRiveResource(
//            resId = R.raw.state_machine_saving,
//            stateMachineName = "State Machine 1" // نام دقیق state machine را وارد کنید
//        )
//
//        animation_daily.setNumberState("State Machine 1", "save", 10.0f) // نام trigger را جایگزین کنید

        return binding.root
    }

}