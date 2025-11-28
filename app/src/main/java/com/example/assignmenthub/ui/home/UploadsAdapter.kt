package com.example.assignmenthub.ui.home

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.assignmenthub.R
import com.google.android.material.button.MaterialButton

class UploadsAdapter(
    private val uploads: List<Upload>,
    private val onPdfClick: (String) -> Unit
) : RecyclerView.Adapter<UploadsAdapter.UploadViewHolder>() {

    inner class UploadViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val jobTitle: TextView = view.findViewById(R.id.job_title)
        val description: TextView = view.findViewById(R.id.job_assignment_description)
        val cost: TextView = view.findViewById(R.id.job_cost)
        val deadline: TextView = view.findViewById(R.id.job_due_date)
        val viewPdfButton: MaterialButton = view.findViewById(R.id.btn_view_pdf)
        val uploaderName: TextView = view.findViewById(R.id.uploader_name)
        val uploaderPhone: TextView = view.findViewById(R.id.uploader_phone)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UploadViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_job, parent, false)
        // Hide buttons not relevant for the home screen
        view.findViewById<MaterialButton>(R.id.btn_lock_unlock).visibility = View.GONE
        view.findViewById<MaterialButton>(R.id.btn_delete).visibility = View.GONE
        return UploadViewHolder(view)
    }

    override fun onBindViewHolder(holder: UploadViewHolder, position: Int) {
        val upload = uploads[position]
        holder.jobTitle.text = upload.assignmentDescription // Or a more specific title field
        holder.description.text = upload.assignmentDescription
        holder.cost.text = "₹${upload.cost}"
        holder.deadline.text = upload.deadline
        holder.uploaderName.text = upload.username
        holder.uploaderPhone.text = upload.number

        if (!upload.pdfPath.isNullOrEmpty()) {
            holder.viewPdfButton.visibility = View.VISIBLE
            holder.viewPdfButton.setOnClickListener {
                onPdfClick(upload.pdfPath!!)
            }
        } else {
            holder.viewPdfButton.visibility = View.GONE
        }

        // Set OnClickListener for the phone number
        holder.uploaderPhone.setOnClickListener {
            val phoneNumber = holder.uploaderPhone.text.toString()
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$phoneNumber")
            }
            startActivity(holder.itemView.context, intent, null)
        }
    }

    override fun getItemCount(): Int = uploads.size
}
