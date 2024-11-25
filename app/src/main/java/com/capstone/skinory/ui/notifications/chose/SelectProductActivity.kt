package com.capstone.skinory.ui.notifications.chose

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.capstone.skinory.R
import com.capstone.skinory.data.Injection
import com.capstone.skinory.data.remote.response.ProductsItem
import com.capstone.skinory.databinding.ActivitySelectProductBinding
import com.capstone.skinory.ui.login.LoginActivity
import com.capstone.skinory.ui.login.TokenDataStore
import com.capstone.skinory.ui.notifications.day.NotifDayViewModel
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class SelectProductActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySelectProductBinding
    private lateinit var viewModel: SelectProductViewModel
    private lateinit var adapter: ProductAdapter
    private lateinit var tokenDataStore: TokenDataStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenDataStore = TokenDataStore.getInstance(this)

        val category = intent.getStringExtra("CATEGORY") ?: return

        // Setup ViewModel
        val viewModelFactory = Injection.provideViewModelFactory(this)
        viewModel = ViewModelProvider(this, viewModelFactory)[SelectProductViewModel::class.java]

        // Setup RecyclerView
        setupRecyclerView()

        // Observe ViewModel states
        observeViewModelStates()

        // Fetch products
        fetchTokenAndProducts(category)
    }

    private fun observeViewModelStates() {
        // Observe loading state
        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        // Observe products result
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
                handleError(exception)
            }
        }
    }

    private fun fetchTokenAndProducts(category: String) {
        lifecycleScope.launch {
            tokenDataStore.token.collect { token ->
                token?.let {
                    viewModel.getProducts(category, it)
                } ?: run {
                    showTokenError()
                }
            }
        }
    }

    private fun showTokenError() {
        AlertDialog.Builder(this)
            .setTitle("Authentication Error")
            .setMessage("Please log in again to continue.")
            .setPositiveButton("Login") { _, _ ->
                // Redirect to login activity
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            }
            .setCancelable(false)
            .show()
    }

    private fun handleError(exception: Throwable) {
        when (exception) {
            is IOException -> showNetworkError()
            is HttpException -> handleHttpError(exception)
            else -> showGenericError(exception.message)
        }
    }

    private fun showNetworkError() {
        AlertDialog.Builder(this)
            .setTitle("Network Error")
            .setMessage("Please check your internet connection and try again.")
            .setPositiveButton("Retry") { _, _ ->
                fetchTokenAndProducts(intent.getStringExtra("CATEGORY") ?: return@setPositiveButton)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun handleHttpError(httpException: HttpException) {
        val errorBody = httpException.response()?.errorBody()?.string()
        when (httpException.code()) {
            401 -> showTokenError() // Unauthorized
            403 -> showForbiddenError()
            404 -> showNotFoundError()
            500 -> showServerError()
            else -> showGenericError(errorBody)
        }
    }

    private fun showForbiddenError() {
        AlertDialog.Builder(this)
            .setTitle("Access Denied")
            .setMessage("You don't have permission to access this resource.")
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showNotFoundError() {
        binding.emptyStateTextView.apply {
            text = "No products found for this category"
            visibility = View.VISIBLE
        }
        binding.recyclerView.visibility = View.GONE
    }

    private fun showServerError() {
        AlertDialog.Builder(this)
            .setTitle("Server Error")
            .setMessage("Something went wrong on our end. Please try again later.")
            .setPositiveButton("Retry") { _, _ ->
                fetchTokenAndProducts(intent.getStringExtra("CATEGORY") ?: return@setPositiveButton)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showGenericError(message: String?) {
        AlertDialog.Builder(this)
            .setTitle("Error")
            .setMessage(message ?: "An unexpected error occurred")
            .setPositiveButton("OK", null)
            .show()
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
            // Select product and return to previous activity
            val intent = Intent().apply {
                putExtra("PRODUCT_NAME", product.nameProduct)
                putExtra("CATEGORY", product.category)
                putExtra("PRODUCT_ID", product.idProduct)
            }
            setResult(RESULT_OK, intent)
            finish()
        }
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
    }
}