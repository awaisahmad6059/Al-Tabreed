package com.aak.al_tabreed.Authentication.Admin.AdminAdapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.aak.al_tabreed.Authentication.Admin.AdminModel.CompletedOrder
import com.aak.al_tabreed.R

class CompletedOrderAdapter(
    private var originalList: List<CompletedOrder>,
    private val onDeleteOrder: (orderId: String) -> Unit,
    private val onItemClick: (CompletedOrder) -> Unit
) : RecyclerView.Adapter<CompletedOrderAdapter.CompletedOrderViewHolder>() {

    private var filteredList: MutableList<CompletedOrder> = originalList.toMutableList()

    inner class CompletedOrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val categoryTextView: TextView = itemView.findViewById(R.id.category)
        val usernameTextView: TextView = itemView.findViewById(R.id.user_name)
        val descTextView: TextView = itemView.findViewById(R.id.desc)
        val locationTextView: TextView = itemView.findViewById(R.id.location)

        init {
            itemView.setOnLongClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onDeleteOrder(filteredList[position].orderId)
                }
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CompletedOrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.itemcompleteorder, parent, false)
        return CompletedOrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: CompletedOrderViewHolder, position: Int) {
        val order = filteredList[position]
        holder.categoryTextView.text = order.category
        holder.usernameTextView.text = order.username
        holder.descTextView.text = order.detail
        holder.locationTextView.text = order.location
        holder.itemView.setOnClickListener {
            onItemClick(order)
        }
    }

    override fun getItemCount(): Int = filteredList.size

    fun filter(query: String) {
        filteredList = if (query.isEmpty()) {
            originalList.toMutableList()
        } else {
            originalList.filter {
                it.username.contains(query, ignoreCase = true) ||
                        it.category.contains(query, ignoreCase = true) ||
                        it.detail.contains(query, ignoreCase = true) ||
                        it.location.contains(query, ignoreCase = true)
            }.toMutableList()
        }
        notifyDataSetChanged()
    }

    fun updateData(newList: List<CompletedOrder>) {
        originalList = newList
        filteredList = newList.toMutableList()
        notifyDataSetChanged()
    }
}