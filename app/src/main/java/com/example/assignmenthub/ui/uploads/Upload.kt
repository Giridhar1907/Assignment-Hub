package com.example.assignmenthub.ui.uploads

data class Upload(
    var assignmentDescription: String = "",
    var cost: String = "",
    var deadline: String = "",
    var pdfPath: String? = null, // Changed from pdfUrl
    var locked: Boolean = false,
    var uploadId: String = "",   // Firebase key
    var uploaderId: String = ""  // Firebase user UID
)
