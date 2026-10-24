package com.example.petjournal.data.repository

import com.example.petjournal.data.local.dao.PetDao
import com.example.petjournal.data.local.entity.PetEntity
import com.example.petjournal.domain.model.Pet
import com.example.petjournal.domain.model.PetInput
import com.example.petjournal.domain.repository.PetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class PetRepositoryImpl(
    private val petDao: PetDao
) : PetRepository {

    override fun observeActivePets(): Flow<List<Pet>> {
        return petDao.observeActivePets().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun observePet(id: String): Flow<Pet?> {
        return petDao.observePet(id).map { it?.toDomain() }
    }

    override suspend fun createPet(input: PetInput): String {
        val id = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        val entity = PetEntity(
            id = id,
            name = input.name,
            species = input.species,
            customSpecies = input.customSpecies,
            breed = input.breed,
            sex = input.sex,
            birthDate = input.birthDate,
            birthDatePrecision = input.birthDatePrecision,
            adoptionDate = input.adoptionDate,
            weightUnitPreference = input.weightUnitPreference,
            photoPath = input.photoPath,
            microchipNumber = input.microchipNumber,
            medicalNotes = input.medicalNotes,
            createdAt = now,
            updatedAt = now,
            archivedAt = null
        )
        petDao.insertPet(entity)
        return id
    }

    override suspend fun updatePet(id: String, input: PetInput) {
        val existing = petDao.getPetById(id) ?: return
        val now = System.currentTimeMillis()
        val updated = existing.copy(
            name = input.name,
            species = input.species,
            customSpecies = input.customSpecies,
            breed = input.breed,
            sex = input.sex,
            birthDate = input.birthDate,
            birthDatePrecision = input.birthDatePrecision,
            adoptionDate = input.adoptionDate,
            weightUnitPreference = input.weightUnitPreference,
            photoPath = input.photoPath,
            microchipNumber = input.microchipNumber,
            medicalNotes = input.medicalNotes,
            updatedAt = now
        )
        petDao.updatePet(updated)
    }

    override suspend fun archivePet(id: String) {
        val now = System.currentTimeMillis()
        petDao.setPetArchivedStatus(id, now, now)
    }

    override suspend fun restorePet(id: String) {
        val now = System.currentTimeMillis()
        petDao.setPetArchivedStatus(id, null, now)
    }

    private fun PetEntity.toDomain(): Pet {
        return Pet(
            id = id,
            name = name,
            species = species,
            customSpecies = customSpecies,
            breed = breed,
            sex = sex,
            birthDate = birthDate,
            birthDatePrecision = birthDatePrecision,
            adoptionDate = adoptionDate,
            weightUnitPreference = weightUnitPreference,
            photoPath = photoPath,
            microchipNumber = microchipNumber,
            medicalNotes = medicalNotes,
            createdAt = createdAt,
            updatedAt = updatedAt,
            archivedAt = archivedAt
        )
    }
}
