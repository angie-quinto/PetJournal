package com.example.petjournal.data.repository

import com.example.petjournal.data.local.dao.HealthEventDao
import com.example.petjournal.data.local.dao.WeightDao
import com.example.petjournal.data.local.entity.*
import com.example.petjournal.domain.model.*
import com.example.petjournal.domain.repository.HealthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class HealthRepositoryImpl(
    private val healthEventDao: HealthEventDao,
    private val weightDao: WeightDao
) : HealthRepository {

    override fun observeEvents(petId: String): Flow<List<HealthEvent>> {
        return healthEventDao.observeEventsWithDetails(petId).map { records ->
            records.map { record ->
                record.event.toDomain().copy(
                    vaccinationDetail = record.vaccinationDetail?.let {
                        VaccinationDetail(it.vaccineName, it.doseDescription, it.batchNumber, it.provider)
                    },
                    dewormingDetail = record.dewormingDetail?.let {
                        DewormingDetail(it.productName, it.activeIngredient, it.dose, it.weightAtAdministrationGrams)
                    },
                    medicationDetail = record.medicationDetail?.let {
                        MedicationDetail(it.medicationName, it.instructions, it.startDate, it.endDate, it.status)
                    },
                    vetVisitDetail = record.vetVisitDetail?.let {
                        VetVisitDetail(it.clinicName, it.reason, it.examinationNotes, it.followUpInstructions)
                    },
                    symptomDetail = record.symptomDetail?.let {
                        SymptomDetail(it.description, it.severity, it.onset, it.resolution)
                    }
                )
            }
        }
    }

    override suspend fun saveEvent(input: HealthEventInput): String {
        val id = input.id ?: UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        val existingEvent = input.id?.let { healthEventDao.getEventById(it) }
        val createdAt = existingEvent?.createdAt ?: now
        val entity = HealthEventEntity(
            id = id,
            petId = input.petId,
            type = input.type,
            title = input.title,
            eventDate = input.eventDate,
            eventTime = input.eventTime,
            notes = input.notes,
            providerName = input.providerName,
            diagnosisText = input.diagnosisText,
            nextDueDate = input.nextDueDate,
            createdAt = createdAt,
            updatedAt = now,
            deletedAt = null
        )
        healthEventDao.saveEventWithDetails(
            event = entity,
            vaccination = input.vaccinationDetail?.let {
                VaccinationDetailEntity(id, it.vaccineName, it.doseDescription, it.batchNumber, it.provider)
            },
            deworming = input.dewormingDetail?.let {
                DewormingDetailEntity(id, it.productName, it.activeIngredient, it.dose, it.weightAtAdministrationGrams)
            },
            medication = input.medicationDetail?.let {
                MedicationDetailEntity(id, it.medicationName, it.instructions, it.startDate, it.endDate, it.status)
            },
            vetVisit = input.vetVisitDetail?.let {
                VetVisitDetailEntity(id, it.clinicName, it.reason, it.examinationNotes, it.followUpInstructions)
            },
            symptom = input.symptomDetail?.let {
                SymptomDetailEntity(id, it.description, it.severity, it.onset, it.resolution)
            },
            clearMissingDetails = existingEvent == null || existingEvent.type != input.type
        )
        return id
    }

    override suspend fun deleteEvent(id: String) {
        healthEventDao.softDeleteEvent(id, System.currentTimeMillis())
    }

    override suspend fun recordWeight(input: WeightInput): String {
        val id = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        val entity = WeightRecordEntity(
            id = id,
            petId = input.petId,
            weightGrams = input.weightGrams,
            measuredAt = input.measuredAt,
            notes = input.notes,
            createdAt = now,
            updatedAt = now,
            deletedAt = null
        )
        weightDao.insertWeightRecord(entity)
        return id
    }

    override fun observeWeightHistory(petId: String): Flow<List<WeightRecord>> {
        return weightDao.observeWeightHistory(petId).map { entities ->
            entities.map {
                WeightRecord(
                    id = it.id,
                    petId = it.petId,
                    weightGrams = it.weightGrams,
                    measuredAt = it.measuredAt,
                    notes = it.notes,
                    createdAt = it.createdAt,
                    updatedAt = it.updatedAt,
                    deletedAt = it.deletedAt
                )
            }
        }
    }

    private fun HealthEventEntity.toDomain(): HealthEvent {
        return HealthEvent(
            id = id,
            petId = petId,
            type = type,
            title = title,
            eventDate = eventDate,
            eventTime = eventTime,
            notes = notes,
            providerName = providerName,
            diagnosisText = diagnosisText,
            nextDueDate = nextDueDate,
            createdAt = createdAt,
            updatedAt = updatedAt,
            deletedAt = deletedAt
        )
    }
}
