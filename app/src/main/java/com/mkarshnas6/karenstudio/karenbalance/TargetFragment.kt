package com.mkarshnas6.karenstudio.karenbalance

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.adivery.sdk.Adivery
import com.adivery.sdk.AdiveryAdListener
import com.mkarshnas6.karenstudio.karenbalance.databinding.ActivityTargetBinding
import com.mkarshnas6.karenstudio.karenbalance.db.DBHandler
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers

class TargetFragment : Fragment(R.layout.activity_target) {

    private lateinit var binding : ActivityTargetBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ActivityTargetBinding.inflate(inflater)

        //        start show ads

        Adivery.configure(requireActivity().application, "1c0fbf6f-8ccb-4b3f-bc06-72351eee6547")
        val bannerAd_bottom = binding.bannerAddTarget

        //        banner bottom
        bannerAd_bottom.setBannerAdListener(object : AdiveryAdListener() {
            override fun onError(reason: String) {
                Log.e("adivary", "${reason}")
            }

            override fun onAdClicked() {
                Toast.makeText(
                    context,
                    "خیلی ممنون که کلیک کردی ❤ :)",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })

        bannerAd_bottom.loadAd()

//        end shwo ADs

        setDataOnRecyclerTarget()

        return binding.root
    }

    @SuppressLint("CheckResult")
    fun setDataOnRecyclerTarget() {
        val db = DBHandler.getDatabase(requireContext())
        val adapter = TargetsRecyclerAdapter(requireActivity(), this)

        binding.reyclerTargets.adapter = adapter
        binding.reyclerTargets.layoutManager = LinearLayoutManager(context)

        db.targetDao().getTargets
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ targets ->

                if (targets.isEmpty()) {
                    binding.txtListEmptyTarget.visibility = View.VISIBLE
                } else {
                    binding.txtListEmptyTarget.visibility = View.GONE
                    adapter.setTargets(targets)
                }

            }, { error ->
                Toast.makeText(
                    requireContext(),
                    resources.getString(R.string.error_fetching_data, error.message),
                    Toast.LENGTH_LONG
                ).show()
            })
    }

}