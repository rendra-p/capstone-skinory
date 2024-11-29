package com.capstone.skinory.ui.notifications.night

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.capstone.skinory.MainActivity
import com.capstone.skinory.R
import com.capstone.skinory.data.Injection
import com.capstone.skinory.databinding.ActivityNotifNightBinding
import com.capstone.skinory.ui.notifications.chose.SelectProductActivity
import com.capstone.skinory.ui.notifications.day.NotifDayActivity
import com.capstone.skinory.ui.notifications.day.NotifDayActivity.Companion

class NotifNightActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNotifNightBinding
    private lateinit var viewModel: NotifNightViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotifNightBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup ViewModel
        val viewModelFactory = Injection.provideViewModelFactory(this)
        viewModel = ViewModelProvider(this, viewModelFactory)[NotifNightViewModel::class.java]

        // Set click listeners for select buttons
        setupButtonListeners()
    }

    private fun setupButtonListeners() {
        // Face Wash
        binding.btnFacewash.setOnClickListener {
            navigateToSelectProduct("facewash")
        }

        // Moisturizer
        binding.button3.setOnClickListener {
            navigateToSelectProduct("moisturizer")
        }

        // Toner
        binding.button4.setOnClickListener {
            navigateToSelectProduct("toner")
        }

        // Save button
        binding.button6.setOnClickListener {
            navigateToMainActivity()
        }
    }

    private fun navigateToSelectProduct(category: String) {
        val intent = Intent(this, SelectProductActivity::class.java).apply {
            putExtra("CATEGORY", category)
            putExtra("ROUTINE_TYPE", "night")
        }
        startActivityForResult(intent, REQUEST_CODE_SELECT_PRODUCT)
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        startActivity(intent)
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SELECT_PRODUCT && resultCode == RESULT_OK) {
            val productName = data?.getStringExtra("PRODUCT_NAME")
            val category = data?.getStringExtra("CATEGORY")
            val productId = data?.getIntExtra("PRODUCT_ID", -1)

            when (category) {
                "facewash" -> {
                    binding.btnFacewash.text = productName
                    viewModel.setSelectedProduct("facewash", productId)
                }
                "moisturizer" -> {
                    binding.button3.text = productName
                    viewModel.setSelectedProduct("moisturizer", productId)
                }
                "toner" -> {
                    binding.button4.text = productName
                    viewModel.setSelectedProduct("toner", productId)
                }
            }
        }
    }

    companion object {
        private const val REQUEST_CODE_SELECT_PRODUCT = 1002
    }
}