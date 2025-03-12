package com.mkarshnas6.karenstudio.karenbalance

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.mkarshnas6.karenstudio.karenbalance.databinding.ActivityMoneyBinding
import com.mkarshnas6.karenstudio.karenbalance.databinding.ActivityTargetBinding

class TargetFragment : Fragment(R.layout.activity_target) {

    private lateinit var binding : ActivityTargetBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ActivityTargetBinding.inflate(inflater)


        return binding.root
    }

}