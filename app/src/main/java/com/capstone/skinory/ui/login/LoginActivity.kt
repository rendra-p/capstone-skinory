package com.capstone.skinory.ui.login

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModelProvider
import com.capstone.skinory.R
import com.capstone.skinory.data.Injection
import com.capstone.skinory.data.UserPreferences
import com.capstone.skinory.databinding.ActivityLoginBinding
import com.capstone.skinory.ui.MainActivity
import com.capstone.skinory.ui.analysis.AnalysisActivity
import com.capstone.skinory.ui.register.RegisterActivity

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var viewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val userPreferences = UserPreferences(this)
        if (userPreferences.isDarkModeEnabled()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        val viewModelFactory = Injection.provideViewModelFactory(this)
        viewModel = ViewModelProvider(this, viewModelFactory)[LoginViewModel::class.java]

        setupView()
        setupAction()
        observeLoginResult()
        observeProfileResult()
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

        if (password.isEmpty()) {
            binding.passwordEditTextLayout.error = "Password cannot be empty"
            isValid = false
        }

        return isValid
    }

    private fun observeLoginResult() {
        viewModel.loginResult.observe(this) { result ->
            result.onSuccess {
                Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
            }.onFailure { exception ->
                Toast.makeText(this, "Login failed: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun observeProfileResult() {
        viewModel.profileResult.observe(this) { result ->
            result.onSuccess { profileResponse ->
                val intent = if (profileResponse.profile?.skinType.isNullOrEmpty()) {
                    Intent(this, AnalysisActivity::class.java)
                } else {
                    Intent(this, MainActivity::class.java)
                }
                startActivity(intent)
                finish()
            }.onFailure { exception ->
                Toast.makeText(this, "Failed to fetch profile: ${exception.message}", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, AnalysisActivity::class.java))
                finish()
            }
        }
    }
}