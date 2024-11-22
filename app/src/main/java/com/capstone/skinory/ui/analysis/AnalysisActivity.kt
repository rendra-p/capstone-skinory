package com.capstone.skinory.ui.analysis

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.capstone.skinory.databinding.ActivityAnalysisBinding
import com.capstone.skinory.ui.home.HomeFragment

class AnalysisActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAnalysisBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAnalysisBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnAnalysis.setOnClickListener {
            startActivity(Intent(this, HomeFragment::class.java))
        }
    }
}