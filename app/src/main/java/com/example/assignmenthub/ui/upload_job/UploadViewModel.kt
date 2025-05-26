package com.example.assignmenthub.ui.upload_job

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class UploadViewModel : ViewModel() {
    private val _pdfUri = MutableLiveData<Uri?>()
    val pdfUri: LiveData<Uri?> = _pdfUri

    fun setPdfUri(uri: Uri?) {
        _pdfUri.value = uri
    }
}
