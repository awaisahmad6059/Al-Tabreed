package com.aak.al_tabreed.Authentication

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.aak.al_tabreed.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SignUpActivity : AppCompatActivity() {

    private lateinit var inputEmail: EditText
    private lateinit var inputPassword: EditText
    private lateinit var inputConfirmPassword: EditText
    private lateinit var inputUsername: EditText
    private lateinit var inputPhone: EditText
    private lateinit var btnRegister: Button
    private lateinit var alreadyHaveAccount: TextView
    private lateinit var backButton: ImageView

    private lateinit var progressDialog: ProgressDialog
    private lateinit var mAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        inputEmail = findViewById(R.id.et_email)
        inputPassword = findViewById(R.id.et_password)
        inputConfirmPassword = findViewById(R.id.et_confirm_password)
        inputUsername = findViewById(R.id.et_username)
        inputPhone = findViewById(R.id.et_phone)
        btnRegister = findViewById(R.id.btn_sign_up)
        alreadyHaveAccount = findViewById(R.id.tv_sign_in)
        backButton = findViewById(R.id.back_button)

        progressDialog = ProgressDialog(this)
        mAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        alreadyHaveAccount.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        backButton.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        btnRegister.setOnClickListener {
            validateInput()
        }
    }

    private fun validateInput() {
        val email = inputEmail.text.toString().trim()
        val password = inputPassword.text.toString().trim()
        val confirmPassword = inputConfirmPassword.text.toString().trim()
        val username = inputUsername.text.toString().trim()
        val phone = inputPhone.text.toString().trim()

        if (!email.matches(Regex("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"))) {
            inputEmail.error = "Enter a valid email"
            return
        }

        if (!(phone.startsWith("+92") || phone.startsWith("+966"))) {
            inputPhone.error = "Use +92 (Pakistan) or +966 (Saudi Arabia) country code"
            return
        }


        if (password.isEmpty() || password.length < 6) {
            inputPassword.error = "Password too short"
            return
        }

        if (password != confirmPassword) {
            inputConfirmPassword.error = "Passwords do not match"
            return
        }

        checkIfUserExists(email, username, phone, password)
    }

    private fun checkIfUserExists(email: String, username: String, phone: String, password: String) {
        progressDialog.setMessage("Checking user info...")
        progressDialog.setCancelable(false)
        progressDialog.show()

        firestore.collection("users")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { emailDocs ->
                if (!emailDocs.isEmpty) {
                    progressDialog.dismiss()
                    inputEmail.error = "Email already registered"
                    return@addOnSuccessListener
                }

                firestore.collection("users")
                    .whereEqualTo("username", username)
                    .get()
                    .addOnSuccessListener { usernameDocs ->
                        if (!usernameDocs.isEmpty) {
                            progressDialog.dismiss()
                            inputUsername.error = "Username already taken"
                            return@addOnSuccessListener
                        }

                        firestore.collection("users")
                            .whereEqualTo("phone", phone)
                            .get()
                            .addOnSuccessListener { phoneDocs ->
                                if (!phoneDocs.isEmpty) {
                                    progressDialog.dismiss()
                                    inputPhone.error = "Phone already registered"
                                } else {
                                    registerUser(email, username, phone, password)
                                }
                            }
                    }
            }
    }

    private fun registerUser(email: String, username: String, phone: String, password: String) {
        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = mAuth.currentUser?.uid ?: return@addOnCompleteListener
                    val userMap = hashMapOf(
                        "userId" to userId,
                        "email" to email,
                        "username" to username,
                        "phone" to phone,
                        "userType" to "User"
                    )

                    firestore.collection("users")
                        .document(userId)
                        .set(userMap)
                        .addOnSuccessListener {
                            progressDialog.dismiss()
                            Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, LoginActivity::class.java))
                            finish()
                        }
                        .addOnFailureListener {
                            progressDialog.dismiss()
                            Toast.makeText(this, "Error saving user: ${it.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    progressDialog.dismiss()
                    Toast.makeText(this, "Registration failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
