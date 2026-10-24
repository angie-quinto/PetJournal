package com.example.petjournal.domain.model

data class JournalAttachment(
    val id: String,
    val petId: String,
    val journalEntryId: String,
    val filePath: String,
    val mimeType: String,
    val caption: String?
)
