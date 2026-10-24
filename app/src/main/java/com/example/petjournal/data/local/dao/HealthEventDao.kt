package com.example.petjournal.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.petjournal.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface HealthEventDao {
    @Query("SELECT * FROM health_events WHERE petId = :petId AND deletedAt IS NULL ORDER BY eventDate DESC, createdAt DESC")
    fun observeEvents(petId: String): Flow<List<HealthEventEntity>>

    @Transaction
    @Query("SELECT * FROM health_events WHERE petId = :petId AND deletedAt IS NULL ORDER BY eventDate DESC, createdAt DESC")
    fun observeEventsWithDetails(petId: String): Flow<List<HealthEventWithDetails>>

    @Query("SELECT * FROM health_events WHERE id = :id")
    suspend fun getEventById(id: String): HealthEventEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertEventIfAbsent(event: HealthEventEntity): Long

    @Update
    suspend fun updateEvent(event: HealthEventEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVaccinationDetail(detail: VaccinationDetailEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDewormingDetail(detail: DewormingDetailEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicationDetail(detail: MedicationDetailEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVetVisitDetail(detail: VetVisitDetailEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSymptomDetail(detail: SymptomDetailEntity)

    @Query("UPDATE health_events SET deletedAt = :deletedAt WHERE id = :id")
    suspend fun softDeleteEvent(id: String, deletedAt: Long)

    @Query("DELETE FROM vaccination_details WHERE healthEventId = :id")
    suspend fun deleteVaccinationDetail(id: String)

    @Query("DELETE FROM deworming_details WHERE healthEventId = :id")
    suspend fun deleteDewormingDetail(id: String)

    @Query("DELETE FROM medication_details WHERE healthEventId = :id")
    suspend fun deleteMedicationDetail(id: String)

    @Query("DELETE FROM vet_visit_details WHERE healthEventId = :id")
    suspend fun deleteVetVisitDetail(id: String)

    @Query("DELETE FROM symptom_details WHERE healthEventId = :id")
    suspend fun deleteSymptomDetail(id: String)

    @Transaction
    suspend fun saveEventWithDetails(
        event: HealthEventEntity,
        vaccination: VaccinationDetailEntity?,
        deworming: DewormingDetailEntity?,
        medication: MedicationDetailEntity?,
        vetVisit: VetVisitDetailEntity?,
        symptom: SymptomDetailEntity?,
        clearMissingDetails: Boolean
    ) {
        if (insertEventIfAbsent(event) == -1L) {
            updateEvent(event)
        }
        if (clearMissingDetails) {
            deleteVaccinationDetail(event.id)
            deleteDewormingDetail(event.id)
            deleteMedicationDetail(event.id)
            deleteVetVisitDetail(event.id)
            deleteSymptomDetail(event.id)
        }
        vaccination?.let { insertVaccinationDetail(it) }
        deworming?.let { insertDewormingDetail(it) }
        medication?.let { insertMedicationDetail(it) }
        vetVisit?.let { insertVetVisitDetail(it) }
        symptom?.let { insertSymptomDetail(it) }
    }
}
