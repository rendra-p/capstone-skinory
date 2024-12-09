package com.capstone.skinory.ui.settings

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.capstone.skinory.data.Injection
import com.capstone.skinory.data.UserPreferences
import com.capstone.skinory.data.remote.response.PasswordRequest
import com.capstone.skinory.databinding.FragmentSettingsBinding
import com.capstone.skinory.ui.customview.ChangePasswordInput
import com.capstone.skinory.ui.login.LoginActivity
import com.capstone.skinory.ui.login.TokenDataStore
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var settingsViewModel: SettingsViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)

        val viewModelFactory = Injection.provideViewModelFactory(requireContext())
        settingsViewModel = ViewModelProvider(this, viewModelFactory)[SettingsViewModel::class.java]

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.linearLayout1.setOnClickListener {
            val changePasswordInput = ChangePasswordInput(requireContext())

            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Change Password")
                .setView(changePasswordInput)
                .setPositiveButton("Save") { _, _ ->
                    if (changePasswordInput.isPasswordValid()) {
                        val newPassword = changePasswordInput.getPassword()
                        if (!changePasswordInput.isErrorEnabled) {
                            lifecycleScope.launch {
                                try {
                                    val token = TokenDataStore.getInstance(requireContext())
                                        .token.first() ?: throw Exception("Token not found")

                                    val result = settingsViewModel.updatePassword(
                                        token,
                                        PasswordRequest(newPassword)
                                    )

                                    result.onSuccess { response ->
                                        Toast.makeText(
                                            requireContext(),
                                            response.message ?: "Password changed successfully",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }.onFailure { error ->
                                        Toast.makeText(
                                            requireContext(),
                                            error.message ?: "Failed to change password",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(
                                        requireContext(),
                                        e.message ?: "Error occurred",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    } else {
                        Toast.makeText(requireContext(), "Invalid password", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        val isDarkModeEnabled = settingsViewModel.isDarkModeEnabled()
        binding.switchDarkMode.isChecked = isDarkModeEnabled

        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            settingsViewModel.setDarkMode(isChecked)

            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }

        binding.linearLayout3.setOnClickListener {
            lifecycleScope.launch {
                TokenDataStore.getInstance(requireContext()).clearToken()

                UserPreferences(requireContext()).apply {
                    removeUserId()
                    setNotificationsEnabled(false)
                }

                val intent = Intent(requireContext(), LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)

                requireActivity().finish()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}