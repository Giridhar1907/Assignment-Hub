package com.example.assignmenthub.ui.home

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.assignmenthub.databinding.FragmentHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var databaseRef: DatabaseReference
    private val auth = FirebaseAuth.getInstance()
    private lateinit var uploadsAdapter: UploadsAdapter
    private val uploadsList = mutableListOf<Upload>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        setupRecyclerView()
        fetchUploads()

        return binding.root
    }

    private fun setupRecyclerView() {
        uploadsAdapter = UploadsAdapter(uploadsList) { pdfPath ->
            openPdf(pdfPath)
        }
        binding.uploadsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.uploadsRecyclerView.adapter = uploadsAdapter
    }

    private fun fetchUploads() {
        databaseRef = FirebaseDatabase.getInstance().getReference("Jobs")
        val currentUserId = auth.currentUser?.uid ?: ""

        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                uploadsList.clear()
                if (!snapshot.exists()) {
                    if (isAdded) {
                        Toast.makeText(context, "No jobs found", Toast.LENGTH_SHORT).show()
                    }
                    return
                }

                for (jobSnapshot in snapshot.children) {
                    val job = jobSnapshot.getValue(Upload::class.java)
                    if (job != null && job.uploaderId != null && job.uploaderId != currentUserId && !job.locked) {
                        val userRef = FirebaseDatabase.getInstance().getReference("users").child(job.uploaderId)
                        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(userSnapshot: DataSnapshot) {
                                val enhancedUpload = job.copy(
                                    username = userSnapshot.child("username").getValue(String::class.java),
                                    email = userSnapshot.child("email").getValue(String::class.java),
                                    regd = userSnapshot.child("regd").getValue(String::class.java),
                                    number = userSnapshot.child("number").getValue(String::class.java)
                                )
                                uploadsList.add(enhancedUpload)
                                uploadsAdapter.notifyItemInserted(uploadsList.size - 1)
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Log.e("HomeFragment", "User data fetch failed: ${error.message}")
                            }
                        })
                    }
                }
                if (uploadsList.isEmpty() && isAdded) {
                    Toast.makeText(context, "No other jobs found", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                if (isAdded) {
                    Toast.makeText(context, "Failed to load jobs: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun openPdf(pdfPath: String) {
        val storageRef = FirebaseStorage.getInstance().getReference(pdfPath)
        storageRef.downloadUrl.addOnSuccessListener { uri ->
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                flags = Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            val chooser = Intent.createChooser(intent, "Open PDF")
            if (intent.resolveActivity(requireContext().packageManager) != null) {
                startActivity(chooser)
            } else {
                val browserIntent = Intent(Intent.ACTION_VIEW, uri)
                startActivity(browserIntent)
            }
        }.addOnFailureListener { 
            Toast.makeText(requireContext(), "Failed to get PDF URL", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
