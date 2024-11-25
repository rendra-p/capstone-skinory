package com.capstone.skinory.ui.notifications

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
    private val _dayRoutines = MutableLiveData<List<RoutinesItem>>()
    val dayRoutines: LiveData<List<RoutinesItem>> = _dayRoutines

    private val _nightRoutines = MutableLiveData<List<RoutinesItem>>()
    val nightRoutines: LiveData<List<RoutinesItem>> = _nightRoutines

    fun fetchRoutines() {
        viewModelScope.launch {
            tokenDataStore.token.collect { token ->
                token?.let {
                    try {
                        // Fetch day routines
                        val dayResponse = repository.getDayRoutines(it)
                        dayResponse.onSuccess { response ->
                            _dayRoutines.value = response.routines?.map { routine ->
                                routine?.copy(applied = "Day") // Secara eksplisit set applied ke "Day"
                            }?.filterNotNull() ?: emptyList()
                        }

                        // Fetch night routines
                        val nightResponse = repository.getNightRoutines(it)
                        nightResponse.onSuccess { response ->
                            _nightRoutines.value = response.routines?.map { routine ->
                                routine?.copy(applied = "Night") // Secara eksplisit set applied ke "Night"
                            }?.filterNotNull() ?: emptyList()
                        }
                    } catch (e: Exception) {
                        // Handle error
                    }
                }
            }
        }
    }
}