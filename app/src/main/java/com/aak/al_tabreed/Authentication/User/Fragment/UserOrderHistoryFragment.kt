package com.aak.al_tabreed.Authentication.User.Fragment

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aak.al_tabreed.Authentication.User.UserAdapter.UserMyTaskAdapter
import com.aak.al_tabreed.Authentication.User.UserModel.UserRecentTask
import com.aak.al_tabreed.R
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot

class UserOrderHistoryFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: UserMyTaskAdapter
    private val taskList = mutableListOf<UserRecentTask>()
    private val firestore = FirebaseFirestore.getInstance()
    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userId = arguments?.getString("userId")


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_user_order_history, container, false)
        recyclerView = view.findViewById(R.id.allTasksRecycler)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = UserMyTaskAdapter(
            taskList,
            onItemClick = { task -> /* Handle task click */ },
            showContactDialog = { type, number -> showContactDialog(type, number) }
        )

        recyclerView.adapter = adapter
        fetchAllTasks()
        return view
    }

    private fun showContactDialog(type: String, number: String) {
        val options = arrayOf("Copy/ينسخ", if (type == "WhatsApp") "Chat/محادثة" else "Call/يتصل")

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Select an action/حدد إجراءً")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> { // Copy
                        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Contact", number)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(requireContext(), "$type number copied/تم نسخ الرقم", Toast.LENGTH_SHORT).show()
                    }
                    1 -> {
                        if (type == "WhatsApp") {
                            openWhatsAppWithCheck(number)
                        } else {
                            makePhoneCall(number)
                        }
                    }
                }
                dialog.dismiss()
            }
            .show()
    }



    private fun openWhatsAppWithCheck(number: String) {
        val cleanNumber = number.replace("+", "").replace(" ", "")
        val uri = "https://wa.me/$cleanNumber"

        val pm = requireContext().packageManager
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))

        // Check normal WhatsApp
        if (isPackageInstalled("com.whatsapp", pm)) {
            intent.setPackage("com.whatsapp")
            startActivity(intent)
            return
        }

        // Check WhatsApp Business
        if (isPackageInstalled("com.whatsapp.w4b", pm)) {
            intent.setPackage("com.whatsapp.w4b")
            startActivity(intent)
            return
        }

        Toast.makeText(requireContext(), "WhatsApp not installed", Toast.LENGTH_SHORT).show()
    }

    private fun isPackageInstalled(packageName: String, pm: PackageManager): Boolean {
        return try {
            pm.getPackageInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }


    private fun makePhoneCall(number: String) {
        try {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number"))
            startActivity(intent)
        } catch (e: Exception) {
            Log.e("PhoneCall", "Error making call", e)
        }
    }

    private fun fetchAllTasks() {
        userId?.let { uid ->
            val combinedTaskList = mutableListOf<UserRecentTask>()
            val pendingTasksQuery = firestore.collection("newOrders").whereEqualTo("userId", uid)
            val completedTasksQuery = firestore.collection("completeOrders").whereEqualTo("userId", uid)

            Tasks.whenAllSuccess<QuerySnapshot>(
                pendingTasksQuery.get(),
                completedTasksQuery.get()
            ).addOnSuccessListener { querySnapshots ->
                taskList.clear()
                querySnapshots.forEach { snapshot ->
                    for (doc in snapshot) {
                        val task = doc.toObject(UserRecentTask::class.java).copy(taskId = doc.id)
                        combinedTaskList.add(task)
                    }
                }
                taskList.addAll(combinedTaskList.sortedByDescending { it.time })
                adapter.notifyDataSetChanged()
            }.addOnFailureListener { e ->
                Log.e("UserDashboard", "Failed to fetch tasks", e)
            }
        } ?: run {
            Log.e("UserDashboard", "User ID is null")
        }
    }
}