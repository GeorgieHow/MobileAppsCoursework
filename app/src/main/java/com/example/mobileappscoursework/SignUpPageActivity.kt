package com.example.mobileappscoursework

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class SignUpPageActivity: AppCompatActivity() {

    private var mAuth = FirebaseAuth.getInstance()
    var db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.signup_page)

        val rootView = findViewById<View>(android.R.id.content)

        val createAccountButton = findViewById<Button>(R.id.create_account_button)
        createAccountButton.setOnClickListener {

            val emailEntered = findViewById<EditText>(R.id.email_text_input)
            val passwordEntered = findViewById<EditText>(R.id.password_text_input)
            val passwordRetyped = findViewById<EditText>(R.id.retype_password_text_input)
            val firstName = findViewById<EditText>(R.id.first_name_text_input)
            val secondName = findViewById<EditText>(R.id.second_name_text_input)

            val emailString = emailEntered.text.toString()
            val passwordString = passwordEntered.text.toString()
            val passwordRetypedString = passwordRetyped.text.toString()
            val firstNameString = firstName.text.toString()
            val secondNameString = secondName.text.toString()

            if(emailString.isNotEmpty() && passwordString.isNotEmpty()
                && firstNameString.isNotEmpty() && secondNameString.isNotEmpty() ){

                if(passwordString.equals(passwordRetypedString)){
                    mAuth.createUserWithEmailAndPassword(emailString, passwordString)
                        .addOnCompleteListener(this){ task ->
                            if(task.isSuccessful){
                                val user = task.result?.user
                                val userid = user?.uid

                                val usersCollection = FirebaseFirestore.getInstance()
                                    .collection("users")
                                val userDocument = usersCollection.document(userid!!)

                                val userDetails = hashMapOf(
                                    "email" to emailString,
                                    "firstName" to firstNameString,
                                    "secondName" to secondNameString,
                                )

                                userDocument.set(userDetails)
                                    .addOnSuccessListener {
                                        Log.e("Fire store", "Successful creation.")
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e("Fire store", "Error writing document", e)
                                    }

                                val createdAccountSnackbar = Snackbar.make(rootView,
                                    "Account has been successfully created.",
                                    Snackbar.LENGTH_LONG)
                                val textView = createdAccountSnackbar.view
                                    .findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
                                textView.textSize = 20f
                                createdAccountSnackbar.show()

                                finish()
                            }
                            else{
                                val errorMessage = task.exception?.message ?:
                                "Unknown error occurred when creating account. Please try again."
                                val errorCreatingAccountSnackbar = Snackbar.make(rootView,
                                    errorMessage, Snackbar.LENGTH_LONG)
                                val textView = errorCreatingAccountSnackbar.view
                                    .findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
                                textView.maxLines = 5
                                textView.textSize = 20f
                                errorCreatingAccountSnackbar.show()
                            }
                        }
                }
                else{
                    val mismatchedPasswordsSnackbar = Snackbar.make(rootView,
                        "Password fields do not match. Please re-enter and try again.",
                        Snackbar.LENGTH_LONG)
                    val textView = mismatchedPasswordsSnackbar.view
                        .findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
                    textView.textSize = 20f
                    mismatchedPasswordsSnackbar.show()
                }
            }
            else{
                val emptyFieldsSnackbar = Snackbar.make(rootView,
                    "Some fields are empty. Please ensure all fields are filled.",
                    Snackbar.LENGTH_LONG)
                val textView = emptyFieldsSnackbar.view
                    .findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
                textView.textSize = 20f
                emptyFieldsSnackbar.show()
            }
            }

            val backButton = findViewById<Button>(R.id.back_button)
            backButton.setOnClickListener {
                finish()
            }

    }
}