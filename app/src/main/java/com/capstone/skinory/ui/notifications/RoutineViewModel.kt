package com.capstone.skinory.ui.notifications

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.capstone.skinory.data.DataRepository
import com.capstone.skinory.data.remote.response.RoutinesItem
import com.capstone.skinory.ui.login.TokenDataStore
import kotlinx.coroutines.launch

class RoutineViewModel(
    private val repository: DataRepository,
    private val tokenDataStore: TokenDataStore
) : ViewModel() {
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _dayRoutines = MutableLiveData<List<RoutinesItem>>()
    val dayRoutines: LiveData<List<RoutinesItem>> = _dayRoutines

    private val _nightRoutines = MutableLiveData<List<RoutinesItem>>()
    val nightRoutines: LiveData<List<RoutinesItem>> = _nightRoutines

    fun fetchRoutines() {
        viewModelScope.launch {
            _isLoading.value = true
            tokenDataStore.token.collect { token ->
                token?.let {
                    try {
                        val dayResponse = repository.getDayRoutines(it)
                        dayResponse.onSuccess { response ->
                            _dayRoutines.value = response.routines?.mapNotNull { routine ->
                                routine?.copy(applied = "Day")
                            } ?: emptyList()
                        }

                        val nightResponse = repository.getNightRoutines(it)
                        nightResponse.onSuccess { response ->
                            _nightRoutines.value = response.routines?.mapNotNull { routine ->
                                routine?.copy(applied = "Night")
                            } ?: emptyList()
                        }
                    } catch (e: Exception) {
                        Log.e("RoutineViewModel", "Fetch routine error", e)
                    } finally {
                        _isLoading.value = false
                    }
                }
            }
        }
    }

    fun deleteRoutine(isDay: Boolean) {
        viewModelScope.launch {
            _isLoading.value = true
            tokenDataStore.token.collect { token ->
                token?.let {
                    try {
                        val result = if (isDay) {
                            repository.deleteDayRoutine(token)
                        } else {
                            repository.deleteNightRoutine(token)
                        }
                        result.onSuccess {
                            fetchRoutines()
                        }.onFailure { exception ->
                            Log.e("RoutineViewModel", "Delete routine error", exception)
                        }
                    } catch (e: Exception) {
                        Log.e("RoutineViewModel", "Delete routine error", e)
                    } finally {
                        _isLoading.value = false
                    }
                }
            }
        }
    }
}