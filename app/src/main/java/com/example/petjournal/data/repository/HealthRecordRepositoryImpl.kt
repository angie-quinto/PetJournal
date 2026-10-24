package com.example.petjournal.data.repository

import com.example.petjournal.data.local.dao.HealthRecordDao
import com.example.petjournal.data.local.entity.HealthRecordEntity
import com.example.petjournal.data.local.entity.HealthVaccinationDetailEntity
import com.example.petjournal.domain.model.HealthRecord
import com.example.petjournal.domain.model.HealthRecordInput
import com.example.petjournal.domain.repository.HealthRecordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class HealthRecordRepositoryImpl(
    private val healthRecordDao: HealthRecordDao
) : HealthRecordRepository {

    override fun observeHealthRecords(petId: String): Flow<List<HealthRecord>> {
        return healthRecordDao.observeHealthRecords(petId).map { entities ->
            entities.map {
                HealthRecord(
                    id = it.id,
                    petId = it.petId,
                    type = it.type,
                    title = it.title,
                    occurredAt = it.occurredAt,
                    notes = it.notes,
                    veterinarianOrClinic = it.veterinarianOrClinic,
                    createdAt = it.createdAt,
                    updatedAt = it.updatedAt
                )
            }
        }
    }

    override suspend fun saveHealthRecord(input: HealthRecordInput): String {
        val id = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        val entity = HealthRecordEntity(
            id = id,
            petId = input.petId,
            type = input.type,
            title = input.title,
            occurredAt = input.occurredAt,
            notes = input.notes,
            veterinarianOrClinic = input.veterinarianOrClinic,
            createdAt = now,
            updatedAt = now
        )
        healthRecordDao.insertHealthRecord(entity)

        if (input.type == "VACCINATION" && !input.vaccineName.isNullOrBlank()) {
            healthRecordDao.insertVaccinationDetail(
                HealthVaccinationDetailEntity(
                    healthRecordId = id,
                    customVaccineName = input.vaccineName,
                    nextDueAt = input.nextDueAt,
                    batchNumber = input.batchNumber
                )
            )
        }
        return id
    }

    override suspend fun deleteHealthRecord(id: String) {
        healthRecordDao.softDeleteHealthRecord(id, System.currentTimeMillis())
    }
}
