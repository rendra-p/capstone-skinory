package com.capstone.skinory.ui.notifications.chose

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.capstone.skinory.data.DataRepository
import com.capstone.skinory.data.remote.response.ProductsItem
import kotlinx.coroutines.launch

class SelectProductViewModel(private val repository: DataRepository) : ViewModel() {
    private val _productsResult = MutableLiveData<Result<List<ProductsItem>>>()
    val productsResult: LiveData<Result<List<ProductsItem>>> = _productsResult

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun getProducts(category: String, token: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val result = repository.getProducts(category, token)
                _productsResult.value = Result.success(result)
            } catch (e: Exception) {
                Log.e("GetProductsError", "Error fetching products", e)
                val errorMessage = e.message ?: "Failed to fetch products"
                _productsResult.value = Result.failure(Exception(errorMessage))
            } finally {
                _isLoading.value = false
            }
        }
    }
}