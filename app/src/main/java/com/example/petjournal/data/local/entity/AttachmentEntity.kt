package com.example.petjournal.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "attachments",
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
        Index(value = ["journalEntryId"]),
        Index(value = ["healthEventId"]),
        Index(value = ["deletedAt"])
    ]
)
data class AttachmentEntity(
    @PrimaryKey
    val id: String,
    val petId: String,
    val journalEntryId: String? = null,
    val healthEventId: String? = null,
    val relativeFilePath: String,
    val mimeType: String,
    val caption: String? = null,
    val createdAt: Long,
    val deletedAt: Long? = null
)
