package com.example.petjournal.feature.health

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.petjournal.domain.model.*
import com.example.petjournal.domain.repository.HealthRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HealthViewModel(
    private val healthRepository: HealthRepository,
    private val petId: String
) : ViewModel() {

    val events: StateFlow<List<HealthEvent>> = healthRepository.observeEvents(petId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val weightHistory: StateFlow<List<WeightRecord>> = healthRepository.observeWeightHistory(petId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun saveEvent(input: HealthEventInput, onComplete: () -> Unit) {
        viewModelScope.launch {
            healthRepository.saveEvent(input)
            onComplete()
        }
    }

    fun deleteEvent(id: String) {
        viewModelScope.launch {
            healthRepository.deleteEvent(id)
        }
    }

    fun recordWeight(input: WeightInput, onComplete: () -> Unit) {
        viewModelScope.launch {
            healthRepository.recordWeight(input)
            onComplete()
        }
    }

    companion object {
        fun provideFactory(
            healthRepository: HealthRepository,
            petId: String
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return HealthViewModel(healthRepository, petId) as T
            }
        }
    }
}
