package com.example.petjournal.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

data class HealthEventWithDetails(
    @Embedded val event: HealthEventEntity,
    @Relation(parentColumn = "id", entityColumn = "healthEventId")
    val vaccinationDetail: VaccinationDetailEntity?,
    @Relation(parentColumn = "id", entityColumn = "healthEventId")
    val dewormingDetail: DewormingDetailEntity?,
    @Relation(parentColumn = "id", entityColumn = "healthEventId")
    val medicationDetail: MedicationDetailEntity?,
    @Relation(parentColumn = "id", entityColumn = "healthEventId")
    val vetVisitDetail: VetVisitDetailEntity?,
    @Relation(parentColumn = "id", entityColumn = "healthEventId")
    val symptomDetail: SymptomDetailEntity?
)
