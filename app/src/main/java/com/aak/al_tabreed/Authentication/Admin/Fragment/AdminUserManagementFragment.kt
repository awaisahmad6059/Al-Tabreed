package com.aak.al_tabreed.Authentication.Admin.Fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aak.al_tabreed.Authentication.Admin.AdminAdapter.UserAdapter
import com.aak.al_tabreed.Authentication.Admin.AdminModel.User
import com.aak.al_tabreed.R
import com.google.firebase.firestore.FirebaseFirestore

class AdminUserManagementFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: UserAdapter
    private val userList = mutableListOf<User>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_admin_user_management, container, false)
        recyclerView = view.findViewById(R.id.userRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = UserAdapter(userList)
        recyclerView.adapter = adapter

        FirebaseFirestore.getInstance().collection("users").get()
            .addOnSuccessListener { result ->
                for (doc in result) {
                    val user = User(
                        username = doc.getString("username") ?: "",
                        userType = doc.getString("userType") ?: "",
                        profileImageUrl = doc.getString("profileImageUrl") ?: ""
                    )
                    userList.add(user)
                }
                adapter.notifyDataSetChanged()
            }
        return view
    }
}

