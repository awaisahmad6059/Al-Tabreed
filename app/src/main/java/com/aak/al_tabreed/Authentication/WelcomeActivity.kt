package com.aak.al_tabreed.Authentication

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.aak.al_tabreed.Authentication.Admin.AdminDashboardActivity
import com.aak.al_tabreed.Authentication.User.UserDashboardActivity
import com.aak.al_tabreed.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale

class WelcomeActivity : AppCompatActivity() {

    private lateinit var logo: ImageView
    private lateinit var topAnim: Animation
    private lateinit var bottomAnim: Animation

    private lateinit var mAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        loadLocale() // ✅ Load language before layout is set
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        logo = findViewById(R.id.logo)

        topAnim = AnimationUtils.loadAnimation(this, R.anim.top_animation)
        bottomAnim = AnimationUtils.loadAnimation(this, R.anim.bottom_animation)

        logo.startAnimation(bottomAnim)
        topAnim.duration = 3000
        bottomAnim.duration = 3000

        mAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        checkUserSession()
    }

    private fun loadLocale() {
        val sharedPref = getSharedPreferences("Settings", Context.MODE_PRIVATE)
        val language = sharedPref.getString("My_Lang", "en") // default English
        val locale = Locale(language!!)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        config.setLayoutDirection(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    private fun checkUserSession() {
        val currentUser = mAuth.currentUser
        Handler(Looper.getMainLooper()).postDelayed({
            if (currentUser != null) {
                redirectToDashboard(currentUser.uid)
            } else {
                navigateToLogin()
            }
        }, 2000)
    }

    private fun redirectToDashboard(userId: String) {
        firestore.collection("admins").document(userId).get()
            .addOnSuccessListener { adminDoc ->
                if (adminDoc.exists()) {
                    val intent = Intent(this, AdminDashboardActivity::class.java)
                    intent.putExtra("adminId", userId)
                    startActivity(intent)
                    finish()
                } else {
                    firestore.collection("users").document(userId).get()
                        .addOnSuccessListener { userDoc ->
                            if (userDoc.exists()) {
                                val username = userDoc.getString("username") ?: ""
                                val userType = userDoc.getString("userType") ?: "User"
                                val intent = Intent(this, UserDashboardActivity::class.java)
                                intent.putExtra("userId", userId)
                                intent.putExtra("username", username)
                                intent.putExtra("userType", userType)
                                startActivity(intent)
                                finish()
                            } else {
                                navigateToLogin()
                            }
                        }
                        .addOnFailureListener {
                            navigateToLogin()
                        }
                }
            }
            .addOnFailureListener {
                navigateToLogin()
            }
    }

    private fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}
