package com.aak.al_tabreed.Authentication.Admin

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.aak.al_tabreed.R
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AdminInprogressDetailActivity : AppCompatActivity() {

    private val firestore = FirebaseFirestore.getInstance()

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_inprogress_detail)

        val orderId = intent.getStringExtra("orderId") ?: return

        val deleteButton: Button = findViewById(R.id.deleteTask)
        val completeButton: Button = findViewById(R.id.completeTask)
        val cancelButton: Button = findViewById(R.id.cancelTask)
        val backButton: ImageButton = findViewById(R.id.back_button)

        fetchOrderDetails(orderId)

        backButton.setOnClickListener { onBackPressed() }
        cancelButton.setOnClickListener { onBackPressed() }



        completeButton.setOnClickListener {
            markOrderAsComplete(orderId)
        }

        deleteButton.setOnClickListener {
            deleteOrder(orderId)
        }
    }

    private fun deleteOrder(orderId: String) {
        firestore.collection("newOrders").document(orderId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Order deleted", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to delete order", Toast.LENGTH_SHORT).show()
            }
    }




    private fun fetchOrderDetails(orderId: String) {
        firestore.collection("newOrders").document(orderId).get()
            .addOnSuccessListener { doc ->
                val userId = doc.getString("userId") ?: ""
                val category = doc.getString("category") ?: ""
                val detail = doc.getString("detail") ?: ""

                val location = doc.getString("location") ?: ""
                val status = doc.getString("status") ?: ""
                val imageUrl = doc.getString("imageUrl") ?: ""

                findViewById<TextView>(R.id.useridTextView).text = userId
                findViewById<TextView>(R.id.categoryTextView).text = category
                findViewById<TextView>(R.id.detailTextView).text = detail
                findViewById<TextView>(R.id.locationTextView).text = location
                findViewById<TextView>(R.id.statusTxtView).text = status
                val imageView = findViewById<ImageView>(R.id.pictureImageView)

                if (imageUrl.isNotEmpty()) {
                    val extension = imageUrl.substringAfterLast('.', "").lowercase()

                    if (extension in listOf("jpg", "jpeg", "png", "gif")) {
                        // Load and show image
                        Picasso.get().load(imageUrl).into(imageView)

                        // On click, show download dialog
                        imageView.setOnClickListener {
                            showDownloadDialog(imageUrl, extension)
                        }
                    } else {
                        // If not image, hide the ImageView or set placeholder
                        imageView.setImageResource(R.drawable.image)
                        imageView.setOnClickListener(null)
                    }
                }

                firestore.collection("users").document(userId).get()
                    .addOnSuccessListener { userDoc ->
                        val username = userDoc.getString("username") ?: "Unknown"
                        findViewById<TextView>(R.id.userTextView).text = username
                    }
            }
    }

    private fun showDownloadDialog(url: String, extension: String) {
        val finalExtension = if (extension.isEmpty()) "jpg" else extension
        val fileName = "order_image_${System.currentTimeMillis()}.$finalExtension"

        AlertDialog.Builder(this)
            .setTitle("Download Image")
            .setMessage("Do you want to download this image?")
            .setPositiveButton("Download") { _, _ ->
                downloadFile(url, fileName)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun downloadFile(url: String, fileName: String) {
        val extension = fileName.substringAfterLast('.', "").lowercase()
        val mimeType = when (extension) {
            "jpg", "jpeg", "png", "gif" -> "image/*"
            else -> "*/*"
        }

        try {
            val request = DownloadManager.Request(Uri.parse(url))
                .setTitle(fileName)
                .setDescription("Downloading image...")
                .setMimeType(mimeType)
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

            val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            downloadManager.enqueue(request)

            Toast.makeText(this, "Download started...", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Download failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun markOrderAsComplete(orderId: String) {
        val sdf = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault())
        val currentTime = sdf.format(Date())

        firestore.collection("newOrders").document(orderId).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val userId = doc.getString("userId") ?: ""
                    val category = doc.getString("category") ?: ""
                    val detail = doc.getString("detail") ?: ""

                    val location = doc.getString("location") ?: ""
                    val imageUrl = doc.getString("imageUrl") ?: ""

                    firestore.collection("users").document(userId).get()
                        .addOnSuccessListener { userDoc ->
                            val username = userDoc.getString("username") ?: "Unknown"

                            val completeOrder = hashMapOf(
                                "userId" to userId,
                                "username" to username,
                                "category" to category,
                                "detail" to detail,
                                "location" to location,
                                "imageUrl" to imageUrl,
                                "status" to "Completed/مكتمل",
                                "time" to currentTime
                            )

                            // yahan update ke jagah set() use karo
                            firestore.collection("completeOrders").document(orderId)
                                .set(completeOrder)
                                .addOnSuccessListener {
                                    firestore.collection("newOrders").document(orderId)
                                        .delete()
                                        .addOnSuccessListener {
                                            Toast.makeText(this, "Order marked as completed", Toast.LENGTH_SHORT).show()
                                            finish()
                                        }
                                        .addOnFailureListener { e ->
                                            Toast.makeText(this, "Failed to delete from tasks: ${e.message}", Toast.LENGTH_SHORT).show()
                                        }
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(this, "Failed to save completed order: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                }
            }
    }

}