package com.example.petjournal.domain.repository

import com.example.petjournal.domain.model.JournalEntry
import com.example.petjournal.domain.model.JournalEntryInput
import kotlinx.coroutines.flow.Flow

interface JournalRepository {
    fun observeEntries(petId: String): Flow<List<JournalEntry>>
    suspend fun saveEntry(input: JournalEntryInput): String
    suspend fun deleteEntry(id: String)
}
