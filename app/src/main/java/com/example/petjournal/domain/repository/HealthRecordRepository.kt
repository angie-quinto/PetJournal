package com.example.petjournal.domain.repository

import com.example.petjournal.domain.model.HealthRecord
import com.example.petjournal.domain.model.HealthRecordInput
import kotlinx.coroutines.flow.Flow

interface HealthRecordRepository {
    fun observeHealthRecords(petId: String): Flow<List<HealthRecord>>
    suspend fun saveHealthRecord(input: HealthRecordInput): String
    suspend fun deleteHealthRecord(id: String)
}
