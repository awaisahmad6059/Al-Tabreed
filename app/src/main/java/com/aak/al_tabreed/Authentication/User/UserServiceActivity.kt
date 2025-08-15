package com.aak.al_tabreed.Authentication.User

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aak.al_tabreed.Authentication.Admin.AdminAdapter.ServiceAdapter
import com.aak.al_tabreed.Authentication.Admin.AdminModel.Service
import com.aak.al_tabreed.Authentication.User.UserAdapter.UserServiceAdapter
import com.aak.al_tabreed.R
import com.google.firebase.firestore.FirebaseFirestore

class UserServiceActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var UserServiceAdapter: UserServiceAdapter
    private val serviceList = mutableListOf<Service>()

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_service)

        val backButton: ImageButton = findViewById(R.id.back_button)
        backButton.setOnClickListener { onBackPressed() }


        recyclerView = findViewById(R.id.serviceRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        UserServiceAdapter = UserServiceAdapter(
            serviceList,

        )

        recyclerView.adapter = UserServiceAdapter

        fetchServicesFromFirestore()
    }

    private fun fetchServicesFromFirestore() {
        val db = FirebaseFirestore.getInstance()

        db.collection("arrays")
            .get()
            .addOnSuccessListener { documents ->
                serviceList.clear()
                for (doc in documents) {
                    val category = doc.getString("category") ?: continue
                    serviceList.add(Service(category))
                }
                UserServiceAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to fetch: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
