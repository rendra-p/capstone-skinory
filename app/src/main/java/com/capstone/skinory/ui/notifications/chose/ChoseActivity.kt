package com.capstone.skinory.ui.notifications.chose

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.capstone.skinory.data.Injection
import com.capstone.skinory.databinding.ActivityChoseBinding
import com.capstone.skinory.ui.notifications.RoutineViewModel
import com.capstone.skinory.ui.notifications.day.NotifDayActivity
import com.capstone.skinory.ui.notifications.night.NotifNightActivity

class ChoseActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChoseBinding
    private lateinit var routineViewModel: RoutineViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChoseBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        val viewModelFactory = Injection.provideViewModelFactory(this)
        routineViewModel = ViewModelProvider(this, viewModelFactory)[RoutineViewModel::class.java]

        routineViewModel.fetchRoutines()
        setAction()
    }

    private fun setAction() {
        binding.btnDay.setOnClickListener {
            routineViewModel.dayRoutines.observe(this) { dayRoutines ->
                if (dayRoutines.isNotEmpty()) {
                    Toast.makeText(this, "You already have a Day routine", Toast.LENGTH_SHORT).show()
                } else {
                    startActivity(Intent(this, NotifDayActivity::class.java))
                }
            }
        }
        binding.btnNight.setOnClickListener {
            routineViewModel.nightRoutines.observe(this) { nightRoutines ->
                if (nightRoutines.isNotEmpty()) {
                    Toast.makeText(this, "You already have a Night routine", Toast.LENGTH_SHORT).show()
                } else {
                    startActivity(Intent(this, NotifNightActivity::class.java))
                }
            }
        }
    }
}