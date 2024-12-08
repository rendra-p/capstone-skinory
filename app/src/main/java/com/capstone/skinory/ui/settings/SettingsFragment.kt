package com.capstone.skinory.ui.settings

import android.content.Intent
import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.capstone.skinory.R
import com.capstone.skinory.data.Injection
import com.capstone.skinory.data.UserPreferences
import com.capstone.skinory.data.remote.response.PasswordRequest
import com.capstone.skinory.databinding.FragmentResultBinding
import com.capstone.skinory.databinding.FragmentSettingsBinding
import com.capstone.skinory.ui.customview.PasswordInputLayout
import com.capstone.skinory.ui.login.LoginActivity
import com.capstone.skinory.ui.login.TokenDataStore
import com.capstone.skinory.ui.result.ResultViewModel
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

        // Reset Password Click
        binding.linearLayout1.setOnClickListener {
            val passwordInputLayout = PasswordInputLayout(requireContext())
            val passwordEditText = passwordInputLayout.editText

            // Tampilkan dialog untuk input password
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Ubah Password")
                .setView(passwordInputLayout)
                .setPositiveButton("Simpan") { _, _ ->
                    val newPassword = passwordEditText?.text.toString()

                    // Validasi password
                    if (!passwordInputLayout.isErrorEnabled) {
                        lifecycleScope.launch {
                            try {
                                // Ambil token dari TokenDataStore
                                val token = TokenDataStore.getInstance(requireContext())
                                    .token.first() ?: throw Exception("Token not found")

                                // Kirim password baru ke repository
                                val result = settingsViewModel.updatePassword(
                                    token,
                                    PasswordRequest(newPassword)
                                )

                                // Tampilkan pesan berhasil
                                result.onSuccess { response ->
                                    Toast.makeText(
                                        requireContext(),
                                        response.message ?: "Password berhasil diubah",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }.onFailure { error ->
                                    Toast.makeText(
                                        requireContext(),
                                        error.message ?: "Gagal mengubah password",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(
                                    requireContext(),
                                    e.message ?: "Terjadi kesalahan",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }
                .setNegativeButton("Batal", null)
                .show()
        }

        // Dark Mode Switch
        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            // Simpan preferensi dark mode
            settingsViewModel.setDarkMode(isChecked)

            // Terapkan tema
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }

        // Logout Click
        binding.linearLayout3.setOnClickListener {
            lifecycleScope.launch {
                // Hapus token
                TokenDataStore.getInstance(requireContext()).clearToken()

                // Hapus data user preferences
                UserPreferences(requireContext()).apply {
                    removeUserId()
                    setNotificationsEnabled(false)
                }

                // Navigasi ke LoginActivity
                val intent = Intent(requireContext(), LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)

                // Tutup fragment/activity saat ini
                requireActivity().finish()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}