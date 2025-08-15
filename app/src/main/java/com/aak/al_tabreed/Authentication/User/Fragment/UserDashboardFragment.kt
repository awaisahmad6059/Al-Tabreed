package com.aak.al_tabreed.Authentication.User.Fragment

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aak.al_tabreed.Authentication.Admin.AdminCompleteorderActivity
import com.aak.al_tabreed.Authentication.LoginActivity
import com.aak.al_tabreed.Authentication.User.UserAdapter.UserRecentTaskAdapter
import com.aak.al_tabreed.Authentication.User.UserModel.UserRecentTask
import com.aak.al_tabreed.Authentication.User.UserNewOrderActivity
import com.aak.al_tabreed.Authentication.User.UserServiceActivity
import com.aak.al_tabreed.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import java.util.Locale

class UserDashboardFragment : Fragment() {

    private var userId: String? = null

    private lateinit var userName: TextView
    private lateinit var recyclerView: RecyclerView

    private lateinit var adapter: UserRecentTaskAdapter
    private val taskList = mutableListOf<UserRecentTask>()
    private val firestore = FirebaseFirestore.getInstance()
    private var alertDialog: androidx.appcompat.app.AlertDialog? = null
    private var recentTasksListener: ListenerRegistration? = null





    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_user_dashboard, container, false)

        userId = arguments?.getString("userId")
        userName = view.findViewById(R.id.user_name)
        recyclerView = view.findViewById(R.id.recentTasksRecycler)
        recyclerView = view.findViewById(R.id.recentTasksRecycler)


        adapter = UserRecentTaskAdapter(taskList) { task ->
            // Handle task item click – maybe open detail fragment
        }
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
        fetchRecentTasks() // ✅ Always load recent tasks when fragment starts

        fetchAndShowNotification()


        view.findViewById<LinearLayout>(R.id.requestmaintanace).setOnClickListener {
            startActivity(Intent(requireActivity(), UserNewOrderActivity::class.java))
        }
        view.findViewById<LinearLayout>(R.id.notification).setOnClickListener {
            startActivity(Intent(requireActivity(), UserServiceActivity::class.java))
        }

        // Setup RecyclerView


        val logoutBtn = view.findViewById<ImageView>(R.id.sign_out_icon)
        logoutBtn.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Logout/ تسجيل الخروج")
                .setMessage("Are you sure you want to logout?/هل أنت متأكد أنك تريد تسجيل الخروج؟")
                .setPositiveButton("Yes/نعم") { dialog, _ ->
                    // Perform logout
                    FirebaseAuth.getInstance().signOut()
                    val intent = Intent(activity, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    activity?.finish()
                    dialog.dismiss()
                }
                .setNegativeButton("No/لا") { dialog, _ ->
                    dialog.dismiss() // Just dismiss the dialog
                }
                .setCancelable(false)
                .show()
        }



        if (userId != null) {
            loadUserProfile(userId!!)
        } else {
            Log.e("UserDashboardFragment", "User ID is null")
        }


        return view
    }





    private fun fetchAndShowNotification() {
        userId?.let { uid ->
            val db = FirebaseFirestore.getInstance()
            val userClosedRef = db.collection("users").document(uid).collection("closedNotifications")

            db.collection("notifications")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener { result ->
                    for (doc in result) {
                        val notificationId = doc.id
                        val title = doc.getString("title") ?: continue
                        val description = doc.getString("description") ?: continue

                        userClosedRef.document(notificationId).get().addOnSuccessListener { closedDoc ->
                            if (!closedDoc.exists()) {
                                showNotificationDialog(title, description, notificationId)
                            }
                        }
                    }
                }
        }
    }

    private fun showNotificationDialog(title: String, description: String, notificationId: String) {
        val context = requireContext()

        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 40, 48, 24)
        }

        val titleView = TextView(context).apply {
            text = title
            textSize = 20f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.BLACK)
        }

        val descView = TextView(context).apply {
            text = description
            textSize = 16f
            setTextColor(Color.DKGRAY)
            setPadding(0, 20, 0, 20)
        }

        val closeButton = Button(context).apply {
            text = "Close"
            setOnClickListener {
                // Save close status in Firestore
                userId?.let { uid ->
                    val closedRef = FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(uid)
                        .collection("closedNotifications")
                        .document(notificationId)

                    val closedData = mapOf("closedAt" to System.currentTimeMillis())
                    closedRef.set(closedData)
                }
                alertDialog?.dismiss()
            }
        }

        layout.addView(titleView)
        layout.addView(descView)
        layout.addView(closeButton)

        alertDialog = androidx.appcompat.app.AlertDialog.Builder(context)
            .setView(layout)
            .setCancelable(false)
            .create()

        alertDialog?.show()
    }


    private fun fetchRecentTasks() {
        userId?.let { uid ->
            // Remove previous listener if any
            recentTasksListener?.remove()

            recentTasksListener = firestore.collection("newOrders")
                .whereEqualTo("userId", uid)
                .whereEqualTo("status", "pending/قيد الانتظار")
                .orderBy("timestamp", Query.Direction.DESCENDING) // Optional: sort by newest
                .limit(3)
                .addSnapshotListener { snapshots, e ->
                    if (e != null) {
                        Log.e("UserDashboard", "Listen failed.", e)
                        return@addSnapshotListener
                    }

                    if (snapshots != null) {
                        taskList.clear()
                        for (doc in snapshots) {
                            val task = doc.toObject(UserRecentTask::class.java).copy(taskId = doc.id)
                            taskList.add(task)
                        }
                        adapter.notifyDataSetChanged()
                    }
                }
        } ?: run {
            Log.e("UserDashboard", "User ID is null")
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        recentTasksListener?.remove()
    }

    private fun loadUserProfile(userId: String) {
        firestore.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    userName.text = document.getString("username") ?: "Unknown User"
                } else {
                    Log.d("UserDashboardFragment", "No user profile found")
                }
            }
            .addOnFailureListener { e ->
                Log.e("UserDashboardFragment", "Error loading user profile", e)
            }
    }
}