package com.example.petjournal.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.petjournal.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface HealthRecordDao {
    @Query("SELECT * FROM health_records WHERE petId = :petId AND deletedAt IS NULL ORDER BY occurredAt DESC, createdAt DESC")
    fun observeHealthRecords(petId: String): Flow<List<HealthRecordEntity>>

    @Query("SELECT * FROM health_records WHERE id = :id")
    suspend fun getHealthRecordById(id: String): HealthRecordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHealthRecord(record: HealthRecordEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVaccinationDetail(detail: HealthVaccinationDetailEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicationDetail(detail: HealthMedicationDetailEntity)

    @Query("UPDATE health_records SET deletedAt = :deletedAt WHERE id = :id")
    suspend fun softDeleteHealthRecord(id: String, deletedAt: Long)
}
