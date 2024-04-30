package com.example.mobileappscoursework

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class LoggingChunksContinuedActivity: AppCompatActivity() {

    private lateinit var selectedImageUri: Uri
    private val db = FirebaseFirestore.getInstance()
    private val mAUth = FirebaseAuth.getInstance()
    private val uid = mAUth.currentUser?.uid
    private val storage = FirebaseStorage.getInstance().reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.logging_chunks_continued_layout)

        val uploadedDocumentID = intent.getStringExtra("uploadedDocumentID")

        val pictureImageView: ImageView = findViewById(R.id.picture_image_view)
        val uploadPictureButton: Button = findViewById(R.id.upload_picture_button)
        val finishUploadButton: Button = findViewById(R.id.finish_upload_button)

        uploadPictureButton.setOnClickListener {
            openFilePicker()
        }

        finishUploadButton.setOnClickListener {
            if (::selectedImageUri.isInitialized) {
                val imageRef = storage.child("images/${System.currentTimeMillis()}_${selectedImageUri.lastPathSegment}")
                val uploadTask = imageRef.putFile(selectedImageUri)

                uploadTask.continueWithTask { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let {
                            throw it
                        }
                    }
                    imageRef.downloadUrl
                }.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val downloadUri = task.result
                        val location = findViewById<TextInputEditText>(R.id.chunk_location_text).text.toString()

                        val logUpdate = hashMapOf<String, Any>()
                        logUpdate["imageUrl"] = downloadUri?.toString() ?: "N/A"
                        logUpdate["location"] = location.ifEmpty { "N/A" }

                        uid?.let { userId ->
                            db.collection("users").document(userId)
                                .collection("logs").document(uploadedDocumentID!!)
                                .update(logUpdate as Map<String, Any>)
                                .addOnSuccessListener {
                                    finish()
                                }
                                .addOnFailureListener { _ ->
                                }
                        }
                    } else {
                    }
                }
            } else {
                val location = findViewById<TextInputEditText>(R.id.chunk_location_text).text.toString()

                val logUpdate = hashMapOf<String, Any>()
                logUpdate["imageUrl"] = "N/A"
                logUpdate["location"] = location.ifEmpty { "N/A" }

                uid?.let { userId ->
                    db.collection("users").document(userId)
                        .collection("logs").document(uploadedDocumentID!!)
                        .update(logUpdate as Map<String, Any>)
                        .addOnSuccessListener {
                            finish()
                        }
                        .addOnFailureListener { _ ->
                        }
                }
            }

        }
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
                    .into(findViewById(R.id.picture_image_view))
            }
        }
    }

}