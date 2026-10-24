package com.example.petjournal.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "vaccination_record_details",
    foreignKeys = [
        ForeignKey(
            entity = HealthRecordEntity::class,
            parentColumns = ["id"],
            childColumns = ["healthRecordId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class HealthVaccinationDetailEntity(
    @PrimaryKey
    val healthRecordId: String,
    val vaccineCatalogId: String? = null,
    val customVaccineName: String? = null,
    val nextDueAt: String? = null,
    val batchNumber: String? = null
)

@Entity(
    tableName = "medication_record_details",
    foreignKeys = [
        ForeignKey(
            entity = HealthRecordEntity::class,
            parentColumns = ["id"],
            childColumns = ["healthRecordId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class HealthMedicationDetailEntity(
    @PrimaryKey
    val healthRecordId: String,
    val medicationName: String,
    val instructions: String? = null,
    val startDate: String,
    val endDate: String? = null
)

@Entity(
    tableName = "reminder_occurrences",
    foreignKeys = [
        ForeignKey(
            entity = CareReminderEntity::class,
            parentColumns = ["id"],
            childColumns = ["reminderId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ReminderOccurrenceEntity(
    @PrimaryKey
    val id: String,
    val reminderId: String,
    val scheduledAt: Long,
    val status: String = "SCHEDULED", // SCHEDULED, COMPLETED, SKIPPED, MISSED, CANCELLED
    val completedAt: Long? = null,
    val actualActionAt: Long? = null,
    val notes: String? = null
)

@Entity(
    tableName = "vaccine_catalog"
)
data class VaccineCatalogEntity(
    @PrimaryKey
    val id: String,
    val species: String,
    val name: String,
    val category: String,
    val guidanceNote: String,
    val sourceUrl: String,
    val lastVerifiedAt: String
)
