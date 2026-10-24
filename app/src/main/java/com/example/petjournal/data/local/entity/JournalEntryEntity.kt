package com.example.petjournal.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "journal_entries",
    foreignKeys = [
        ForeignKey(
            entity = PetEntity::class,
            parentColumns = ["id"],
            childColumns = ["petId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["petId"]),
        Index(value = ["eventDate"]),
        Index(value = ["category"]),
        Index(value = ["deletedAt"])
    ]
)
data class JournalEntryEntity(
    @PrimaryKey
    val id: String,
    val petId: String,
    val title: String,
    val content: String,
    val category: String, // MEMORY, MILESTONE, OBSERVATION, OTHER
    val eventDate: String, // ISO YYYY-MM-DD
    val createdAt: Long,
    val updatedAt: Long,
    val deletedAt: Long? = null
)
