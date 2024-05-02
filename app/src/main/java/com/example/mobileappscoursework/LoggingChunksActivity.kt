package com.example.mobileappscoursework

import NumberPickerDialogFragment
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class LoggingChunksActivity: AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val mAUth = FirebaseAuth.getInstance()
    private val uid = mAUth.currentUser?.uid
    private val tags = mutableListOf<String>()
    private val uploadedTags = mutableSetOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.logging_chunks_layout)

        val rootView = findViewById<View>(android.R.id.content)

        uid?.let { userId ->
            db.collection("tags").whereEqualTo("uid", userId).get().addOnSuccessListener { documents ->
                uploadedTags.addAll(documents.mapNotNull { it.getString("tag") })
            }
        }

        // Cancel Button.
        val cancelButton = findViewById<Button>(R.id.cancel_button)
        cancelButton.setOnClickListener {
            finish()
        }

        // Functionality for autofill.
        val tagsAutoCompleteTextView = findViewById<AutoCompleteTextView>(R.id.chunk_tags_text)
        setupAutoCompleteTextView(tagsAutoCompleteTextView)
        val chipGroup = findViewById<ChipGroup>(R.id.tags_chip_group)

        tagsAutoCompleteTextView.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val tagText = tagsAutoCompleteTextView.text.toString()
                if (tagText.isNotEmpty()) {
                    addChipToGroup(tagText, chipGroup)
                    tagsAutoCompleteTextView.text = null
                }
                true
            } else {
                false
            }
        }

        val dateEditText = findViewById<TextInputEditText>(R.id.chunk_date_text)
        val currentDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Calendar.getInstance().time)
        dateEditText.setText(currentDate)

        // Functionality for continue upload, uploads to firebase.
        val continueButton = findViewById<Button>(R.id.continue_button)
        continueButton.setOnClickListener {

            val title = findViewById<TextInputEditText>(R.id.chunk_title_text).text.toString()
            val description = findViewById<TextInputEditText>(R.id.chunk_description_text).text.toString()
            val date = findViewById<TextInputEditText>(R.id.chunk_date_text).text.toString()
            val hours = findViewById<TextInputEditText>(R.id.chunk_hours_text).text.toString().toIntOrNull() ?: 0


            val inputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val dateFormat = inputFormat.parse(date)
            val targetFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val sortableDate = targetFormat.format(dateFormat)

            if (title.isBlank() || description.isBlank() || date.isBlank()
                || hours.toString().isBlank() || tags.isEmpty()) {
                val emptyFieldsSnackbar = Snackbar.make(rootView,
                    "Please ensure all fields have been filled in. " +
                            "Tags field needs at least one tag.",
                    Snackbar.LENGTH_LONG)
                val textView = emptyFieldsSnackbar.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
                textView.textSize = 20f
                textView.maxLines = 3
                emptyFieldsSnackbar.show()
            }
            else if(!isValidDate(date)){
                val invalidDateSnackbar = Snackbar.make(rootView,
                    "The date you have filled is invalid. Make sure it is in the format of" +
                            " dd/MM/yyyy (e.g. 12/03/2024), and is not after todays date.",
                    Snackbar.LENGTH_LONG)
                val textView = invalidDateSnackbar.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
                textView.textSize = 20f
                textView.maxLines = 5
                invalidDateSnackbar.show()
            }
            else{
                val logEntry = hashMapOf(
                    "title" to title,
                    "description" to description,
                    "date" to date,
                    "hours" to hours,
                    "tags" to tags.toList(),
                    "sortableDate" to sortableDate
                )

                uid?.let { userId ->
                    val userLogsRef = db.collection("users").document(userId).collection("logs")
                    userLogsRef.add(logEntry).addOnSuccessListener { document ->
                        tags.forEach { tag ->
                            if (!uploadedTags.contains(tag)) {
                                val tagsRef = db.collection("tags")
                                tagsRef.add(
                                    hashMapOf(
                                        "tag" to tag,
                                        "uid" to userId
                                    )
                                )
                            }
                        }

                        val uploadedDocumentID = document.id

                        val intent = Intent(this, LoggingChunksContinuedActivity::class.java)
                        intent.putExtra("uploadedDocumentID", uploadedDocumentID)
                        startActivity(intent)
                        finish()

                    }.addOnFailureListener { e ->
                        val errorUploadSnackbar = Snackbar.make(rootView,
                            "Unable to upload log. Please try again.",
                            Snackbar.LENGTH_LONG)
                        val textView = errorUploadSnackbar.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
                        textView.textSize = 20f
                        errorUploadSnackbar.show()
                    }
                }
            }
        }
    }

    private fun setupAutoCompleteTextView(autoCompleteTextView: AutoCompleteTextView) {
        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line)
        autoCompleteTextView.setAdapter(adapter)

        uid?.let { userId ->
            db.collection("tags").whereEqualTo("uid", userId).get().addOnSuccessListener { documents ->
                val existingTags = documents.map { it.getString("tag") ?: "" }.filterNot { it.isBlank() }
                adapter.clear()
                adapter.addAll(existingTags)
                adapter.notifyDataSetChanged()
            }
        }
    }

    private fun addChipToGroup(tagText: String, chipGroup: ChipGroup) {
        val chip = Chip(this).apply {
            text = tagText
            isCloseIconVisible = true
            isClickable = true
            isCheckable = false
            setOnCloseIconClickListener {
                chipGroup.removeView(this)
                tags.remove(tagText)
            }
        }
        chipGroup.addView(chip)
        if (!tags.contains(tagText)) {
            tags.add(tagText)
        }
    }

    fun onDateFieldClicked(view: View) {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select date")
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .build()

        datePicker.addOnPositiveButtonClickListener { selection: Long ->
            val formattedDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(selection))
            val dateEditText = findViewById<TextInputEditText>(R.id.chunk_date_text)
            dateEditText.setText(formattedDate)
        }

        datePicker.show(supportFragmentManager, datePicker.toString())
    }

    fun onHoursFieldClicked(view: View) {
        val numberPickerDialog = HoursPickerDialogFragment().apply {
            listener = object : HoursPickerDialogFragment.HoursPickerDialogListener {
                override fun onNumberPicked(number: Int) {
                    val chunkHoursEditText = findViewById<TextInputEditText>(R.id.chunk_hours_text)
                    chunkHoursEditText.setText(number.toString())
                }
            }
        }
        numberPickerDialog.show(supportFragmentManager, "numberPicker")
    }


    private fun isValidDate(dateStr: String): Boolean {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        dateFormat.isLenient = false
        try {
            val date = dateFormat.parse(dateStr) ?: return false

            val calendarMin = Calendar.getInstance().apply {
                set(Calendar.YEAR, 1970)
                set(Calendar.MONTH, Calendar.JANUARY)
                set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val calendarMax = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }

            val parsedDate = Calendar.getInstance().apply { time = date }
            return !parsedDate.before(calendarMin) && !parsedDate.after(calendarMax)
        } catch (e: ParseException) {
            return false
        }
    }

}