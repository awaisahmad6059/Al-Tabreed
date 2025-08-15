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
        loadLocale()


        ivNewIcon.setOnClickListener {
            showLanguageDialog()
        }

        tvSignUp.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        btnLogin.setOnClickListener {
            loginUser()
        }

        whatsapp.setOnClickListener {
            fetchAndOpenWhatsAppNumber()
        }
    }

    private fun loadLocale() {
        val sharedPref = getSharedPreferences("Settings", MODE_PRIVATE)
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
                    0 -> setLocale("en") // English
                    1 -> setLocale("ar") // Arabic
                }
            }
            .show()
    }

    private fun setLocale(languageCode: String) {
        val sharedPref = getSharedPreferences("Settings", MODE_PRIVATE)
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

        recreate() // Refresh activity to apply language
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

        // Check if user entered an email
        if (android.util.Patterns.EMAIL_ADDRESS.matcher(userInput).matches()) {
            signInWithEmail(userInput, password)
        } else {
            // Search in users and admins for username or phone number
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
                        if (email != null) {
                            found = true
                            signInWithEmail(email, password)
                        }
                    } else {
                        // Try phone number if username fails
                        firestore.collection(collection)
                            .whereEqualTo("phone", identifier)
                            .get()
                            .addOnSuccessListener { phoneDocs ->
                                if (!phoneDocs.isEmpty) {
                                    val email = phoneDocs.documents[0].getString("email")
                                    if (email != null && !found) {
                                        found = true
                                        signInWithEmail(email, password)
                                    }
                                } else if (collection == "users") {
                                    progressDialog.dismiss()
                                    Toast.makeText(
                                        this,
                                        "No user found with given phone or username",
                                        Toast.LENGTH_SHORT
                                    ).show()
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
                            val intent = Intent(this, AdminDashboardActivity::class.java)
                            intent.putExtra("adminId", userId)
                            startActivity(intent)
                            finish()
                        } else {
                            firestore.collection("users").document(userId).get()
                                .addOnSuccessListener { userDoc ->
                                    progressDialog.dismiss()
                                    if (userDoc.exists()) {
                                        val intent = Intent(this, UserDashboardActivity::class.java)
                                        intent.putExtra("userId", userId)
                                        startActivity(intent)
                                        finish()
                                    } else {
                                        Toast.makeText(this, "User not found in any collection", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                .addOnFailureListener {
                                    progressDialog.dismiss()
                                    Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                    }
                    .addOnFailureListener {
                        progressDialog.dismiss()
                        Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Login failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchAndOpenWhatsAppNumber() {
        val loadingDialog = ProgressDialog(this).apply {
            setMessage("Fetching WhatsApp contact...")
            setCancelable(false)
            show()
        }

        val contactDocRef = firestore.collection("contact").document("admin_contact")
        contactDocRef.get()
            .addOnSuccessListener { document ->
                loadingDialog.dismiss()
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
                loadingDialog.dismiss()
                Toast.makeText(this, "Failed to fetch WhatsApp number", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showWhatsAppDialog(number: String) {
        if (isFinishing || isDestroyed) return

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Contact via WhatsApp")
        builder.setMessage("WhatsApp Number: $number")

        builder.setPositiveButton("Chat") { _, _ ->
            openWhatsApp(number)
        }

        builder.setNegativeButton("Copy") { dialog, _ ->
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("WhatsApp Number", number)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "Number copied to clipboard", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        builder.setNeutralButton("Cancel") { dialog, _ -> dialog.dismiss() }
        val dialog = builder.create()
        if (!isFinishing && !isDestroyed) dialog.show()
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
}
