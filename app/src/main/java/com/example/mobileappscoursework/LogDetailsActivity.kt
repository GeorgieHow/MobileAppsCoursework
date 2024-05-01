package com.example.mobileappscoursework

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class LogDetailsActivity: AppCompatActivity(){

    private val db = FirebaseFirestore.getInstance()
    private val mAuth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance().reference

    private lateinit var titleTextView: TextView
    private lateinit var dateTextView: TextView
    private lateinit var locationTextView: TextView
    private lateinit var descriptionTextView: TextView
    private lateinit var hoursTitleTextView: TextView
    private lateinit var hoursTextView: TextView
    private lateinit var tagsTextView: TextView
    private lateinit var pictureImageView: ImageView

    private lateinit var titleEditTextLayout: TextInputLayout
    private lateinit var descriptionEditTextLayout: TextInputLayout
    private lateinit var dateEditTextLayout: TextInputLayout
    private lateinit var hoursEditTextLayout: TextInputLayout
    private lateinit var tagsEditTextLayout: TextInputLayout
    private lateinit var tagsChipGroup: ChipGroup
    private lateinit var locationEditTextLayout: TextInputLayout
    private lateinit var pictureEditImageView: ImageView
    private lateinit var editPictureButton: Button

    private lateinit var titleEditText: TextInputEditText
    private lateinit var descriptionEditText: TextInputEditText
    private lateinit var dateEditText: TextInputEditText
    private lateinit var hoursEditText: TextInputEditText
    private lateinit var tagsEditText: AutoCompleteTextView
    private lateinit var locationEditText: TextInputEditText

    private lateinit var backButton: Button
    private lateinit var editButton: Button
    private lateinit var cancelEditButton: Button
    private lateinit var confirmEditButton: Button

    private lateinit var selectedImageUri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.log_item_details_layout)

        backButton = findViewById(R.id.back_button)
        backButton.setOnClickListener {
            finish()
        }

        editButton = findViewById(R.id.update_button)
        editButton.setOnClickListener {
            loadEditUI()
        }

        cancelEditButton = findViewById(R.id.cancel_update_button)
        cancelEditButton.setOnClickListener {
            loadDetailsUI()
        }

        val logId = intent.getStringExtra("logID")

        confirmEditButton = findViewById(R.id.confirm_update_button)
        confirmEditButton.setOnClickListener{
            if (logId != null) {
                updateLogEntry(logId)
            }
        }

        val date = intent.getStringExtra("logDate")
        val title = intent.getStringExtra("logTitle")
        val description = intent.getStringExtra("logDescription")
        val hours = intent.getIntExtra("logHours", 0)
        val tags = intent.getStringArrayListExtra("logTags")
        val location = intent.getStringExtra("logLocation")
        val imageUri = intent.getStringExtra("logImage")
        if (!imageUri.isNullOrEmpty()){
            selectedImageUri = Uri.parse(imageUri)
        }

        titleTextView = findViewById(R.id.title_text_view)
        dateTextView = findViewById(R.id.date_text_view)
        locationTextView = findViewById(R.id.location_text_view)
        descriptionTextView = findViewById(R.id.description_text_view)
        hoursTextView = findViewById(R.id.hours_text_view)
        tagsTextView = findViewById(R.id.tags_text_view)
        pictureImageView = findViewById(R.id.log_image_view)
        hoursTitleTextView = findViewById(R.id.hours_title)

        titleTextView.text = title
        dateTextView.text = date
        descriptionTextView.text = description
        hoursTextView.text = hours.toString()
        tagsTextView.text = tags.toString()
        locationTextView.text = location

        Glide.with(this)
            .load(imageUri)
            .placeholder(R.drawable.shadow)
            .into(pictureImageView)

        titleEditTextLayout = findViewById(R.id.chunk_title)
        descriptionEditTextLayout = findViewById(R.id.chunk_description)
        dateEditTextLayout = findViewById(R.id.chunk_date)
        hoursEditTextLayout = findViewById(R.id.chunk_hours)
        tagsEditTextLayout = findViewById(R.id.chunk_tags)
        tagsChipGroup = findViewById(R.id.tags_chip_group)
        locationEditTextLayout = findViewById(R.id.chunk_location)
        pictureEditImageView = findViewById(R.id.picture_image_view)
        editPictureButton = findViewById(R.id.edit_picture_button)
        editPictureButton.setOnClickListener {
            openFilePicker()
        }


        titleEditText = findViewById(R.id.chunk_title_text)
        descriptionEditText = findViewById(R.id.chunk_description_text)
        dateEditText = findViewById(R.id.chunk_date_text)
        hoursEditText = findViewById(R.id.chunk_hours_text)
        tagsEditText = findViewById(R.id.chunk_tags_text)
        locationEditText = findViewById(R.id.chunk_location_text)

        titleEditText.setText(title)
        descriptionEditText.setText(description)
        dateEditText.setText(date)
        hoursEditText.setText(hours.toString())
        locationEditText.setText(location)

        if (tags != null) {
            for (tag in tags) {
                val chip = Chip(this).apply {
                    text = tag
                    isClickable = true
                    isCheckable = false
                    isCloseIconVisible = true
                    setOnCloseIconClickListener {
                        tagsChipGroup.removeView(this)
                    }
                }
                tagsChipGroup.addView(chip)
            }
        }

        Glide.with(this)
            .load(imageUri)
            .placeholder(R.drawable.shadow)
            .into(pictureEditImageView)
    }

    private fun loadEditUI(){
        titleTextView.visibility = View.GONE
        dateTextView.visibility = View.GONE
        descriptionTextView.visibility = View.GONE
        hoursTextView.visibility = View.GONE
        tagsTextView.visibility = View.GONE
        locationTextView.visibility = View.GONE
        pictureImageView.visibility = View.GONE
        hoursTitleTextView.visibility = View.GONE

        titleEditTextLayout.visibility = View.VISIBLE
        descriptionEditTextLayout.visibility = View.VISIBLE
        dateEditTextLayout.visibility = View.VISIBLE
        hoursEditTextLayout.visibility = View.VISIBLE
        tagsEditTextLayout.visibility = View.VISIBLE
        tagsChipGroup.visibility = View.VISIBLE
        locationEditTextLayout.visibility = View.VISIBLE
        pictureEditImageView.visibility = View.VISIBLE
        editPictureButton.visibility = View.VISIBLE

        backButton.visibility = View.GONE
        editButton.visibility = View.GONE
        cancelEditButton.visibility = View.VISIBLE
        confirmEditButton.visibility = View.VISIBLE
    }

    private fun loadDetailsUI(){
        titleTextView.visibility = View.VISIBLE
        dateTextView.visibility = View.VISIBLE
        descriptionTextView.visibility = View.VISIBLE
        hoursTextView.visibility = View.VISIBLE
        tagsTextView.visibility = View.VISIBLE
        locationTextView.visibility = View.VISIBLE
        pictureImageView.visibility = View.VISIBLE
        hoursTitleTextView.visibility = View.VISIBLE

        titleEditTextLayout.visibility = View.GONE
        descriptionEditTextLayout.visibility = View.GONE
        dateEditTextLayout.visibility = View.GONE
        hoursEditTextLayout.visibility = View.GONE
        tagsEditTextLayout.visibility = View.GONE
        tagsChipGroup.visibility = View.GONE
        locationEditTextLayout.visibility = View.GONE
        pictureEditImageView.visibility = View.GONE
        editPictureButton.visibility = View.GONE

        backButton.visibility = View.VISIBLE
        editButton.visibility = View.VISIBLE
        cancelEditButton.visibility = View.GONE
        confirmEditButton.visibility = View.GONE
    }

    private fun updateLogEntry(documentId: String) {
        val title = titleEditText.text.toString()
        val description = descriptionEditText.text.toString()
        val date = dateEditText.text.toString()
        val hours = hoursEditText.text.toString().toIntOrNull() ?: 0
        val location = locationEditText.text.toString()
        val tags = collectTagsFromChipGroup()

        if (title.isBlank() || description.isBlank() || date.isBlank()) {

            return
        }
        else if(!isValidDate(date)){

            return
        }

        val logRef = db.collection("users").document(mAuth.currentUser!!.uid).collection("logs").document(documentId!!)

        val logUpdate = hashMapOf(
            "title" to title,
            "description" to description,
            "date" to date,
            "hours" to hours,
            "tags" to tags,
            "location" to location
        )

        logRef.update(logUpdate)
            .addOnSuccessListener {
                handleImageUpload(logRef)
            }
            .addOnFailureListener { e ->
            }

        titleTextView.text = title
        dateTextView.text = date
        descriptionTextView.text = description
        hoursTextView.text = hours.toString()
        tagsTextView.text = tags.toString()
        locationTextView.text = location

        loadDetailsUI()
    }

    private fun collectTagsFromChipGroup(): List<String> {
        val tags = mutableListOf<String>()
        for (i in 0 until tagsChipGroup.childCount) {
            val chip = tagsChipGroup.getChildAt(i) as Chip
            tags.add(chip.text.toString())
        }
        return tags
    }

    private fun handleImageUpload(logRef: DocumentReference) {
        selectedImageUri?.let { uri ->
            val imageRef = storage.child("images/${System.currentTimeMillis()}_${uri.lastPathSegment}")
            imageRef.putFile(uri).continueWithTask { task ->
                if (!task.isSuccessful) task.exception?.let { throw it }
                imageRef.downloadUrl
            }.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val imageUrl = task.result.toString()
                    logRef.update("imageUrl", imageUrl)
                }
            }
        }

        Glide.with(this)
            .load(selectedImageUri)
            .placeholder(R.drawable.shadow)
            .into(pictureImageView)
    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.type = "image/*"
        resultLauncher.launch(intent)
    }

    private var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedImageUri = uri
                Glide.with(this)
                    .load(selectedImageUri)
                    .into(pictureEditImageView)
            }
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