package com.capstone.skinory.ui.notifications.day

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.capstone.skinory.R
import com.capstone.skinory.databinding.ActivityNotifDayBinding

class NotifDayActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNotifDayBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotifDayBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}