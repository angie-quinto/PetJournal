package com.example.petjournal.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.petjournal.data.local.entity.WeightRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WeightDao {
    @Query("SELECT * FROM weight_records WHERE petId = :petId AND deletedAt IS NULL ORDER BY measuredAt DESC")
    fun observeWeightHistory(petId: String): Flow<List<WeightRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeightRecord(record: WeightRecordEntity)

    @Query("UPDATE weight_records SET deletedAt = :deletedAt WHERE id = :id")
    suspend fun softDeleteWeightRecord(id: String, deletedAt: Long)
}
