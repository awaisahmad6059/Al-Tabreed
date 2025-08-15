package com.aak.al_tabreed.Authentication.Admin

import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.aak.al_tabreed.R
import com.google.firebase.firestore.FirebaseFirestore

class AdminAddServiceActivity : AppCompatActivity() {

    private lateinit var categoryEditText: EditText
    private lateinit var descEditText: EditText
    private lateinit var submitBtn: Button
    private lateinit var cancelBtn: Button

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_add_service)

        categoryEditText = findViewById(R.id.editcategory)
        descEditText = findViewById(R.id.desc)
        submitBtn = findViewById(R.id.submit_btn)
        cancelBtn = findViewById(R.id.cancel_btn)

        val editCategory = intent.getStringExtra("category")
        var documentId: String? = null

        if (!editCategory.isNullOrEmpty()) {
            // Editing an existing service
            submitBtn.text = "Update"

            db.collection("arrays")
                .whereEqualTo("category", editCategory)
                .get()
                .addOnSuccessListener { documents ->
                    for (doc in documents) {
                        documentId = doc.id
                        val data = doc.data

                        categoryEditText.setText(data["category"] as? String ?: "")
                        descEditText.setText(data["description"] as? String ?: "")

                        // Load dynamic service fields

                    }
                }
        } else {
            // Add first empty service field if not editing
        }



        submitBtn.setOnClickListener {
            val category = categoryEditText.text.toString().trim()
            val description = descEditText.text.toString().trim()

            if (category.isEmpty() || description.isEmpty()) {
                Toast.makeText(this, "Please fill all main fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }



            val serviceData = hashMapOf<String, Any>(
                "category" to category,
                "description" to description,
            )





            if (documentId != null) {
                // UPDATE existing document
                db.collection("arrays").document(documentId!!)
                    .set(serviceData)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Service updated successfully", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Update failed: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            } else {
                // ADD new document
                db.collection("arrays")
                    .add(serviceData)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Services saved successfully", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            }
        }

        cancelBtn.setOnClickListener {
            finish()
        }
    }




}