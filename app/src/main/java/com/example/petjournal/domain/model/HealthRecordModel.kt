package com.example.petjournal.domain.model

data class HealthRecord(
    val id: String,
    val petId: String,
    val type: String,
    val title: String,
    val occurredAt: String?,
    val notes: String?,
    val veterinarianOrClinic: String?,
    val createdAt: Long,
    val updatedAt: Long
)

data class HealthRecordInput(
    val petId: String,
    val type: String,
    val title: String,
    val occurredAt: String? = null,
    val notes: String? = null,
    val veterinarianOrClinic: String? = null,
    val vaccineName: String? = null,
    val nextDueAt: String? = null,
    val batchNumber: String? = null
)
