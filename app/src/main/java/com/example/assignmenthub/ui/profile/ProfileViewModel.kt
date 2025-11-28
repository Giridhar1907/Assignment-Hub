package com.example.assignmenthub.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

data class UserProfile(
    var username: String? = null,
    var number: String? = null,
    var email: String? = null
)


class ProfileViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()
    private val userRef: DatabaseReference?

    private val _userProfile = MutableLiveData<UserProfile>()
    val userProfile: LiveData<UserProfile> get() = _userProfile

    init {
        userRef = auth.currentUser?.uid?.let { uid ->
            database.reference.child("users").child(uid)
        }
        fetchUserProfile()
    }

    private fun fetchUserProfile() {
        userRef?.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val profile = snapshot.getValue(UserProfile::class.java)
                _userProfile.value = profile
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle errors here if needed
            }
        })
    }

    fun saveUserProfile(profile: UserProfile, onComplete: (Boolean) -> Unit) {
        userRef?.setValue(profile)
            ?.addOnSuccessListener { onComplete(true) }
            ?.addOnFailureListener { onComplete(false) }
    }
}
