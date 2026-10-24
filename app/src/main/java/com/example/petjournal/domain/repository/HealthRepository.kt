package com.example.petjournal.domain.repository

import com.example.petjournal.domain.model.*
import kotlinx.coroutines.flow.Flow

interface HealthRepository {
    fun observeEvents(petId: String): Flow<List<HealthEvent>>
    suspend fun saveEvent(input: HealthEventInput): String
    suspend fun deleteEvent(id: String)
    suspend fun recordWeight(input: WeightInput): String
    fun observeWeightHistory(petId: String): Flow<List<WeightRecord>>
}
