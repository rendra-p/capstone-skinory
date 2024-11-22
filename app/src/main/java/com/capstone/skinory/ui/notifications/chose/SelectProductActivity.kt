package com.capstone.skinory.ui.notifications.chose

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.capstone.skinory.R
import com.capstone.skinory.databinding.ActivitySelectProductBinding

class SelectProductActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySelectProductBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

    }
}