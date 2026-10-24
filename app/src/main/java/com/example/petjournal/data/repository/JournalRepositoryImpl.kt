package com.example.petjournal.data.repository

import com.example.petjournal.data.local.dao.JournalDao
import com.example.petjournal.data.local.entity.JournalEntryEntity
import com.example.petjournal.domain.model.JournalEntry
import com.example.petjournal.domain.model.JournalEntryInput
import com.example.petjournal.domain.repository.JournalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class JournalRepositoryImpl(
    private val journalDao: JournalDao
) : JournalRepository {

    override fun observeEntries(petId: String): Flow<List<JournalEntry>> {
        return journalDao.observeEntries(petId).map { entities ->
            entities.map {
                JournalEntry(
                    id = it.id,
                    petId = it.petId,
                    title = it.title,
                    content = it.content,
                    category = it.category,
                    eventDate = it.eventDate,
                    createdAt = it.createdAt,
                    updatedAt = it.updatedAt,
                    deletedAt = it.deletedAt
                )
            }
        }
    }

    override suspend fun saveEntry(input: JournalEntryInput): String {
        val id = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        val entity = JournalEntryEntity(
            id = id,
            petId = input.petId,
            title = input.title,
            content = input.content,
            category = input.category,
            eventDate = input.eventDate,
            createdAt = now,
            updatedAt = now,
            deletedAt = null
        )
        journalDao.insertEntry(entity)
        return id
    }

    override suspend fun deleteEntry(id: String) {
        journalDao.softDeleteEntry(id, System.currentTimeMillis())
    }
}
