package com.example.petjournal.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "health_events",
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
        Index(value = ["type"]),
        Index(value = ["deletedAt"])
    ]
)
data class HealthEventEntity(
    @PrimaryKey
    val id: String,
    val petId: String,
    val type: String, // VACCINATION, DEWORMING, MEDICATION, VET_VISIT, SYMPTOM, TREATMENT, PARASITE_PREVENTION, OTHER
    val title: String,
    val eventDate: String, // ISO YYYY-MM-DD
    val eventTime: String? = null,
    val notes: String? = null,
    val providerName: String? = null,
    val diagnosisText: String? = null,
    val nextDueDate: String? = null,
    val createdAt: Long,
    val updatedAt: Long,
    val deletedAt: Long? = null
)
