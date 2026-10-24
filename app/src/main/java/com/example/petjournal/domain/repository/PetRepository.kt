package com.example.petjournal.domain.repository

import com.example.petjournal.domain.model.Pet
import com.example.petjournal.domain.model.PetInput
import kotlinx.coroutines.flow.Flow

interface PetRepository {
    fun observeActivePets(): Flow<List<Pet>>
    fun observePet(id: String): Flow<Pet?>
    suspend fun createPet(input: PetInput): String
    suspend fun updatePet(id: String, input: PetInput)
    suspend fun archivePet(id: String)
    suspend fun restorePet(id: String)
}
