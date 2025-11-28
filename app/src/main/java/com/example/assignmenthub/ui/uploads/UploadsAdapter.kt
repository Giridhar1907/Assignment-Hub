package com.example.assignmenthub.ui.uploads

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.assignmenthub.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class UploadsAdapter(
    private val uploads: MutableList<Upload>
) : RecyclerView.Adapter<UploadsAdapter.UploadViewHolder>() {

    inner class UploadViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val assignmentDescription: TextView = itemView.findViewById(R.id.job_assignment_description)
        val cost: TextView = itemView.findViewById(R.id.job_cost)
        val dueDate: TextView = itemView.findViewById(R.id.job_due_date)
        val pdfIcon: Button = itemView.findViewById(R.id.btn_view_pdf)
        val lockButton: Button = itemView.findViewById(R.id.btn_lock_unlock) // Optional
        val deleteButton: Button = itemView.findViewById(R.id.btn_delete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UploadViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_job, parent, false)
        return UploadViewHolder(view)
    }

    override fun onBindViewHolder(holder: UploadViewHolder, position: Int) {
        val upload = uploads[position]

        holder.assignmentDescription.text = upload.assignmentDescription
        holder.cost.text = "Cost: ${upload.cost}"
        holder.dueDate.text = "Due Date: ${upload.deadline}"

        if (!upload.pdfPath.isNullOrEmpty()) {
            holder.pdfIcon.visibility = View.VISIBLE
            holder.pdfIcon.setOnClickListener { 
                val storageRef = FirebaseStorage.getInstance().getReference(upload.pdfPath!!)
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(uri, "application/pdf")
                        flags = Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_GRANT_READ_URI_PERMISSION
                    }
                    if (intent.resolveActivity(holder.itemView.context.packageManager) != null) {
                        holder.itemView.context.startActivity(intent)
                    } else {
                        val browserIntent = Intent(Intent.ACTION_VIEW, uri)
                        holder.itemView.context.startActivity(browserIntent)
                    }
                }.addOnFailureListener { 
                    Toast.makeText(holder.itemView.context, "Failed to get PDF URL", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            holder.pdfIcon.visibility = View.GONE
        }

        holder.lockButton.text = if (upload.locked) "🔓 Unlock" else "🔒 Lock"

        holder.lockButton.setOnClickListener {
            val newLockStatus = !upload.locked
            val databaseRef = FirebaseDatabase.getInstance().getReference("Jobs")
            val uploadKey = upload.uploadId

            if (uploadKey.isNotEmpty()) {
                databaseRef.child(uploadKey).child("locked").setValue(newLockStatus)
                    .addOnSuccessListener {
                        upload.locked = newLockStatus
                        notifyItemChanged(position)
                        Toast.makeText(holder.itemView.context, "Lock status updated", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(holder.itemView.context, "Failed to update lock status", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(holder.itemView.context, "Upload ID is missing", Toast.LENGTH_SHORT).show()
                Log.e("UploadsAdapter", "Missing uploadId for item at position $position")
            }
        }

        holder.deleteButton.setOnClickListener {
            val uploadKey = upload.uploadId
            if (uploadKey.isNotEmpty()) {
                val currentUser = FirebaseAuth.getInstance().currentUser
                if (currentUser != null && currentUser.uid == upload.uploaderId) {
                    // User is the owner, proceed with deletion
                    upload.pdfPath?.let {
                        val storageRef = FirebaseStorage.getInstance().getReference(it)
                        storageRef.delete().addOnSuccessListener {
                            // File deleted successfully, now delete the database entry
                            deleteJobFromDatabase(uploadKey, holder)
                        }.addOnFailureListener { e ->
                            // Handle failure to delete file
                            Log.e("UploadsAdapter", "Failed to delete PDF: ", e)
                            Toast.makeText(holder.itemView.context, "Failed to delete PDF: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    } ?: deleteJobFromDatabase(uploadKey, holder) // If no PDF, just delete from database
                } else {
                    Toast.makeText(holder.itemView.context, "You do not have permission to delete this job", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(holder.itemView.context, "Upload ID is missing", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun deleteJobFromDatabase(uploadKey: String, holder: UploadViewHolder) {
        val databaseRef = FirebaseDatabase.getInstance().getReference("Jobs")
        databaseRef.child(uploadKey).removeValue()
            .addOnSuccessListener {
                val currentPosition = holder.adapterPosition
                if (currentPosition != RecyclerView.NO_POSITION) {
                    uploads.removeAt(currentPosition)
                    notifyItemRemoved(currentPosition)
                    notifyItemRangeChanged(currentPosition, uploads.size)
                    Toast.makeText(holder.itemView.context, "Job deleted", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Log.e("UploadsAdapter", "Failed to delete job from database: ", e)
                Toast.makeText(holder.itemView.context, "Failed to delete job: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    override fun getItemCount(): Int = uploads.size
}
