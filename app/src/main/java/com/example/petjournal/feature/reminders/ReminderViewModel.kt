package com.example.petjournal.feature.reminders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.petjournal.domain.model.CareReminder
import com.example.petjournal.domain.model.ReminderInput
import com.example.petjournal.domain.repository.ReminderRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ReminderViewModel(
    private val reminderRepository: ReminderRepository
) : ViewModel() {

    val reminders: StateFlow<List<CareReminder>> = reminderRepository.observeReminders()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun saveReminder(input: ReminderInput, onComplete: (String) -> Unit) {
        viewModelScope.launch {
            onComplete(reminderRepository.saveReminder(input))
        }
    }

    fun completeReminder(id: String, onComplete: (CareReminder?) -> Unit = {}) {
        viewModelScope.launch {
            onComplete(reminderRepository.completeReminder(id, System.currentTimeMillis()))
        }
    }

    companion object {
        fun provideFactory(reminderRepository: ReminderRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ReminderViewModel(reminderRepository) as T
                }
            }
    }
}
