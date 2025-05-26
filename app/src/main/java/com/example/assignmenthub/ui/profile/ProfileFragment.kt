package com.example.assignmenthub.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.assignmenthub.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var profileViewModel: ProfileViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        profileViewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)

        // Observe user profile LiveData
        profileViewModel.userProfile.observe(viewLifecycleOwner) { profile ->
            profile?.let {
                binding.editName.setText(it.username)
                binding.editReg.setText(it.regd)
                binding.editPhone.setText(it.number)
                binding.editEmail.setText(it.email)

            }
        }

        binding.btnSave.setOnClickListener {
            val updatedProfile = UserProfile(
                username = binding.editName.text.toString(),
                regd = binding.editReg.text.toString(),
                number = binding.editPhone.text.toString(),
                email = binding.editEmail.text.toString()
            )
            profileViewModel.saveUserProfile(updatedProfile) { success ->
                if (success) {
                    Toast.makeText(requireContext(), "Profile updated", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Update failed", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.btnCancel.setOnClickListener {
            // Optionally reload profile or clear fields
            profileViewModel.userProfile.value?.let {
                binding.editName.setText(it.username)
                binding.editReg.setText(it.regd)
                binding.editPhone.setText(it.number)
                binding.editEmail.setText(it.email)
            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
