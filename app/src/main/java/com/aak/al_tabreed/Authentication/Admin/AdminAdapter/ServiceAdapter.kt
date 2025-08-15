package com.aak.al_tabreed.Authentication.Admin.AdminAdapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.aak.al_tabreed.Authentication.Admin.AdminModel.Service
import com.aak.al_tabreed.R

class ServiceAdapter(
    private val serviceList: List<Service>,
    private val onEditClick: (Service) -> Unit,
    private val onDeleteClick: (Service) -> Unit
) : RecyclerView.Adapter<ServiceAdapter.ServiceViewHolder>() {

    class ServiceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val serviceNameText: TextView = itemView.findViewById(R.id.serviceNameText)
        val editIcon: View = itemView.findViewById(R.id.editIcon)
        val deleteIcon: View = itemView.findViewById(R.id.deleteIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_service, parent, false)
        return ServiceViewHolder(view)
    }

    override fun onBindViewHolder(holder: ServiceViewHolder, position: Int) {
        val service = serviceList[position]
        holder.serviceNameText.text = service.category

        holder.editIcon.setOnClickListener {
            onEditClick(service)
        }

        holder.deleteIcon.setOnClickListener {
            onDeleteClick(service)
        }
    }

    override fun getItemCount(): Int = serviceList.size
}