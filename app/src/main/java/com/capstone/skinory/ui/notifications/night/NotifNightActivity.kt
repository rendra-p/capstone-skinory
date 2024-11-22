package com.capstone.skinory.ui.notifications.night

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.capstone.skinory.R
import com.capstone.skinory.databinding.ActivityNotifNightBinding

class NotifNightActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNotifNightBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotifNightBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}