package com.capstone.skinory.ui.notifications.chose

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.capstone.skinory.R
import com.capstone.skinory.databinding.ActivityChoseBinding
import com.capstone.skinory.ui.notifications.day.NotifDayActivity
import com.capstone.skinory.ui.notifications.night.NotifNightActivity

class ChoseActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChoseBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChoseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setAction()
    }

    private fun setAction() {
        binding.btnDay.setOnClickListener {
            startActivity(Intent(this, NotifDayActivity::class.java))
        }
        binding.btnNight.setOnClickListener {
            startActivity(Intent(this, NotifNightActivity::class.java))
        }
    }
}