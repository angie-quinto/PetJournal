package com.example.petjournal.feature.journal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import android.net.Uri
import com.example.petjournal.domain.model.JournalAttachment
import com.example.petjournal.domain.model.JournalEntry
import com.example.petjournal.domain.model.JournalEntryInput
import com.example.petjournal.domain.repository.AttachmentRepository
import com.example.petjournal.domain.repository.JournalRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.IOException

class JournalViewModel(
    private val journalRepository: JournalRepository,
    private val attachmentRepository: AttachmentRepository,
    private val petId: String
) : ViewModel() {

    val entries: StateFlow<List<JournalEntry>> = journalRepository.observeEntries(petId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val attachments: StateFlow<List<JournalAttachment>> = attachmentRepository.observeForPet(petId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _attachmentError = MutableStateFlow<String?>(null)
    val attachmentError: StateFlow<String?> = _attachmentError.asStateFlow()

    fun saveEntry(input: JournalEntryInput, photoUris: List<Uri> = emptyList(), onComplete: () -> Unit) {
        viewModelScope.launch {
            _attachmentError.value = null
            val entryId = journalRepository.saveEntry(input)
            try {
                photoUris.forEach { uri ->
                    attachmentRepository.importJournalAttachment(input.petId, entryId, uri)
                }
                onComplete()
            } catch (error: IOException) {
                _attachmentError.value = error.message ?: "The entry was saved, but one or more photos could not be attached."
                onComplete()
            }
        }
    }

    fun clearAttachmentError() {
        _attachmentError.value = null
    }

    fun deleteEntry(id: String) {
        viewModelScope.launch {
            journalRepository.deleteEntry(id)
            attachmentRepository.deleteAttachmentsForEntry(id)
        }
    }

    fun deleteAttachment(id: String) {
        viewModelScope.launch {
            attachmentRepository.deleteJournalAttachment(id)
        }
    }

    companion object {
        fun provideFactory(
            journalRepository: JournalRepository,
            attachmentRepository: AttachmentRepository,
            petId: String
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return JournalViewModel(journalRepository, attachmentRepository, petId) as T
            }
        }
    }
}
