package com.capstone.skinory.ui.notifications.chose

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.capstone.skinory.data.Injection
import com.capstone.skinory.databinding.ActivitySelectProductBinding
import com.capstone.skinory.ui.login.TokenDataStore
import kotlinx.coroutines.launch

class SelectProductActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySelectProductBinding
    private lateinit var viewModel: SelectProductViewModel
    private lateinit var adapter: ProductAdapter
    private lateinit var tokenDataStore: TokenDataStore
    private lateinit var routineType: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenDataStore = TokenDataStore.getInstance(this)

        val category = intent.getStringExtra("CATEGORY") ?: return
        routineType = intent.getStringExtra("ROUTINE_TYPE") ?: "day"

        val viewModelFactory = Injection.provideViewModelFactory(this)
        viewModel = ViewModelProvider(this, viewModelFactory)[SelectProductViewModel::class.java]

        setupRecyclerView()
        observeViewModelStates()
        fetchTokenAndProducts(category)
    }

    private fun observeViewModelStates() {
        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.productsResult.observe(this) { result ->
            result.onSuccess { products ->
                if (products.isNotEmpty()) {
                    adapter.submitList(products)
                    binding.emptyStateTextView.visibility = View.GONE
                    binding.recyclerView.visibility = View.VISIBLE
                } else {
                    showEmptyState()
                }
            }.onFailure { exception ->
                Toast.makeText(this, "An error has occurred: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
        }

        if (routineType == "day") {
            viewModel.saveRoutineDayResult.observe(this) { result ->
                result.onSuccess {
                    Toast.makeText(this, "Routine day saved successfully", Toast.LENGTH_SHORT).show()
                    finish()
                }.onFailure { exception ->
                    Toast.makeText(this, "Failed to save routine day: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            viewModel.saveRoutineNightResult.observe(this) { result ->
                result.onSuccess {
                    Toast.makeText(this, "Routine night saved successfully", Toast.LENGTH_SHORT).show()
                    finish()
                }.onFailure { exception ->
                    Toast.makeText(this, "Failed to save routine night: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun fetchTokenAndProducts(category: String) {
        lifecycleScope.launch {
            tokenDataStore.token.collect { token ->
                token?.let {
                    viewModel.getProducts(category, it)
                }
            }
        }
    }

    private fun showEmptyState() {
        binding.emptyStateTextView.apply {
            text = "No products available"
            visibility = View.VISIBLE
        }
        binding.recyclerView.visibility = View.GONE
    }

    private fun setupRecyclerView() {
        adapter = ProductAdapter { product ->
            lifecycleScope.launch {
                tokenDataStore.token.collect { token ->
                    token?.let {
                        if (product.category != null && product.idProduct != null) {
                            val selectedProducts = mapOf(product.category to product.idProduct)

                            if (routineType == "day") {
                                viewModel.saveRoutineDay(
                                    category = product.category,
                                    productId = product.idProduct,
                                    selectedProducts = selectedProducts,
                                    it
                                )
                            } else {
                                viewModel.saveRoutineNight(
                                    category = product.category,
                                    productId = product.idProduct,
                                    selectedProducts = selectedProducts,
                                    it
                                )
                            }

                            val resultIntent = Intent().apply {
                                putExtra("PRODUCT_NAME", "Selected")
                                putExtra("CATEGORY", product.category)
                                putExtra("PRODUCT_ID", product.idProduct)
                            }
                            setResult(RESULT_OK, resultIntent)
                            finish()
                        } else {
                            Toast.makeText(this@SelectProductActivity, "Product category or ID is null", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
    }
}