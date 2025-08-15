package com.aak.al_tabreed.Authentication.User

import android.app.Activity
import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.aak.al_tabreed.Authentication.FileUtil
import com.aak.al_tabreed.R
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import id.zelory.compressor.Compressor
import kotlinx.coroutines.launch
import java.io.File
import java.util.Calendar

class UserNewOrderActivity : AppCompatActivity() {

    companion object {
        private var cloudinaryInitialized = false
    }
    private lateinit var spinnerCategory: Spinner
    private lateinit var inputDetail: EditText
    private lateinit var addMedia: LinearLayout
    private lateinit var imagePreview: ImageView
    private lateinit var inputTime: TextView
    private lateinit var inputLocation: EditText
    private lateinit var submitBtn: Button

    private val firestore by lazy { FirebaseFirestore.getInstance() }
    private val auth by lazy { FirebaseAuth.getInstance() }
    private var userId: String? = null

    private val categoryMap = mutableMapOf<String, Map<String, Any>>()
    private var uploadedImageUrl: String? = null

    private val IMAGE_PICK_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_new_order)

        spinnerCategory = findViewById(R.id.spinner_category)
        inputDetail = findViewById(R.id.input_detail)
        addMedia = findViewById(R.id.addmedia)
        imagePreview = findViewById(R.id.image_preview)
        inputTime = findViewById(R.id.inputtime)
        inputLocation = findViewById(R.id.inoutlocation)
        submitBtn = findViewById(R.id.submit_btn)
        val backButton = findViewById<ImageButton>(R.id.back_button)
        val cancelButton = findViewById<Button>(R.id.cancel_btn)

        backButton.setOnClickListener { finish() }
        cancelButton.setOnClickListener { finish() }

        userId = intent.getStringExtra("userId") ?: auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "User ID is not available", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        fetchCategories()

        addMedia.setOnClickListener { openGallery() }
        inputTime.setOnClickListener { showDateTimePicker() }
        submitBtn.setOnClickListener { submitOrder() }
    }

    private fun fetchCategories() {
        firestore.collection("arrays")
            .get()
            .addOnSuccessListener { result ->
                val categoryList = mutableListOf("Choose Category")
                for (document in result) {
                    val category = document.getString("category") ?: continue
                    categoryList.add(category)
                    categoryMap[category] = document.data
                }
                val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categoryList)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerCategory.adapter = adapter
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    private fun showDateTimePicker() {
        val calendar = Calendar.getInstance()

        DatePickerDialog(this, { _, year, month, day ->
            TimePickerDialog(this, { _, hour, minute ->
                val dateTime = String.format("%02d/%02d/%04d %02d:%02d", day, month + 1, year, hour, minute)
                inputTime.text = dateTime
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false).show()

        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == IMAGE_PICK_CODE && resultCode == Activity.RESULT_OK) {
            val uri = data?.data ?: return
            imagePreview.setImageURI(uri) // show image immediately

            lifecycleScope.launch {
                val file = FileUtil.from(this@UserNewOrderActivity, uri)
                val compressedFile = Compressor.compress(this@UserNewOrderActivity, file)
                uploadImageToCloudinary(compressedFile)
            }
        }
    }

    private fun uploadImageToCloudinary(file: File) {
        val config = hashMapOf(
            "cloud_name" to "df4g8opnc",
            "api_key" to "332919639529399",
            "api_secret" to "Lrs2SexJDtpswPfi1V5KVtjVn6s"
        )

        // Prevent multiple initialization using custom flag
        if (!cloudinaryInitialized) {
            MediaManager.init(this, config)
            cloudinaryInitialized = true
        }

        MediaManager.get().upload(file.absolutePath)
            .callback(object : UploadCallback {
                override fun onStart(requestId: String?) {
                    Toast.makeText(this@UserNewOrderActivity, "Uploading.../جارٍ التحميل...", Toast.LENGTH_SHORT).show()
                }

                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}

                override fun onSuccess(requestId: String?, resultData: Map<*, *>?) {
                    uploadedImageUrl = resultData?.get("secure_url").toString()
                    Toast.makeText(this@UserNewOrderActivity, "Image uploaded!/تم تحميل الصورة!", Toast.LENGTH_SHORT).show()
                }

                override fun onError(requestId: String?, error: ErrorInfo?) {
                    Toast.makeText(this@UserNewOrderActivity, "Upload failed/فشل التحميل", Toast.LENGTH_SHORT).show()
                }

                override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
            }).dispatch()
    }


    private fun submitOrder() {
        val category = spinnerCategory.selectedItem?.toString() ?: ""
        val detail = inputDetail.text.toString().trim()
        val time = inputTime.text.toString()
        val location = inputLocation.text.toString().trim()

        if (category == "Choose Category" || detail.isEmpty() || time.isEmpty() || location.isEmpty()) {
            Toast.makeText(this, "Fill all fields/املأ جميع الحقول", Toast.LENGTH_SHORT).show()
            return
        }

        simulateLoading {
            val data = hashMapOf(
                "userId" to userId,
                "category" to category,
                "detail" to detail,
                "imageUrl" to uploadedImageUrl,
                "time" to time,
                "location" to location,
                "timestamp" to FieldValue.serverTimestamp(),
                "status" to "pending/قيد الانتظار"
            )
            firestore.collection("newOrders")
                .add(data)
                .addOnSuccessListener {
                    Toast.makeText(this, "Order submitted!/تم تقديم الطلب!", Toast.LENGTH_SHORT).show()
                    clearForm()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Submit failed/فشل الإرسال", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun simulateLoading(onComplete: () -> Unit) {
        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Submitting..../تقديم...")
        progressDialog.setCancelable(false)
        progressDialog.show()

        var progress = 0
        val handler = Handler(Looper.getMainLooper())
        handler.post(object : Runnable {
            override fun run() {
                progress += 1
                progressDialog.setMessage("Loading/تحميل $progress%")
                if (progress < 100) {
                    handler.postDelayed(this, 30)
                } else {
                    progressDialog.dismiss()
                    onComplete()
                }
            }
        })
    }

    private fun clearForm() {
        spinnerCategory.setSelection(0)
        inputDetail.text.clear()
        inputTime.text = ""
        inputLocation.text.clear()
        imagePreview.setImageResource(0)
        uploadedImageUrl = null
    }
}
