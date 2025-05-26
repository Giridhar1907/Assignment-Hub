package com.example.assignmenthub.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.assignmenthub.R

class UploadsAdapter(
    private val uploads: List<Upload>,
    private val onPdfClick: (String) -> Unit
) : RecyclerView.Adapter<UploadsAdapter.UploadViewHolder>() {

    inner class UploadViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val description: TextView = view.findViewById(R.id.job_assignment_description)
        val cost: TextView = view.findViewById(R.id.job_cost)
        val deadline: TextView = view.findViewById(R.id.job_due_date)
        val uploaderName: TextView = view.findViewById(R.id.uploader_name)
        val uploaderEmail: TextView = view.findViewById(R.id.uploader_email)
        val uploaderRegd: TextView = view.findViewById(R.id.uploader_reg_number)
        val uploaderNumber: TextView = view.findViewById(R.id.uploader_phone)
        val viewPdfButton: Button = view.findViewById(R.id.btn_view_pdf)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UploadViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_jobs, parent, false)
        return UploadViewHolder(view)
    }

    override fun onBindViewHolder(holder: UploadViewHolder, position: Int) {
        val upload = uploads[position]
        holder.description.text = "Description: ${upload.assignmentDescription}"
        holder.cost.text = "Cost: ${upload.cost}"
        holder.deadline.text = "Deadline: ${upload.deadline}"
        holder.uploaderName.text = "Name: ${upload.username}"
        holder.uploaderEmail.text = "Email: ${upload.email}"
        holder.uploaderRegd.text = "Reg No: ${upload.regd}"
        holder.uploaderNumber.text = "Phone: ${upload.number}"

        if (!upload.pdfUrl.isNullOrEmpty()) {
            holder.viewPdfButton.visibility = View.VISIBLE
            holder.viewPdfButton.setOnClickListener {
                onPdfClick(upload.pdfUrl!!)
            }
        } else {
            holder.viewPdfButton.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = uploads.size
}
