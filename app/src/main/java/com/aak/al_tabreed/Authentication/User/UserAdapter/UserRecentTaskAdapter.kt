package com.aak.al_tabreed.Authentication.User.UserAdapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.aak.al_tabreed.Authentication.User.UserModel.UserRecentTask
import com.aak.al_tabreed.R
import com.google.android.material.chip.Chip

class UserRecentTaskAdapter(
    private val taskList: List<UserRecentTask>,
    private val onItemClick: (UserRecentTask) -> Unit
) : RecyclerView.Adapter<UserRecentTaskAdapter.TaskViewHolder>() {

    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val taskTitle: TextView = itemView.findViewById(R.id.taskdetailTextView)
        val location: TextView = itemView.findViewById(R.id.locationTextView)
        val time: TextView = itemView.findViewById(R.id.timeTextView)
        val statusChip: Chip = itemView.findViewById(R.id.statusChip)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.itemuserrecenttask, parent, false)
        return TaskViewHolder(view)
    }

    override fun getItemCount(): Int = taskList.size

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = taskList[position]
        holder.taskTitle.text = task.category
        holder.location.text = task.location
        holder.statusChip.text = task.status
        holder.time.text = task.time

        holder.itemView.setOnClickListener {
            onItemClick(task)
        }
    }
}