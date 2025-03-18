package com.mkarshnas6.karenstudio.karenbalance

import PersianDate
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity.MODE_PRIVATE
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputLayout
import com.mkarshnas6.karenstudio.karenbalance.databinding.ListItemTargetsBinding
import com.mkarshnas6.karenstudio.karenbalance.db.DBHandler
import com.mkarshnas6.karenstudio.karenbalance.db.model.TargetEntity
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class TargetsRecyclerAdapter(
    private val context: Activity,
    private val listener: TargetFragment
) : RecyclerView.Adapter<TargetsRecyclerAdapter.TargetsViewHolder>() {

    private val TargetsList = mutableListOf<TargetEntity>()
    private val PersianDateToday = PersianDate.getCurrentShamsiDate()
    private val persian_date_today: String
        get() = PersianDate.getPersianDateToday()
    private var selectedImageUri: Uri? = null
    private var image_alert: ImageView? = null
    lateinit var pref: SharedPreferences
    private var dailySpendingLimit = 0
    private var totalExpenseMonthly = 0

    inner class TargetsViewHolder(
        private val binding: ListItemTargetsBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n", "DefaultLocale")
        fun bind(target: TargetEntity) {
            pref = context.getSharedPreferences("Prefs_KarenBalance", MODE_PRIVATE)
            //        get monthly income .........

            val monthly_income = pref.getLong("monthly_income", 1111111111)
            dailySpendingLimit = (monthly_income / 31).toInt()
            val saving_income = pref.getLong("saving_income",0)
            val main_target = (pref.getString("main_target","0"))?.toInt()

            val percentage = if (target.price > 0) {
                (saving_income.toFloat() / target.price.toFloat()) * 100
            } else { 0f }

            if (target.id != main_target) {
                binding.txtNameTarget.setTextColor(context.getColor(R.color.white))
                binding.txtNameTarget.text = target.name
            }else{
                binding.txtNameTarget.setTextColor(context.getColor(R.color.green_200))
                binding.txtNameTarget.text = "${ target.name } ( Main )"
            }

            binding.txtDateTarget.text = target.date
            if (percentage > 0) {
                if (percentage <= 100)
                    binding.txtShowProgress.text = String.format("%.2f %%", percentage)
                else
                    binding.txtShowProgress.text = "100 %"

                binding.txtDescriptionTarget.setTextColor(context.getColor(R.color.white))
                if(saving_income >= target.price){
                    binding.txtDescriptionTarget.text = "${context.getString(R.string.you_have)} ${target.price.toLong().format_number()} ${context.getString(R.string.out_of)} ${target.price.toLong().format_number()}"
                    binding.txtDescriptionTarget.setTextColor(context.getColor(R.color.green_200))
                }else{
                    binding.txtDescriptionTarget.text = "${context.getString(R.string.you_have)} ${saving_income.toLong().format_number()} ${context.getString(R.string.out_of)} ${target.price.toLong().format_number()}"
                    binding.txtDescriptionTarget.setTextColor(context.getColor(R.color.white))
                }
                binding.progressBarTarget.progress = percentage.toInt()
            }else{
                binding.txtShowProgress.text = "0 %"
                binding.txtDescriptionTarget.setTextColor(context.getColor(R.color.oring))
                binding.txtDescriptionTarget.text = "${context.getString(R.string.you_have)} 0 ${context.getString(R.string.out_of)} ${target.price.toLong().format_number()}"
                binding.progressBarTarget.progress = 0

            }

            val imgPath = target.img



            if (imgPath.startsWith("content://")) {
                try {
                    val inputStream = context.contentResolver.openInputStream(Uri.parse(imgPath))
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    binding.imgTarget.setImageBitmap(bitmap)
                } catch (e: Exception) {
                    e.printStackTrace()
                    binding.imgTarget.setImageResource(R.drawable.luxe_home)
                }
            } else {
                val file = File(imgPath)
                if (file.exists()) {
                    binding.imgTarget.setImageURI(Uri.fromFile(file))
                } else {
                    binding.imgTarget.setImageResource(R.drawable.luxe_home)
                }
            }



            if (target.necessary) {
                binding.txtTypeTarget.setTextColor(context.getColor(R.color.green_200))
                binding.txtTypeTarget.text = "Necessary"
            } else {
                binding.txtTypeTarget.setTextColor(context.getColor(R.color.oring))
                binding.txtTypeTarget.text = "Unnecessary"
            }

            binding.root.setOnClickListener {
                showPopupMenu(it, target)
                true
            }

        }
    }

    private fun showPopupMenu(view: View, target: TargetEntity) {
        val wrapper = ContextThemeWrapper(context, R.style.CustomPopupMenu)
        val popupMenu = PopupMenu(wrapper, view)
        popupMenu.menuInflater.inflate(R.menu.menu_target_options, popupMenu.menu)


        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_main_target -> {
                    onChangeMainTarget(context,target)
                    true
                }

                R.id.action_edit -> {
                    show_alert_update_target(target)
                    true
                }

                R.id.action_delete -> {
                    onDeleteTarget(target)
                    true
                }

                else -> false
            }
        }

        popupMenu.show()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun onChangeMainTarget(context: Context, target: TargetEntity) {
        AlertDialog.Builder(context)
            .setTitle("Change Main Target")
            .setMessage("Are you sure you want to set this as your main target?")
            .setPositiveButton("OK") { _, _ ->
                pref.edit().putString("main_target", target.id.toString()).apply()

                notifyDataSetChanged()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }


    fun onDeleteTarget(target: TargetEntity) {
        AlertDialog.Builder(context)
            .setTitle("Delete Target")
            .setMessage("Are you sure you want to delete ${target.name}?")
            .setPositiveButton("Delete") { _, _ ->
                val db = DBHandler.getDatabase(context)
                Completable.fromAction {
                    db.targetDao().deleteTargetById(target.id)
                }.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnTerminate {
                        Toast.makeText(context, "Deleted successfully", Toast.LENGTH_SHORT).show()
                    }
                    .subscribe()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

//    ............. for update the Targets in recycler adapter ................................

    @SuppressLint("CheckResult")
    fun show_alert_update_target(target: TargetEntity) {
        val builderAlert = AlertDialog.Builder(context, R.style.Base_Theme_KarenBalance)

        val titleLayout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.START
            setPadding(32, 32, 32, 32)
        }

        val titleTextView = TextView(context).apply {
            text = "Update Target"
            textSize = 20f
            setTextColor(ContextCompat.getColor(context, R.color.chocolate_brown))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 20
            }
        }
        titleLayout.addView(titleTextView)

        val layoutAlertDialog = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(32, 32, 32, 32)
        }

        val nameTextInputLayout = TextInputLayout(context).apply {
            hint = "Enter name"
            setPadding(10, 10, 10, 10)
        }
        val nameEditText = EditText(context).apply {
            setTextColor(ContextCompat.getColor(context, R.color.chocolate_brown))
            setBackgroundResource(R.drawable.back_view_border)
            textSize = 20f
            setText(target.name)
        }
        nameTextInputLayout.addView(nameEditText)

        val priceTextInputLayout = TextInputLayout(context).apply {
            hint = "Enter price"
            setPadding(10, 10, 10, 10)
        }
        val priceEditText = EditText(context).apply {
            setTextColor(ContextCompat.getColor(context, R.color.chocolate_brown))
            inputType = InputType.TYPE_CLASS_NUMBER
            setBackgroundResource(R.drawable.back_view_border)
            textSize = 18f
            setText(target.price.toString().toLong().format_number())
        }

        priceEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val price_edit_txt = priceEditText.text.toString()

                if (price_edit_txt.isNotEmpty()) {
                    val cleanString = price_edit_txt.replace(",", "")
                    try {
                        val priceLong = cleanString.toLong()
                        val formattedPrice = String.format("%,d", priceLong)
                        priceEditText.removeTextChangedListener(this)
                        priceEditText.setText(formattedPrice)

                        priceEditText.setSelection(formattedPrice.length)
                        priceEditText.addTextChangedListener(this)
                    } catch (e: NumberFormatException) {
                        Toast.makeText(context, "enter the valid number !!!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        priceTextInputLayout.addView(priceEditText)

        val necessaryCheckBox = CheckBox(context).apply {
            setTextColor(ContextCompat.getColor(context, R.color.chocolate_brown))
            text = "Is it necessary?"
            setPadding(10, 10, 10, 20)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = 50 }
            isChecked = target.necessary
        }

        // Initialize image_alert only after the layout is set up
        val imageView = ImageView(context).apply {
            setImageURI(target.img.toUri())
            contentDescription = "Select an image"
            setPadding(10, 10, 10, 10)
            setBackgroundResource(R.drawable.back_view_border)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = 50 }
            scaleType = ImageView.ScaleType.CENTER_INSIDE
        }

        // Add a button or TextView to clear the image
        val clearImageButton = Button(context).apply {
            text = "Clear Image"
            setTextColor(ContextCompat.getColor(context, R.color.red)) // Optional: Style the button
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = 20 }
        }

        clearImageButton.setOnClickListener {
            imageView.setImageURI(null) // Clear the image from ImageView
        }

        image_alert = imageView

        layoutAlertDialog.apply {
            addView(titleLayout)
            addView(nameTextInputLayout)
            addView(priceTextInputLayout)
            addView(necessaryCheckBox)
            addView(imageView)
            addView(clearImageButton) // Add the clear button to the layout
        }

        builderAlert.setView(layoutAlertDialog)
        builderAlert.setPositiveButton("Update", null)

        val dialog = builderAlert.create().apply {
            setCancelable(false)
            setCanceledOnTouchOutside(false)
            show()
        }

        val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE).apply {
            setTextColor(ContextCompat.getColor(context, R.color.white))
            setBackgroundColor(ContextCompat.getColor(context, R.color.caramel))
            textSize = 18f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        positiveButton.setOnClickListener {
            val name = nameEditText.text.toString().trim()
            val priceText = priceEditText.text.toString().replace(",","").trim()
            val date = persian_date_today
            val isNecessary = necessaryCheckBox.isChecked
            val img = imageView.drawable?.let { drawable ->
                val uri = getUriFromDrawable(drawable)
                uri?.let { saveImageToInternalStorage(it) }
            } ?: "android.resource://${context.packageName}/drawable/luxe_home"

            // Validate inputs
            if (name.isEmpty()) {
                Toast.makeText(context, "Name is empty!", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            if (priceText.isEmpty()) {
                Toast.makeText(context, "Price is empty!", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val price = priceText.toLongOrNull()
            if (price == null) {
                Toast.makeText(context, "Invalid price!", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val db = DBHandler.getDatabase(context)

            val updatedTarget = target.copy(
                name = name,
                price = price,
                necessary = isNecessary,
                img = img
            )

            Completable.fromAction {
                db.targetDao().updateTarget(updatedTarget)
            }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnTerminate {
                    Toast.makeText(context, "Update Target Successfully ✔✔✔", Toast.LENGTH_SHORT)
                        .show()
                    dialog.dismiss()
                    selectedImageUri = null
                    image_alert?.setImageResource(android.R.drawable.ic_menu_camera)
                }
                .subscribe(
                    { /* success */ },
                    { error -> error.printStackTrace() }
                )
        }

        dialog.setOnKeyListener { _, keyCode, _ ->
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                dialog.dismiss()
                true
            } else false
        }
    }

    fun saveImageToInternalStorage(uri: Uri): String? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val fileName = "image_${System.currentTimeMillis()}.jpg"
                val file = File(context.filesDir, fileName)
                FileOutputStream(file).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
                file.absolutePath
            }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    fun getUriFromDrawable(drawable: Drawable): Uri? {
        val bitmap = (drawable as BitmapDrawable).bitmap
        val fileName = "image_${System.currentTimeMillis()}.jpg"
        val file = File(context.cacheDir, fileName)

        try {
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
            }
            return Uri.fromFile(file) // مسیر فایل به Uri تبدیل می‌شود
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return null
    }

//    ............. end update the target recycler ..........................


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
