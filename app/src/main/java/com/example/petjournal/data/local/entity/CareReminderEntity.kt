package com.example.petjournal.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "care_reminders",
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
        Index(value = ["dueAt"]),
        Index(value = ["status"])
    ]
)
data class CareReminderEntity(
    @PrimaryKey
    val id: String,
    val petId: String,
    val healthEventId: String? = null,
    val title: String,
    val notes: String? = null,
    val dueAt: Long, // Epoch ms
    val recurrenceType: String = "NONE", // NONE, DAILY, WEEKLY, MONTHLY, CUSTOM
    val recurrenceRule: String? = null,
    val status: String = "ACTIVE", // ACTIVE, COMPLETED, CANCELLED
    val completedAt: Long? = null,
    val notificationEnabled: Boolean = true,
    val createdAt: Long,
    val updatedAt: Long
)
