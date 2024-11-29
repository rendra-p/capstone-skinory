package com.capstone.skinory.ui.notifications.night

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.capstone.skinory.data.DataRepository

class NotifNightViewModel(private val repository: DataRepository) : ViewModel() {
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
}