package com.aak.al_tabreed.Authentication.Admin.Fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.aak.al_tabreed.R
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class AdminNotificationFragment : Fragment() {



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_admin_notification, container, false)

        val etTitle = view.findViewById<EditText>(R.id.etTitle)
        val etDesc = view.findViewById<EditText>(R.id.etDesc)
        val btnSend = view.findViewById<Button>(R.id.btnSend)
        val btnCancel = view.findViewById<Button>(R.id.btnCancel)

        btnSend.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val desc = etDesc.text.toString().trim()

            if (title.isEmpty() || desc.isEmpty()) {
                Toast.makeText(context, "Fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val data = hashMapOf(
                "title" to title,
                "description" to desc,
                "timestamp" to FieldValue.serverTimestamp(),
                "visibleToAll" to true
            )

            FirebaseFirestore.getInstance().collection("notifications").add(data)
                .addOnSuccessListener {
                    Toast.makeText(context, "Sent", Toast.LENGTH_SHORT).show()
                    etTitle.text.clear()
                    etDesc.text.clear()
                }
        }

        btnCancel.setOnClickListener {
            etTitle.text.clear()
            etDesc.text.clear()
        }

        return view
    }
}