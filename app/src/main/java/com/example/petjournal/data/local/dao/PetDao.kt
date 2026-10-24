package com.example.petjournal.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.petjournal.data.local.entity.PetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PetDao {
    @Query("SELECT * FROM pets WHERE archivedAt IS NULL ORDER BY createdAt DESC")
    fun observeActivePets(): Flow<List<PetEntity>>

    @Query("SELECT * FROM pets WHERE id = :id")
    fun observePet(id: String): Flow<PetEntity?>

    @Query("SELECT * FROM pets WHERE id = :id")
    suspend fun getPetById(id: String): PetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPet(pet: PetEntity)

    @Update
    suspend fun updatePet(pet: PetEntity)

    @Query("UPDATE pets SET archivedAt = :archivedAt, updatedAt = :updatedAt WHERE id = :id")
    suspend fun setPetArchivedStatus(id: String, archivedAt: Long?, updatedAt: Long)
}
