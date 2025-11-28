package com.example.assignmenthub.ui.uploads

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.assignmenthub.databinding.FragmentUploadsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class UploadsFragment : Fragment() {

    private var _binding: FragmentUploadsBinding? = null
    private val binding get() = _binding!!

    private lateinit var uploadsAdapter: UploadsAdapter
    private val uploadsList = mutableListOf<Upload>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUploadsBinding.inflate(inflater, container, false)
        setupRecyclerView()
        fetchUploadsFromFirebase()
        return binding.root
    }

    private fun setupRecyclerView() {
        uploadsAdapter = UploadsAdapter(uploadsList)
        binding.jobsRecyclerView.apply {
            adapter = uploadsAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun fetchUploadsFromFirebase() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId == null) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        val databaseRef = FirebaseDatabase.getInstance().getReference("Jobs")

        databaseRef.orderByChild("uploaderId").equalTo(currentUserId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    uploadsList.clear()
                    if (!snapshot.exists()) {
                        Log.d("UploadsFragment", "No uploads found")
                    }
                    for (uploadSnapshot in snapshot.children) {
                        val upload = uploadSnapshot.getValue(Upload::class.java)
                        upload?.let {
                            it.uploadId = uploadSnapshot.key ?: ""
                            uploadsList.add(it)
                        }
                    }
                    Log.d("UploadsFragment", "Uploads size: ${uploadsList.size}")
                    uploadsAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), "Failed to load uploads", Toast.LENGTH_SHORT).show()
                }
            })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
