package com.example.mobileappscoursework

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

class LogDetailsActivity: AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.log_item_details_layout)

        val backButton = findViewById<Button>(R.id.back_button)
        backButton.setOnClickListener {
            finish()
        }

        val date = intent.getStringExtra("logDate")
        val title = intent.getStringExtra("logTitle")
        val description = intent.getStringExtra("logDescription")
        val hours = intent.getIntExtra("logHours", 0)
        val tags = intent.getStringArrayListExtra("logTags")
        val location = intent.getStringExtra("logLocation")
        val imageUri = intent.getStringExtra("logImage")

        val titleTextView = findViewById<TextView>(R.id.title_text_view)
        val dateTextView = findViewById<TextView>(R.id.date_text_view)
        val locationTextView = findViewById<TextView>(R.id.location_text_view)
        val descriptionTextView = findViewById<TextView>(R.id.description_text_view)
        val hoursTextView = findViewById<TextView>(R.id.hours_text_view)
        val tagsTextView = findViewById<TextView>(R.id.tags_text_view)
        val pictureImageView = findViewById<ImageView>(R.id.log_image_view)

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
    }
}