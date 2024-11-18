package com.capstone.skinory.ui.register

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.capstone.skinory.R
import com.capstone.skinory.data.Injection
import com.capstone.skinory.databinding.ActivityRegisterBinding
import com.capstone.skinory.ui.ViewModelFactory
import com.capstone.skinory.ui.login.LoginActivity

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var viewModel: RegisterViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val viewModelFactory = Injection.provideViewModelFactory(this)
        viewModel = ViewModelProvider(this, viewModelFactory)[RegisterViewModel::class.java]

        setupView()
        setupAction()
        observeRegistrationResult()
    }

    private fun setupView() {
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        supportActionBar?.hide()
    }

    private fun setupAction() {
        binding.registerButton.setOnClickListener {
            val username = binding.edRegisterName.text.toString()
            val email = binding.edRegisterEmail.text.toString()
            val password = binding.edRegisterPassword.text.toString()

            // Validasi input
            if (validateInput(username, email, password)) {
                viewModel.registerUser(username, email, password)
            }
        }
        viewModel.isLoading.observe(this) { isLoading ->
            binding.registerButton.isEnabled = !isLoading
            if (isLoading) {
                binding.registerButton.text = getString(R.string.loading)
                binding.progressBar.visibility = View.VISIBLE
            } else {
                binding.registerButton.text = getString(R.string.register)
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun validateInput(name: String, email: String, password: String): Boolean {
        var isValid = true

        if (name.isEmpty()) {
            binding.nameEditTextLayout.error = "Name cannot be empty"
            isValid = false
        }

        if (email.isEmpty()) {
            binding.emailEditTextLayout.error = "Email cannot be empty"
            isValid = false
        }

        if (password.isEmpty() || password.length < 8) {
            binding.passwordEditTextLayout.error = "Password must be at least 8 characters"
            isValid = false
        }

        return isValid
    }

    private fun observeRegistrationResult() {
        viewModel.registrationResult.observe(this) { result ->
            result.onSuccess { response ->
                if (response.error == false) {
                    // Registrasi berhasil
                    Toast.makeText(this, response.message, Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                } else {
                    // Registrasi gagal
                    Toast.makeText(this, response.message, Toast.LENGTH_SHORT).show()
                }
            }.onFailure { exception ->
                // Tangani error jaringan atau lainnya
                Toast.makeText(this, "Registration failed: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}