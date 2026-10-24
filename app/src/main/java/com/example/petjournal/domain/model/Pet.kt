package com.example.petjournal.domain.model

data class Pet(
    val id: String,
    val name: String,
    val species: String,
    val customSpecies: String?,
    val breed: String?,
    val sex: String?,
    val birthDate: String?,
    val birthDatePrecision: String,
    val adoptionDate: String?,
    val weightUnitPreference: String,
    val photoPath: String?,
    val microchipNumber: String?,
    val medicalNotes: String?,
    val createdAt: Long,
    val updatedAt: Long,
    val archivedAt: Long?
)

data class PetInput(
    val name: String,
    val species: String,
    val customSpecies: String? = null,
    val breed: String? = null,
    val sex: String? = null,
    val birthDate: String? = null,
    val birthDatePrecision: String = "EXACT",
    val adoptionDate: String? = null,
    val weightUnitPreference: String = "KG",
    val photoPath: String? = null,
    val microchipNumber: String? = null,
    val medicalNotes: String? = null
)
