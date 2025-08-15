package com.aak.al_tabreed.Authentication.User.UserAdapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.aak.al_tabreed.Authentication.Admin.AdminAdapter.ServiceAdapter
import com.aak.al_tabreed.Authentication.Admin.AdminModel.Service
import com.aak.al_tabreed.R

class UserServiceAdapter (
    private val serviceList: List<Service>,
) : RecyclerView.Adapter<UserServiceAdapter.UserServiceAdapter>() {

    class UserServiceAdapter(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val serviceNameText: TextView = itemView.findViewById(R.id.serviceNameText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserServiceAdapter {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.itemuserservice, parent, false)
        return UserServiceAdapter(view)
    }

    override fun onBindViewHolder(holder: UserServiceAdapter, position: Int) {
        val service = serviceList[position]
        holder.serviceNameText.text = service.category


    }

    override fun getItemCount(): Int = serviceList.size
}