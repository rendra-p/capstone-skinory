package com.capstone.skinory.ui.login

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.capstone.skinory.databinding.ActivityErrorBinding

class ErrorActivity : AppCompatActivity() {
    private lateinit var binding: ActivityErrorBinding

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityErrorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val errorType = intent.getStringExtra("error_type")
        val errorMessage = intent.getStringExtra("error_message")

        when (errorType) {
            "no_internet" -> {
                binding.errorTitle.text = "No Internet Connection"
                binding.errorMessage.text = "Please check your internet connection"
            }
            "unknown" -> {
                binding.errorTitle.text = "Error occurred"
                binding.errorMessage.text = errorMessage ?: "An unknown error occurred"
            }
        }

        binding.retryButton.setOnClickListener {
            startActivity(Intent(this, Splash::class.java))
            finish()
        }
    }
}