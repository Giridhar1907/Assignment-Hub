package com.example.assignmenthub.ui.upload_job

import android.app.DatePickerDialog
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.assignmenthub.R
import com.example.assignmenthub.databinding.FragmentUploadBinding
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.*

class UploadFragment : Fragment() {

    private var _binding: FragmentUploadBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: UploadViewModel

    private val pickPdf = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            viewModel.setPdfUri(uri)
            binding.buttonUploadPdf.text = "PDF Selected"
            (binding.buttonUploadPdf as MaterialButton).icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_check_circle)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUploadBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[UploadViewModel::class.java]

        binding.buttonUploadPdf.setOnClickListener {
            pickPdf.launch("application/pdf")
        }

        binding.editDeadline.setOnClickListener {
            showDatePicker()
        }

        binding.buttonSubmit.setOnClickListener {
            uploadDataToFirebase()
        }

        return binding.root
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val datePicker = DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                val selectedDate = Calendar.getInstance().apply {
                    set(year, month, day)
                }
                val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                binding.editDeadline.setText(format.format(selectedDate.time))
            },
            calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.show()
    }

    private fun uploadDataToFirebase() {
        val assignment = binding.editAssignmentName.text.toString()
        val cost = binding.editCost.text.toString()
        val deadline = binding.editDeadline.text.toString()
        val pdfUri = viewModel.pdfUri.value

        if (assignment.isBlank() || cost.isBlank() || deadline.isBlank() || pdfUri == null) {
            showConfirmationDialog("Incomplete Form", "Please fill all fields and select a PDF.")
            return
        }

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            showConfirmationDialog("Authentication Error", "Please login to upload files.")
            return
        }

        val storageRef = FirebaseStorage.getInstance().reference
        val fileName = "userFiles/$userId/${System.currentTimeMillis()}.pdf"
        val pdfRef = storageRef.child(fileName)

        pdfRef.putFile(pdfUri)
            .addOnSuccessListener { 
                val job = UploadJob(
                    assignmentDescription = assignment,
                    cost = cost,
                    deadline = deadline,
                    pdfPath = fileName, // Store the path instead of the URL
                    uploaderId = userId
                )
                val dbRef = FirebaseDatabase.getInstance().getReference("Jobs")
                val key = dbRef.push().key!!

                dbRef.child(key).setValue(job)
                    .addOnSuccessListener {
                        dbRef.child(key).child("uploadId").setValue(key) // Set the uploadId
                        showConfirmationDialog("Success", "Your job has been uploaded successfully.")
                        clearFields()
                    }
                    .addOnFailureListener {
                        showConfirmationDialog("Database Error", "Failed to upload job details. Please try again.")
                    }
            }
            .addOnFailureListener {
                showConfirmationDialog("Upload Failed", "Failed to upload PDF. Please try again.")
            }
    }

    private fun showConfirmationDialog(title: String, message: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun clearFields() {
        binding.editAssignmentName.text?.clear()
        binding.editCost.text?.clear()
        binding.editDeadline.text?.clear()
        viewModel.setPdfUri(null)
        binding.buttonUploadPdf.text = "Upload PDF"
        (binding.buttonUploadPdf as MaterialButton).icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_upload_file)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
