package com.example.petjournal.domain.model

data class WeightRecord(
    val id: String,
    val petId: String,
    val weightGrams: Int,
    val measuredAt: Long,
    val notes: String?,
    val createdAt: Long,
    val updatedAt: Long,
    val deletedAt: Long?
)

data class WeightInput(
    val petId: String,
    val weightGrams: Int,
    val measuredAt: Long,
    val notes: String? = null
)
