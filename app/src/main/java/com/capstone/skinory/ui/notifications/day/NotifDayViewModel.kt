package com.capstone.skinory.ui.notifications.day

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class NotifDayViewModel : ViewModel() {
    private val _selectedProducts = MutableLiveData<MutableMap<String, Int>>(mutableMapOf())

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun setSelectedProduct(category: String, productId: Int?) {
        productId?.let {
            _selectedProducts.value?.put(category, it)
        }
    }
}