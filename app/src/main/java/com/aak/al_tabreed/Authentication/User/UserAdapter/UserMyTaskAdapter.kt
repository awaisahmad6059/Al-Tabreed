package com.aak.al_tabreed.Authentication.User.UserAdapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.aak.al_tabreed.Authentication.User.UserModel.UserRecentTask
import com.aak.al_tabreed.R
import com.google.android.material.chip.Chip
import com.google.firebase.firestore.FirebaseFirestore

class UserMyTaskAdapter(
    private val taskList: List<UserRecentTask>,
    private val onItemClick: (UserRecentTask) -> Unit,
    private val showContactDialog: (String, String) -> Unit
) : RecyclerView.Adapter<UserMyTaskAdapter.TaskViewHolder>() {

    private val firestore = FirebaseFirestore.getInstance()
    private val contactDocRef = firestore.collection("contact").document("admin_contact")

    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val taskcategory: TextView = itemView.findViewById(R.id.taskcategoryTextView)
        val location: TextView = itemView.findViewById(R.id.tvLocation)
        val time: TextView = itemView.findViewById(R.id.tvTime)
        val statusChip: Chip = itemView.findViewById(R.id.statusChip)
        val chatIcon: ImageView = itemView.findViewById(R.id.ivChat)
        val callIcon: ImageView = itemView.findViewById(R.id.ivCall)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.itemusermytask, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = taskList[position]
        holder.taskcategory.text = task.category
        holder.location.text = task.location
        holder.time.text = task.time

        // Set status with color
        setStatusColor(holder.statusChip, task.status)
        holder.statusChip.text = task.status

        // Set click listeners
        holder.itemView.setOnClickListener { onItemClick(task) }

        holder.chatIcon.setOnClickListener {
            contactDocRef.get().addOnSuccessListener { doc ->
                val whatsappNumber = doc.getString("whatsapp") ?: return@addOnSuccessListener
                showContactDialog("WhatsApp", whatsappNumber)

            }
        }

        holder.callIcon.setOnClickListener {
            contactDocRef.get().addOnSuccessListener { doc ->
                val callNumber = doc.getString("call") ?: return@addOnSuccessListener
                showContactDialog("Call", callNumber)

            }
        }
    }

    private fun setStatusColor(chip: Chip, status: String?) {
        val context = chip.context
        val normalizedStatus = status?.trim()?.lowercase()

        when (normalizedStatus) {
            "pending/قيد الانتظار" -> {
                chip.setChipBackgroundColorResource(R.color.status_pending)
                chip.setTextColor(ContextCompat.getColor(context, R.color.white))
            }
            "inprogress", "inprogress/في تَقَدم" -> {
                chip.setChipBackgroundColorResource(R.color.status_in_progress)
                chip.setTextColor(ContextCompat.getColor(context, R.color.white))
            }
            "completed", "completed/مكتمل" -> {
                chip.setChipBackgroundColorResource(R.color.status_completed)
                chip.setTextColor(ContextCompat.getColor(context, R.color.white))
            }
            else -> {
                Log.w("StatusError", "Unexpected status: '$status'")
                chip.setChipBackgroundColorResource(R.color.status_default)
                chip.setTextColor(ContextCompat.getColor(context, R.color.black))
            }
        }
    }

    override fun getItemCount(): Int = taskList.size
}