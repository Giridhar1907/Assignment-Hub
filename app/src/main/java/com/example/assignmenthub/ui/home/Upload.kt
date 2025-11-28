package com.example.assignmenthub.ui.home

data class Upload(
    // Fields from the "Jobs" node in Firebase
    val assignmentDescription: String? = null,
    val cost: String? = null,
    val deadline: String? = null,
    val pdfPath: String? = null, // Corrected from pdfUrl
    val uploadId: String? = null,
    val uploaderId: String? = null,
    val locked: Boolean = false,

    // Fields from the "users" node, populated after the initial fetch
    val username: String? = null,
    val email: String? = null,
    val regd: String? = null,
    val number: String? = null
)
