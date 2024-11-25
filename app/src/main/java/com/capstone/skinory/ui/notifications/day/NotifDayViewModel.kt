package com.capstone.skinory.ui.notifications.day

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.capstone.skinory.data.DataRepository
import com.capstone.skinory.data.remote.response.ProductsItem
import kotlinx.coroutines.launch

class NotifDayViewModel(private val repository: DataRepository) : ViewModel() {
    private val _selectedProducts = MutableLiveData<MutableMap<String, Int>>(mutableMapOf())

    private val _saveRoutineResult = MutableLiveData<Result<Boolean>>()
    val saveRoutineResult: LiveData<Result<Boolean>> = _saveRoutineResult

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun setSelectedProduct(category: String, productId: Int?) {
        productId?.let {
            _selectedProducts.value?.put(category, it)
        }
    }

    fun saveRoutine() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val result = repository.saveRoutineDay(_selectedProducts.value ?: mutableMapOf())
                _saveRoutineResult.value = Result.success(result)
            } catch (e: Exception) {
                val errorMessage = e.message ?: "Failed to save routine"
                _saveRoutineResult.value = Result.failure(Exception(errorMessage))
            } finally {
                _isLoading.value = false
            }
        }
    }
}