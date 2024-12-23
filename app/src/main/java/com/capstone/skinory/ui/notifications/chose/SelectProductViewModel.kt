package com.capstone.skinory.ui.notifications.chose

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.capstone.skinory.data.DataRepository
import com.capstone.skinory.data.remote.response.ProductsItem
import kotlinx.coroutines.launch

class SelectProductViewModel(private val repository: DataRepository) : ViewModel() {
    private val _productsResult = MutableLiveData<Result<List<ProductsItem>>>()
    val productsResult: LiveData<Result<List<ProductsItem>>> = _productsResult

    private var selectedProduct: ProductsItem? = null

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _saveRoutineDayResult = MutableLiveData<Result<Void?>>()
    val saveRoutineDayResult: LiveData<Result<Void?>> = _saveRoutineDayResult

    private val _saveRoutineNightResult = MutableLiveData<Result<Void?>>()
    val saveRoutineNightResult: LiveData<Result<Void?>> = _saveRoutineNightResult

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

    fun saveRoutineDay(category: String, productId: Int, selectedProducts: Map<String, Int>, token: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val result = repository.saveRoutineDay(category, productId, selectedProducts, token)
                _saveRoutineDayResult.value = result
                selectedProduct = _productsResult.value?.getOrNull()
                    ?.find { it.idProduct == productId && it.category == category }
            } catch (e: Exception) {
                _saveRoutineDayResult.value = Result.failure(e)
                Log.e("SaveRoutineDay", "Error saving routine day", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun saveRoutineNight(category: String, productId: Int, selectedProducts: Map<String, Int>, token: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val result = repository.saveRoutineNight(category, productId, selectedProducts, token)
                _saveRoutineNightResult.value = result
                selectedProduct = _productsResult.value?.getOrNull()
                    ?.find { it.idProduct == productId && it.category == category }
            } catch (e: Exception) {
                _saveRoutineNightResult.value = Result.failure(e)
                Log.e("SaveRoutineNight", "Error saving routine night", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getSelectedProduct(category: String): ProductsItem? {
        return selectedProduct
    }
}