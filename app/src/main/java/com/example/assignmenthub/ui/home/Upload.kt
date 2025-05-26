package com.example.assignmenthub.ui.home

data class Upload(
    val assignmentDescription: String? = null,
    val cost: String? = null,
    val deadline: String? = null,
    val pdfUrl: String? = null,
    val username: String? = null,
    val email: String? = null,
    val regd: String? = null,
    val number: String? = null,
    val uploaderId: String? = null,
    val locked: Boolean = false  // <-- New field
)


