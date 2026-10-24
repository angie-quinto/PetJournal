package com.example.petjournal.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "vaccination_details",
    foreignKeys = [
        ForeignKey(
            entity = HealthEventEntity::class,
            parentColumns = ["id"],
            childColumns = ["healthEventId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class VaccinationDetailEntity(
    @PrimaryKey
    val healthEventId: String,
    val vaccineName: String,
    val doseDescription: String? = null,
    val batchNumber: String? = null,
    val provider: String? = null
)

@Entity(
    tableName = "deworming_details",
    foreignKeys = [
        ForeignKey(
            entity = HealthEventEntity::class,
            parentColumns = ["id"],
            childColumns = ["healthEventId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class DewormingDetailEntity(
    @PrimaryKey
    val healthEventId: String,
    val productName: String,
    val activeIngredient: String? = null,
    val dose: String? = null,
    val weightAtAdministrationGrams: Int? = null
)

@Entity(
    tableName = "medication_details",
    foreignKeys = [
        ForeignKey(
            entity = HealthEventEntity::class,
            parentColumns = ["id"],
            childColumns = ["healthEventId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class MedicationDetailEntity(
    @PrimaryKey
    val healthEventId: String,
    val medicationName: String,
    val instructions: String,
    val startDate: String,
    val endDate: String? = null,
    val status: String = "ACTIVE" // ACTIVE, COMPLETED, STOPPED
)

@Entity(
    tableName = "vet_visit_details",
    foreignKeys = [
        ForeignKey(
            entity = HealthEventEntity::class,
            parentColumns = ["id"],
            childColumns = ["healthEventId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class VetVisitDetailEntity(
    @PrimaryKey
    val healthEventId: String,
    val clinicName: String,
    val reason: String,
    val examinationNotes: String? = null,
    val followUpInstructions: String? = null
)

@Entity(
    tableName = "symptom_details",
    foreignKeys = [
        ForeignKey(
            entity = HealthEventEntity::class,
            parentColumns = ["id"],
            childColumns = ["healthEventId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class SymptomDetailEntity(
    @PrimaryKey
    val healthEventId: String,
    val description: String,
    val severity: String = "MILD", // MILD, MODERATE, SEVERE
    val onset: String? = null,
    val resolution: String? = null
)
