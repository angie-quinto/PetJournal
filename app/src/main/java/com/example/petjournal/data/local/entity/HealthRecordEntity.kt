package com.example.petjournal.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "health_records",
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
        Index(value = ["type"]),
        Index(value = ["occurredAt"])
    ]
)
data class HealthRecordEntity(
    @PrimaryKey
    val id: String,
    val petId: String,
    val type: String, // VACCINATION, MEDICATION, DEWORMING, VET_VISIT, OTHER
    val title: String,
    val occurredAt: String? = null,
    val notes: String? = null,
    val veterinarianOrClinic: String? = null,
    val createdAt: Long,
    val updatedAt: Long,
    val deletedAt: Long? = null
)
