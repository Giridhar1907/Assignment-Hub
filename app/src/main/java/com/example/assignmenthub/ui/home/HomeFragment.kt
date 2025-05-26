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
        fetchUploadsRealtime()

        return binding.root
    }

    private fun setupRecyclerView() {
        uploadsAdapter = UploadsAdapter(uploadsList) { pdfUrl ->
            openPdf(pdfUrl)
        }
        binding.uploadsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.uploadsRecyclerView.adapter = uploadsAdapter
    }

    private fun fetchUploadsRealtime() {
        databaseRef = FirebaseDatabase.getInstance().getReference("upload_jobs")

        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                uploadsList.clear()
                val currentUserId = auth.currentUser?.uid ?: ""
                val tempList = mutableListOf<Upload>()
                val uploadsToProcess = mutableListOf<DataSnapshot>()

                for (postSnapshot in snapshot.children) {
                    val upload = postSnapshot.getValue(Upload::class.java)
                    if (upload != null && upload.uploaderId != currentUserId && !upload.locked) {
                        uploadsToProcess.add(postSnapshot)
                    }
                }

                if (uploadsToProcess.isEmpty()) {
                    uploadsAdapter.notifyDataSetChanged()
                    Toast.makeText(requireContext(), "No uploads found", Toast.LENGTH_SHORT).show()
                    return
                }

                var processedCount = 0

                for (postSnapshot in uploadsToProcess) {
                    val upload = postSnapshot.getValue(Upload::class.java) ?: continue
                    val uploaderId = upload.uploaderId ?: continue
                    val userRef = FirebaseDatabase.getInstance().getReference("users").child(uploaderId)

                    userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(userSnapshot: DataSnapshot) {
                            val enhancedUpload = upload.copy(
                                username = userSnapshot.child("username").getValue(String::class.java),
                                email = userSnapshot.child("email").getValue(String::class.java),
                                regd = userSnapshot.child("regd").getValue(String::class.java),
                                number = userSnapshot.child("number").getValue(String::class.java)
                            )
                            tempList.add(enhancedUpload)

                            processedCount++
                            if (processedCount == uploadsToProcess.size) {
                                uploadsList.clear()
                                uploadsList.addAll(tempList)
                                uploadsAdapter.notifyDataSetChanged()
                                if (uploadsList.isEmpty()) {
                                    Toast.makeText(requireContext(), "No uploads found", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            processedCount++
                            if (processedCount == uploadsToProcess.size) {
                                uploadsList.clear()
                                uploadsList.addAll(tempList)
                                uploadsAdapter.notifyDataSetChanged()
                                if (uploadsList.isEmpty()) {
                                    Toast.makeText(requireContext(), "No uploads found", Toast.LENGTH_SHORT).show()
                                }
                            }
                            Log.e("HomeFragment", "User data fetch failed: ${error.message}")
                        }
                    })
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Failed to load uploads: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }





    private fun openPdf(pdfUrl: String) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(Uri.parse(pdfUrl), "application/pdf")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val chooser = Intent.createChooser(intent, "Open PDF")
        if (intent.resolveActivity(requireContext().packageManager) != null) {
            startActivity(chooser)
        } else {
            // fallback: open in browser
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(pdfUrl))
            startActivity(browserIntent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
