package com.example.petjournal.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "pets",
    indices = [
        Index(value = ["archivedAt"]),
        Index(value = ["createdAt"])
    ]
)
data class PetEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val species: String,
    val customSpecies: String? = null,
    val breed: String? = null,
    val sex: String? = null,
    val birthDate: String? = null, // ISO YYYY-MM-DD
    val birthDatePrecision: String = "EXACT", // EXACT, ESTIMATED, UNKNOWN
    val adoptionDate: String? = null,
    val weightUnitPreference: String = "KG", // KG, LBS
    val photoPath: String? = null,
    val microchipNumber: String? = null,
    val medicalNotes: String? = null,
    val createdAt: Long,
    val updatedAt: Long,
    val archivedAt: Long? = null
)
