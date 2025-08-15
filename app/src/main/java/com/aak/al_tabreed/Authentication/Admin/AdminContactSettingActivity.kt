package com.aak.al_tabreed.Authentication.Admin

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.aak.al_tabreed.R
import com.google.firebase.firestore.FirebaseFirestore

class AdminContactSettingActivity : AppCompatActivity() {

    private lateinit var whatsappNumberEditText: EditText
    private lateinit var callNumberEditText: EditText
    private lateinit var addButton: Button
    private lateinit var cancelButton: Button

    private val firestore = FirebaseFirestore.getInstance()
    private val contactDocRef = firestore.collection("contact").document("admin_contact")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_contact_setting)

        whatsappNumberEditText = findViewById(R.id.whatsappNumberEditText)
        callNumberEditText = findViewById(R.id.callNumberEditText)
        addButton = findViewById(R.id.addButton)
        cancelButton = findViewById(R.id.cancelButton)

        loadExistingContact()

        addButton.setOnClickListener {
            val whatsapp = whatsappNumberEditText.text.toString().trim()
            val call = callNumberEditText.text.toString().trim()

            if (whatsapp.isEmpty() || call.isEmpty()) {
                Toast.makeText(this, "Please enter both WhatsApp and Call numbers", Toast.LENGTH_SHORT).show()
            } else {
                saveContactNumbers(whatsapp, call)
            }
        }

        cancelButton.setOnClickListener {
            finish() // Go back to previous activity
        }
    }

    private fun loadExistingContact() {
        contactDocRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val whatsapp = document.getString("whatsapp")
                    val call = document.getString("call")
                    whatsappNumberEditText.setText(whatsapp)
                    callNumberEditText.setText(call)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load contact", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveContactNumbers(whatsapp: String, call: String) {
        val data = hashMapOf(
            "whatsapp" to whatsapp,
            "call" to call
        )

        contactDocRef.set(data)
            .addOnSuccessListener {
                Toast.makeText(this, "Contact saved successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to save contact", Toast.LENGTH_SHORT).show()
            }
    }
}
