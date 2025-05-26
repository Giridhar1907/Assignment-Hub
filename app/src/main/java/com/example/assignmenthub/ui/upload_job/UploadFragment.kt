package com.example.assignmenthub.ui.upload_job

import android.app.DatePickerDialog
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.assignmenthub.databinding.FragmentUploadBinding
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
            Toast.makeText(requireContext(), "PDF Selected", Toast.LENGTH_SHORT).show()
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
            Toast.makeText(requireContext(), "Please fill all fields and select a PDF", Toast.LENGTH_LONG).show()
            return
        }

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Toast.makeText(requireContext(), "Please login to upload files", Toast.LENGTH_SHORT).show()
            return
        }

        val storageRef = FirebaseStorage.getInstance().reference
        val fileName = "userFiles/$userId/${System.currentTimeMillis()}.pdf"
        val pdfRef = storageRef.child(fileName)

        pdfRef.putFile(pdfUri)
            .addOnSuccessListener {
                pdfRef.downloadUrl.addOnSuccessListener { uri ->
                    val job = UploadJob(
                        assignmentDescription = assignment,
                        cost = cost,
                        deadline = deadline,
                        pdfUrl = uri.toString(),
                        uploaderId = userId // uploaderId added here
                    )
                    val dbRef = FirebaseDatabase.getInstance().getReference("upload_jobs")
                    val key = dbRef.push().key!!

                    dbRef.child(key).setValue(job)
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "Uploaded successfully", Toast.LENGTH_SHORT).show()
                            clearFields()
                        }
                        .addOnFailureListener {
                            Toast.makeText(requireContext(), "Database upload failed", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "PDF upload failed", Toast.LENGTH_SHORT).show()
            }
    }

    private fun clearFields() {
        binding.editAssignmentName.text?.clear()
        binding.editCost.text?.clear()
        binding.editDeadline.text?.clear()
        viewModel.setPdfUri(null)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
