package com.capstone.skinory.ui.login

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.capstone.skinory.MainActivity
import com.capstone.skinory.R
import com.capstone.skinory.data.Injection
import com.capstone.skinory.databinding.ActivityLoginBinding
import com.capstone.skinory.ui.analysis.AnalysisActivity
import com.capstone.skinory.ui.register.RegisterActivity
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var viewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val viewModelFactory = Injection.provideViewModelFactory(this)
        viewModel = ViewModelProvider(this, viewModelFactory)[LoginViewModel::class.java]

        setupView()
        setupAction()
        observeLoginResult()
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
        binding.loginButton.setOnClickListener {
            val email = binding.edLoginEmail.text.toString()
            val password = binding.edLoginPassword.text.toString()

            // Validasi input
            if (validateInput(email, password)) {
                viewModel.login(email, password)
            }
        }

        binding.toRegister.setOnClickListener {
            startActivity(Intent(this@LoginActivity, RegisterActivity::class.java))
        }

        viewModel.isLoading.observe(this) { isLoading ->
            binding.loginButton.isEnabled = !isLoading
            if (isLoading) {
                binding.loginButton.text = getString(R.string.loading)
                binding.progressBar.visibility = View.VISIBLE
            } else {
                binding.loginButton.text = getString(R.string.login)
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun validateInput(email: String, password: String): Boolean {
        var isValid = true

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

    private fun observeLoginResult() {
        viewModel.loginResult.observe(this) { result ->
            result.onSuccess { response ->
                // Login berhasil
                Toast.makeText(this, "Login successful: ${response.message}", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, AnalysisActivity::class.java))
                finish()
            }.onFailure { exception ->
                // Tangani error login
                Toast.makeText(this, "Login failed: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}