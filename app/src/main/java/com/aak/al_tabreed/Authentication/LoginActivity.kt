package com.aak.al_tabreed.Authentication

import android.app.ProgressDialog
import android.content.*
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.aak.al_tabreed.Authentication.Admin.AdminDashboardActivity
import com.aak.al_tabreed.Authentication.User.UserDashboardActivity
import com.aak.al_tabreed.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale

class LoginActivity : AppCompatActivity() {

    private lateinit var inputEmail: EditText
    private lateinit var inputPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var whatsapp: ImageView
    private lateinit var tvSignUp: TextView
    private lateinit var progressDialog: ProgressDialog
    private lateinit var mAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var ivNewIcon: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadLocale() // ✅ Apply language first

        setContentView(R.layout.activity_login)

        inputEmail = findViewById(R.id.et_email)
        inputPassword = findViewById(R.id.et_password)
        btnLogin = findViewById(R.id.btn_sign_in)
        whatsapp = findViewById(R.id.iv_whatsapp)
        tvSignUp = findViewById(R.id.tv_sign_up)
        progressDialog = ProgressDialog(this)
        mAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        ivNewIcon = findViewById(R.id.iv_new_icon)

        // Language change dialog
        ivNewIcon.setOnClickListener { showLanguageDialog() }

        tvSignUp.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        btnLogin.setOnClickListener { loginUser() }
        whatsapp.setOnClickListener { fetchAndOpenWhatsAppNumber() }
    }

    private fun loadLocale() {
        val sharedPref = getSharedPreferences("Settings", MODE_PRIVATE)
        // Set default English if not exist
        if (!sharedPref.contains("My_Lang")) {
            with(sharedPref.edit()) {
                putString("My_Lang", "en")
                apply()
            }
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

        loadLocale()
        recreate() // refresh activity
    }


    private fun loginUser() {
        val userInput = inputEmail.text.toString().trim()
        val password = inputPassword.text.toString().trim()

        if (userInput.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
            return
        }

        progressDialog.setMessage("Logging in...")
        progressDialog.setCanceledOnTouchOutside(false)
        progressDialog.show()

        if (android.util.Patterns.EMAIL_ADDRESS.matcher(userInput).matches()) {
            signInWithEmail(userInput, password)
        } else {
            searchForUser(userInput, password)
        }
    }

    private fun searchForUser(identifier: String, password: String) {
        val collections = listOf("admins", "users")
        var found = false

        for (collection in collections) {
            firestore.collection(collection)
                .whereEqualTo("username", identifier)
                .get()
                .addOnSuccessListener { docs ->
                    if (!docs.isEmpty) {
                        val email = docs.documents[0].getString("email")
                        if (email != null) { found = true; signInWithEmail(email, password) }
                    } else {
                        firestore.collection(collection)
                            .whereEqualTo("phone", identifier)
                            .get()
                            .addOnSuccessListener { phoneDocs ->
                                if (!phoneDocs.isEmpty) {
                                    val email = phoneDocs.documents[0].getString("email")
                                    if (email != null && !found) { found = true; signInWithEmail(email, password) }
                                } else if (collection == "users") {
                                    progressDialog.dismiss()
                                    Toast.makeText(this, "No user found", Toast.LENGTH_SHORT).show()
                                }
                            }
                    }
                }
        }
    }

    private fun signInWithEmail(email: String, password: String) {
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                val userId = mAuth.currentUser?.uid ?: return@addOnSuccessListener
                firestore.collection("admins").document(userId).get()
                    .addOnSuccessListener { adminDoc ->
                        if (adminDoc.exists()) {
                            progressDialog.dismiss()
                            startActivity(Intent(this, AdminDashboardActivity::class.java).apply {
                                putExtra("adminId", userId)
                            })
                            finish()
                        } else {
                            firestore.collection("users").document(userId).get()
                                .addOnSuccessListener { userDoc ->
                                    progressDialog.dismiss()
                                    if (userDoc.exists()) {
                                        startActivity(Intent(this, UserDashboardActivity::class.java).apply {
                                            putExtra("userId", userId)
                                        })
                                        finish()
                                    } else {
                                        Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
                                    }
                                }
                        }
                    }
            }
            .addOnFailureListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Login failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchAndOpenWhatsAppNumber() {
        val loadingDialog = ProgressDialog(this).apply {
            setMessage("Fetching WhatsApp contact..."); setCancelable(false); show()
        }

        firestore.collection("contact").document("admin_contact").get()
            .addOnSuccessListener { doc ->
                loadingDialog.dismiss()
                val number = doc.getString("whatsapp")
                if (!number.isNullOrEmpty()) showWhatsAppDialog(number)
                else Toast.makeText(this, "WhatsApp number not set", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { loadingDialog.dismiss(); Toast.makeText(this, "Failed to fetch", Toast.LENGTH_SHORT).show() }
    }

    private fun showWhatsAppDialog(number: String) {
        if (isFinishing || isDestroyed) return
        AlertDialog.Builder(this)
            .setTitle("Contact via WhatsApp")
            .setMessage("WhatsApp Number: $number")
            .setPositiveButton("Chat") { _, _ -> openWhatsApp(number) }
            .setNegativeButton("Copy") { dialog, _ ->
                (getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).apply {
                    setPrimaryClip(ClipData.newPlainText("WhatsApp Number", number))
                }
                Toast.makeText(this, "Number copied", Toast.LENGTH_SHORT).show(); dialog.dismiss()
            }
            .setNeutralButton("Cancel") { dialog, _ -> dialog.dismiss() }
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


    private fun isPackageInstalled(packageName: String, pm: PackageManager): Boolean {
        return try { pm.getPackageInfo(packageName, 0); true } catch (e: PackageManager.NameNotFoundException) { false }
    }
}
