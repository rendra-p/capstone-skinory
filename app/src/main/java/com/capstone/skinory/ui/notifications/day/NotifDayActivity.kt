package com.capstone.skinory.ui.notifications.day

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.capstone.skinory.ui.MainActivity
import com.capstone.skinory.data.Injection
import com.capstone.skinory.databinding.ActivityNotifDayBinding
import com.capstone.skinory.ui.notifications.chose.SelectProductActivity

class NotifDayActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNotifDayBinding
    private lateinit var viewModel: NotifDayViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotifDayBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val viewModelFactory = Injection.provideViewModelFactory(this)
        viewModel = ViewModelProvider(this, viewModelFactory)[NotifDayViewModel::class.java]

        setupButtonListeners()
    }

    private fun setupButtonListeners() {
        binding.btnFacewash.setOnClickListener {
            navigateToSelectProduct("facewash")
        }

        binding.btnSunscreen.setOnClickListener {
            navigateToSelectProduct("sunscreen")
        }

        binding.btnMoisturizer.setOnClickListener {
            navigateToSelectProduct("moisturizer")
        }

        binding.btnToner.setOnClickListener {
            navigateToSelectProduct("toner")
        }

        binding.btnDone.setOnClickListener {
            navigateToMainActivity()
        }
    }

    private fun navigateToSelectProduct(category: String) {
        val intent = Intent(this, SelectProductActivity::class.java).apply {
            putExtra("CATEGORY", category)
            putExtra("ROUTINE_TYPE", "day")
        }
        startActivityForResult(intent, REQUEST_CODE_SELECT_PRODUCT)
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra("navigate_to", "notifications")
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
                    binding.btnFacewash.isEnabled = false
                    viewModel.setSelectedProduct("facewash", productId)
                }
                "sunscreen" -> {
                    binding.btnSunscreen.text = productName
                    binding.btnSunscreen.isEnabled = false
                    viewModel.setSelectedProduct("sunscreen", productId)
                }
                "moisturizer" -> {
                    binding.btnMoisturizer.text = productName
                    binding.btnMoisturizer.isEnabled = false
                    viewModel.setSelectedProduct("moisturizer", productId)
                }
                "toner" -> {
                    binding.btnToner.text = productName
                    binding.btnToner.isEnabled = false
                    viewModel.setSelectedProduct("toner", productId)
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    companion object {
        private const val REQUEST_CODE_SELECT_PRODUCT = 1001
    }
}