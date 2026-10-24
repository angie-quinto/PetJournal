package com.example.petjournal.domain.repository

import android.net.Uri
import com.example.petjournal.domain.model.JournalAttachment
import kotlinx.coroutines.flow.Flow

interface AttachmentRepository {
    fun observeForPet(petId: String): Flow<List<JournalAttachment>>
    suspend fun importJournalAttachment(petId: String, journalEntryId: String, uri: Uri): JournalAttachment
    suspend fun deleteJournalAttachment(id: String)
    suspend fun deleteAttachmentsForEntry(journalEntryId: String)
}
