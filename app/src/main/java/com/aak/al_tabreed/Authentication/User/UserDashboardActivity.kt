package com.aak.al_tabreed.Authentication.User

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.aak.al_tabreed.Authentication.User.Fragment.UserContactFragment
import com.aak.al_tabreed.Authentication.User.Fragment.UserDashboardFragment
import com.aak.al_tabreed.Authentication.User.Fragment.UserOrderHistoryFragment
import com.aak.al_tabreed.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale

class UserDashboardActivity : AppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView
    private var userId: String? = null
    private var userType: String? = null
    private var username: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadLocale() // Apply language first
        setContentView(R.layout.activity_user_dashboard)

        // Retrieve user info
        userId = intent.getStringExtra("userId")
        userType = intent.getStringExtra("userType")
        username = intent.getStringExtra("username")

        bottomNavigationView = findViewById(R.id.bottom_navigation)

        // Load default fragment
        if (savedInstanceState == null) {
            loadFragment(UserDashboardFragment(), userId, false)
        }

        // Bottom navigation
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> loadFragment(UserDashboardFragment(), userId, true)
                R.id.order -> loadFragment(UserOrderHistoryFragment(), userId, true)
                R.id.contact -> { fetchAndOpenWhatsAppNumber(); false }
                R.id.language -> { showLanguageDialog(); false }
                else -> true
            }
        }

        // Handle back press
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
                if (currentFragment is UserDashboardFragment) {
                    finish()
                } else {
                    loadFragment(UserDashboardFragment(), userId, false)
                    bottomNavigationView.selectedItemId = R.id.home
                }
            }
        })
    }

    private fun loadLocale() {
        val sharedPref = getSharedPreferences("Settings", MODE_PRIVATE)
        // Set default English if first launch
        if (!sharedPref.contains("My_Lang")) {
            with(sharedPref.edit()) { putString("My_Lang", "en"); apply() }
        }
        val language = sharedPref.getString("My_Lang", "en") ?: "en"
        val locale = Locale(language)
        Locale.setDefault(locale)

        val config = resources.configuration
        config.setLocale(locale)
        config.setLayoutDirection(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    private fun showLanguageDialog() {
        val languages = arrayOf("English", "العربية")
        AlertDialog.Builder(this)
            .setTitle("Select Language")
            .setItems(languages) { _, which ->
                when (which) {
                    0 -> setLocale("en")
                    1 -> setLocale("ar")
                }
            }.show()
    }

    private fun setLocale(languageCode: String) {
        val sharedPref = getSharedPreferences("Settings", MODE_PRIVATE)
        with(sharedPref.edit()) { putString("My_Lang", languageCode); apply() }

        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        config.setLayoutDirection(locale)
        resources.updateConfiguration(config, resources.displayMetrics)

        // Restart activity to apply changes
        val restartIntent = intent
        startActivity(restartIntent)
        finish()
    }


    private fun fetchAndOpenWhatsAppNumber() {
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("contact").document("admin_contact")
            .get()
            .addOnSuccessListener { document ->
                val number = document.getString("whatsapp")
                if (!number.isNullOrEmpty()) showWhatsAppDialog(number)
                else Toast.makeText(this, "WhatsApp number not set", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { Toast.makeText(this, "Failed to fetch WhatsApp number", Toast.LENGTH_SHORT).show() }
    }

    private fun showWhatsAppDialog(number: String) {
        AlertDialog.Builder(this)
            .setTitle("Contact via WhatsApp/التواصل عبر الواتس اب")
            .setMessage("WhatsApp Number:/رقم الواتساب: $number")
            .setPositiveButton("Chat/محادثة") { _, _ -> openWhatsApp(number) }
            .setNegativeButton("Copy/ينسخ") { dialog, _ ->
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("WhatsApp Number", number)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(this, "Number copied/تم النسخ", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .setNeutralButton("Cancel/يلغي") { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }

    private fun openWhatsApp(phoneNumber: String) {
        val cleanNumber = phoneNumber.replace("+", "").replace(" ", "")
        val uri = "https://wa.me/$cleanNumber?text="

        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))

            val pm = packageManager
            when {
                isPackageInstalled("com.whatsapp", pm) -> intent.setPackage("com.whatsapp")
                isPackageInstalled("com.whatsapp.w4b", pm) -> intent.setPackage("com.whatsapp.w4b")
                else -> {
                    Toast.makeText(this, "WhatsApp not installed", Toast.LENGTH_SHORT).show()
                    return
                }
            }

            startActivity(intent) // ✅ Call startActivity separately
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error opening WhatsApp", Toast.LENGTH_SHORT).show()
        }
    }


    private fun isPackageInstalled(packageName: String, pm: PackageManager) = try {
        pm.getPackageInfo(packageName, 0); true
    } catch (e: PackageManager.NameNotFoundException) { false }

    private fun loadFragment(fragment: Fragment, userId: String?, withAnimation: Boolean): Boolean {
        fragment.arguments = Bundle().apply { putString("userId", userId) }
        val transaction = supportFragmentManager.beginTransaction()
        if (withAnimation) {
            transaction.setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.slide_out_left,
                R.anim.slide_in_left,
                R.anim.slide_out_right
            )
        }
        transaction.replace(R.id.fragment_container, fragment).addToBackStack(null).commit()
        return true // ✅ Return true instead of TODO
    }

}
