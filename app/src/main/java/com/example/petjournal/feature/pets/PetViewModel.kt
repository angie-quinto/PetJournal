package com.example.petjournal.feature.pets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.petjournal.domain.model.Pet
import com.example.petjournal.domain.model.PetInput
import com.example.petjournal.domain.repository.PetRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PetViewModel(
    private val petRepository: PetRepository
) : ViewModel() {

    val pets: StateFlow<List<Pet>> = petRepository.observeActivePets()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun createPet(input: PetInput, onComplete: (String) -> Unit) {
        viewModelScope.launch {
            val id = petRepository.createPet(input)
            onComplete(id)
        }
    }

    fun updatePet(id: String, input: PetInput, onComplete: () -> Unit) {
        viewModelScope.launch {
            petRepository.updatePet(id, input)
            onComplete()
        }
    }

    fun archivePet(id: String) {
        viewModelScope.launch {
            petRepository.archivePet(id)
        }
    }

    companion object {
        fun provideFactory(petRepository: PetRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return PetViewModel(petRepository) as T
                }
            }
    }
}
