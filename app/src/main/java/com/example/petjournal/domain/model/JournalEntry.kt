package com.example.petjournal.domain.model

data class JournalEntry(
    val id: String,
    val petId: String,
    val title: String,
    val content: String,
    val category: String,
    val eventDate: String,
    val createdAt: Long,
    val updatedAt: Long,
    val deletedAt: Long?
)

data class JournalEntryInput(
    val petId: String,
    val title: String,
    val content: String,
    val category: String,
    val eventDate: String
)
