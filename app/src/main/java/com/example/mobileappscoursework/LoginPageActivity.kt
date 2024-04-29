package com.example.mobileappscoursework

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth

class LoginPageActivity: AppCompatActivity() {

    private var mAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_page)

        val rootView = findViewById<View>(android.R.id.content)
        val loginButton = findViewById<Button>(R.id.login_button)
        loginButton.setOnClickListener {

            val emailEntered = findViewById<EditText>(R.id.email_text_input)
            val passwordEntered = findViewById<EditText>(R.id.password_text_input)
            val emailEnteredString = emailEntered.text.toString()
            val passwordEnteredString = passwordEntered.text.toString()

            if (emailEnteredString.isNotEmpty() && passwordEnteredString.isNotEmpty()) {
                mAuth.signInWithEmailAndPassword(emailEnteredString, passwordEnteredString)
                    .addOnCompleteListener(this) { task ->
                        if(task.isSuccessful){
                            val intent = Intent(this, HomeActivity::class.java)
                            startActivity(intent)
                        }
                        else{
                            val incorrectLoginSnackbar = Snackbar.make(rootView,
                                "Account details have been entered in wrong. Please try again.",
                                Snackbar.LENGTH_LONG)
                            val textView = incorrectLoginSnackbar.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
                            textView.textSize = 20f
                            incorrectLoginSnackbar.show()
                        }
                    }
            }
            else{
                val attemptLoginSnackbar = Snackbar.make(rootView,
                    "Make sure all fields are filled in when attempting to login.",
                    Snackbar.LENGTH_LONG)
                val textView = attemptLoginSnackbar.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
                textView.textSize = 20f
                attemptLoginSnackbar.show()
            }
        }

        val signUpButton = findViewById<Button>(R.id.sign_up_button)
        signUpButton.setOnClickListener {
            val intent = Intent(this, SignUpPageActivity::class.java)
            startActivity(intent)
        }
    }
}