package com.capstone.skinory.ui.result

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.capstone.skinory.data.DataRepository
import com.capstone.skinory.data.remote.response.BestProductsItem
import com.capstone.skinory.ui.login.TokenDataStore
import kotlinx.coroutines.launch

class ResultViewModel(
    private val repository: DataRepository,
    private val tokenDataStore: TokenDataStore
) : ViewModel() {
    private val _bestProducts = MutableLiveData<List<BestProductsItem>>()
    val bestProducts: LiveData<List<BestProductsItem>> = _bestProducts

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun fetchBestProducts() {
        viewModelScope.launch {
            tokenDataStore.token.collect { token ->
                token?.let {
                    _isLoading.value = true
                    try {
                        val response = repository.getBestProduct(it)
                        response.onSuccess { bestProductResponse ->
                            val products = bestProductResponse.bestProducts?.filterNotNull() ?: emptyList()

                            Log.d("ResultViewModel", "Received products: ${products.size}")
                            _bestProducts.value = products
                        }.onFailure { exception ->
                            _error.value = exception.message ?: "Unknown error"
                        }
                    } catch (e: Exception) {
                        _error.value = e.message ?: "Unknown error"
                    } finally {
                        _isLoading.value = false
                    }
                }
            }
        }
    }
}