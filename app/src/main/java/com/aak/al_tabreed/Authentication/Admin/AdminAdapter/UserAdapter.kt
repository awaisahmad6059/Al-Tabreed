package com.aak.al_tabreed.Authentication.Admin.AdminAdapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.aak.al_tabreed.Authentication.Admin.AdminModel.User
import com.aak.al_tabreed.R
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class UserAdapter(private val users: List<User>) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {
    class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.tvName)
        val role: TextView = view.findViewById(R.id.tvRole)
        val image: CircleImageView = view.findViewById(R.id.imgProfile)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.usermanagement_item, parent, false)
        return UserViewHolder(view)
    }

    override fun getItemCount() = users.size

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]
        holder.name.text = user.username
        holder.role.text = user.userType
        if (!user.profileImageUrl.isNullOrEmpty()) {
            Picasso.get()
                .load(user.profileImageUrl)
                .placeholder(R.drawable.account_24)
                .error(R.drawable.account_24)
                .into(holder.image)
        } else {
            holder.image.setImageResource(R.drawable.account_24)
        }
    }
}
