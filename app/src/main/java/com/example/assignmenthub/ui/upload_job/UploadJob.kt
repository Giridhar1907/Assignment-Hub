package com.example.assignmenthub.ui.upload_job

data class UploadJob(
    val assignmentDescription: String = "",
    val cost: String = "",
    val deadline: String = "",
    val pdfPath: String = "", // Changed from pdfUrl
    val uploaderId: String = ""
)
