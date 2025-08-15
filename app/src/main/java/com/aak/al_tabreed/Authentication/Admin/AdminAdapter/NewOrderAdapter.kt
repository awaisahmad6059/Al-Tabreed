package com.aak.al_tabreed.Authentication.Admin.AdminAdapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.aak.al_tabreed.Authentication.Admin.AdminModel.NewOrder
import com.aak.al_tabreed.R
import com.squareup.picasso.Picasso

class NewOrderAdapter(
    private var originalList: List<NewOrder>,
    private val onItemClick: (NewOrder) -> Unit
) : RecyclerView.Adapter<NewOrderAdapter.OrderViewHolder>() {

    private var filteredList: MutableList<NewOrder> = originalList.toMutableList()

    inner class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val usernameTextView: TextView = itemView.findViewById(R.id.username)
        val titleTextView: TextView = itemView.findViewById(R.id.title)
        val profileImageView: de.hdodenhof.circleimageview.CircleImageView = itemView.findViewById(R.id.ProfileImage)


        init {
            itemView.setOnClickListener {
                onItemClick(filteredList[adapterPosition])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.itemneworder, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = filteredList[position]
        holder.usernameTextView.text = "Username: ${order.username}"
        holder.titleTextView.text = "Category: ${order.category}"

        if (order.profileImageUrl.isNotEmpty()) {
            Picasso.get().load(order.profileImageUrl).into(holder.profileImageView)
        } else {
            holder.profileImageView.setImageResource(R.drawable.account_24)
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
                        it.detail.contains(query, ignoreCase = true)
            }.toMutableList()
        }
        notifyDataSetChanged()
    }

    fun updateData(newList: List<NewOrder>) {
        originalList = newList
        filteredList = newList.toMutableList()
        notifyDataSetChanged()
    }
}