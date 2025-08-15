package com.aak.al_tabreed.Authentication.Admin.Fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aak.al_tabreed.Authentication.Admin.AdminAdapter.ServiceAdapter
import com.aak.al_tabreed.Authentication.Admin.AdminAddServiceActivity
import com.aak.al_tabreed.Authentication.Admin.AdminModel.Service
import com.aak.al_tabreed.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore

class AdminServiceFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var serviceAdapter: ServiceAdapter
    private val serviceList = mutableListOf<Service>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_admin_service, container, false)

        recyclerView = view.findViewById(R.id.serviceRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        serviceAdapter = ServiceAdapter(
            serviceList,
            onEditClick = { selectedService ->
                val intent = Intent(requireContext(), AdminAddServiceActivity::class.java)
                intent.putExtra("category", selectedService.category)
                startActivity(intent)
            },
            onDeleteClick = { selectedService ->
                deleteServiceFromFirestore(selectedService)
            }
        )

        recyclerView.adapter = serviceAdapter

        view.findViewById<FloatingActionButton>(R.id.addServiceButton).setOnClickListener {
            startActivity(Intent(requireContext(), AdminAddServiceActivity::class.java))
        }

        fetchServicesFromFirestore()

        return view
    }
    private fun deleteServiceFromFirestore(service: Service) {
        val db = FirebaseFirestore.getInstance()

        db.collection("arrays")
            .whereEqualTo("category", service.category)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    db.collection("arrays").document(document.id).delete()
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "Deleted successfully", Toast.LENGTH_SHORT).show()
                            fetchServicesFromFirestore() // Refresh list
                        }
                        .addOnFailureListener {
                            Toast.makeText(requireContext(), "Failed to delete", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error finding service", Toast.LENGTH_SHORT).show()
            }
    }


    private fun fetchServicesFromFirestore() {
        val db = FirebaseFirestore.getInstance()

        db.collection("arrays")
            .get()
            .addOnSuccessListener { documents ->
                serviceList.clear()
                for (doc in documents) {
                    val category = doc.getString("category") ?: continue
                    serviceList.add(Service(category))
                }
                serviceAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to fetch: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

}