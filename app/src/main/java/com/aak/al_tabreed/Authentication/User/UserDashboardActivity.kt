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
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.aak.al_tabreed.Authentication.User.Fragment.UserContactFragment
import com.aak.al_tabreed.Authentication.User.Fragment.UserDashboardFragment
import com.aak.al_tabreed.Authentication.User.Fragment.UserDepositFragment
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
        setContentView(R.layout.activity_user_dashboard)
        loadLocale()

        userId = savedInstanceState?.getString("userId")
            ?: intent.getStringExtra("userId")
        userType = savedInstanceState?.getString("userType")
            ?: intent.getStringExtra("userType")
        username = savedInstanceState?.getString("username")
            ?: intent.getStringExtra("username")


        val userDashboardFragment = UserDashboardFragment()
        val bundle = Bundle()
        bundle.putString("userId", userId)
        userDashboardFragment.arguments = bundle

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, userDashboardFragment) // Must match the ID in your XML
            .commit()

        bottomNavigationView = findViewById(R.id.bottom_navigation)

        // Get intent extras
        userId = intent.getStringExtra("userId")
        val userType = intent.getStringExtra("userType")
        val username = intent.getStringExtra("username")

        Toast.makeText(this, "Logged in as User: $username", Toast.LENGTH_SHORT).show()

        // Load default fragment
        if (savedInstanceState == null) {
            loadFragment(UserDashboardFragment(), userId, false)
        }

        // Bottom navigation
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> loadFragment(UserDashboardFragment(), userId, true)
                R.id.order -> loadFragment(UserOrderHistoryFragment(), userId, true)

                R.id.contact -> {
                    fetchAndOpenWhatsAppNumber()
                    false
                }
                R.id.language -> {
                    showLanguageDialog()
                    false
                }

            }
            true
        }

        // Handle back press using OnBackPressedDispatcher
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)

                if (currentFragment is UserOrderHistoryFragment ||
                    currentFragment is UserContactFragment
                ) {
                    loadFragment(UserDashboardFragment(), userId, false)
                    bottomNavigationView.selectedItemId = R.id.home
                } else if (currentFragment is UserDashboardFragment) {
                    finish() // Exit app
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("userId", userId)
        outState.putString("userType", userType)
        outState.putString("username", username)
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

    private fun showLanguageDialog() {
        val languages = arrayOf("English", "العربية")
        AlertDialog.Builder(this)
            .setTitle("Select Language")
            .setItems(languages) { _, which ->
                when (which) {
                    0 -> setLocale("en")
                    1 -> setLocale("ar")
                }
            }
            .show()
    }

    private fun setLocale(languageCode: String) {
        val sharedPref = getSharedPreferences("Settings", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("My_Lang", languageCode)
            apply()
        }

        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        config.setLayoutDirection(locale)
        resources.updateConfiguration(config, resources.displayMetrics)

        // Restart activity but keep the logged-in user info
        val restartIntent = Intent(this, UserDashboardActivity::class.java).apply {
            putExtra("userId", userId)
            putExtra("userType", userType)
            putExtra("username", username)
        }
        startActivity(restartIntent)
        finish()
    }




    private fun fetchAndOpenWhatsAppNumber() {
        val firestore = FirebaseFirestore.getInstance()
        val contactDocRef = firestore.collection("contact").document("admin_contact")

        contactDocRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val number = document.getString("whatsapp")
                    if (!number.isNullOrEmpty()) {
                        showWhatsAppDialog(number)
                    } else {
                        Toast.makeText(this, "WhatsApp number not set", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Contact document does not exist", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to fetch WhatsApp number", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showWhatsAppDialog(number: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Contact via WhatsApp/التواصل عبر الواتس اب")
        builder.setMessage("WhatsApp Number:/رقم الواتساب: $number")

        builder.setPositiveButton("Chat/محادثة") { _, _ ->
            openWhatsApp(number)
        }

        builder.setNegativeButton("Copy/ينسخ") { dialog, _ ->
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("WhatsApp Number", number)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "Number copied to clipboard/تم نسخ الرقم إلى الحافظة", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        builder.setNeutralButton("Cancel/يلغي") { dialog, _ ->
            dialog.dismiss()
        }

        builder.create().show()
    }



    private fun openWhatsApp(phoneNumber: String) {
        val cleanNumber = phoneNumber.replace("+", "").replace(" ", "")
        val message = ""
        val uri = "https://wa.me/$cleanNumber?text=${Uri.encode(message)}"

        try {
            val pm = packageManager

            // Check normal WhatsApp
            val whatsappIntent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
            if (isPackageInstalled("com.whatsapp", pm)) {
                whatsappIntent.setPackage("com.whatsapp")
                startActivity(whatsappIntent)
                return
            }

            // Check WhatsApp Business
            if (isPackageInstalled("com.whatsapp.w4b", pm)) {
                whatsappIntent.setPackage("com.whatsapp.w4b")
                startActivity(whatsappIntent)
                return
            }

            Toast.makeText(this, "WhatsApp not installed", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error opening WhatsApp", Toast.LENGTH_SHORT).show()
        }
    }

    private fun isPackageInstalled(packageName: String, packageManager: PackageManager): Boolean {
        return try {
            packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }




    private fun loadFragment(fragment: Fragment, userId: String?, withAnimation: Boolean) {
        val bundle = Bundle().apply {
            putString("userId", userId)
        }
        fragment.arguments = bundle

        val transaction = supportFragmentManager.beginTransaction()

        if (withAnimation) {
            transaction.setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.slide_out_left,
                R.anim.slide_in_left,
                R.anim.slide_out_right
            )
        }

        transaction.replace(R.id.fragment_container, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }
}