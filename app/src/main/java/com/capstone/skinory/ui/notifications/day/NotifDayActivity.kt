package com.capstone.skinory.ui.notifications.day

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.capstone.skinory.MainActivity
import com.capstone.skinory.R
import com.capstone.skinory.data.Injection
import com.capstone.skinory.databinding.ActivityNotifDayBinding
import com.capstone.skinory.ui.ViewModelFactory
import com.capstone.skinory.ui.notifications.chose.SelectProductActivity

class NotifDayActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNotifDayBinding
    private lateinit var viewModel: NotifDayViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotifDayBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup ViewModel
        val viewModelFactory = Injection.provideViewModelFactory(this)
        viewModel = ViewModelProvider(this, viewModelFactory)[NotifDayViewModel::class.java]

        // Set click listeners for select buttons
        setupButtonListeners()
    }

    private fun setupButtonListeners() {
        // Face Wash
        binding.btnFacewash.setOnClickListener {
            navigateToSelectProduct("facewash")
        }

        // Sunscreen
        binding.button3.setOnClickListener {
            navigateToSelectProduct("sunscreen")
        }

        // Moisturizer
        binding.button4.setOnClickListener {
            navigateToSelectProduct("moisturizer")
        }

        // Toner
        binding.button5.setOnClickListener {
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
                "sunscreen" -> {
                    binding.button3.text = productName
                    viewModel.setSelectedProduct("sunscreen", productId)
                }
                "moisturizer" -> {
                    binding.button4.text = productName
                    viewModel.setSelectedProduct("moisturizer", productId)
                }
                "toner" -> {
                    binding.button5.text = productName
                    viewModel.setSelectedProduct("toner", productId)
                }
            }
        }
    }

    companion object {
        private const val REQUEST_CODE_SELECT_PRODUCT = 1001
    }
}