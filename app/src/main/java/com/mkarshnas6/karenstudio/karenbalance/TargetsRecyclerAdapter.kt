package com.mkarshnas6.karenstudio.karenbalance

import PersianDate
import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.mkarshnas6.karenstudio.karenbalance.databinding.ListItemTargetsBinding
import com.mkarshnas6.karenstudio.karenbalance.db.model.TargetEntity
import java.io.File

class TargetsRecyclerAdapter(
    private val context: Activity,
    private val listener: TargetFragment
) : RecyclerView.Adapter<TargetsRecyclerAdapter.TargetsViewHolder>() {

    private val TargetsList = mutableListOf<TargetEntity>()
    private val PersianDateToday = PersianDate.getCurrentShamsiDate()

    inner class TargetsViewHolder(
        private val binding: ListItemTargetsBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(target: TargetEntity) {
            binding.txtNameTarget.text = target.name
            binding.txtDescriptionTarget.text =
                "${context.getString(R.string.you_have)} 10,000 ${context.getString(R.string.out_of)} ${target.price}"
            binding.txtDateTarget.text = target.date
            binding.txtShowProgress.text = "${target.progress} %"
            binding.progressBarTarget.progress = target.progress

//            // Set image from gallery URI
            val imgPath = target.img

            if (imgPath.startsWith("content://")) {
                // اگر تصویر از گالری انتخاب شده باشد
                try {
                    val inputStream = context.contentResolver.openInputStream(Uri.parse(imgPath))
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    binding.imgTarget.setImageBitmap(bitmap)
                } catch (e: Exception) {
                    e.printStackTrace()
                    binding.imgTarget.setImageResource(R.drawable.luxe_home) // عکس پیش‌فرض
                }
            } else {
                // اگر تصویر در حافظه داخلی ذخیره شده باشد
                val file = File(imgPath)
                if (file.exists()) {
                    binding.imgTarget.setImageURI(Uri.fromFile(file))
                } else {
                    binding.imgTarget.setImageResource(R.drawable.luxe_home) // عکس پیش‌فرض
                }
            }



            if (target.necessary) {
                binding.txtTypeTarget.setTextColor(context.getColor(R.color.green_200))
                binding.txtTypeTarget.text = "Necessary"
            } else {
                binding.txtTypeTarget.setTextColor(context.getColor(R.color.oring))
                binding.txtTypeTarget.text = "Unnecessary"
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TargetsViewHolder {
        val binding = ListItemTargetsBinding.inflate(
            LayoutInflater.from(context),
            parent,
            false
        )
        return TargetsViewHolder(binding)
    }

    override fun getItemCount(): Int = TargetsList.size

    override fun onBindViewHolder(holder: TargetsViewHolder, position: Int) {
        holder.bind(TargetsList[position])
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setTargets(targets: List<TargetEntity>) {
        TargetsList.clear()
        TargetsList.addAll(targets)
        notifyDataSetChanged()
    }
}
