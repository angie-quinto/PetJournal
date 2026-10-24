package com.example.petjournal.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.petjournal.data.local.entity.VaccineCatalogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VaccineCatalogDao {
    @Query("SELECT * FROM vaccine_catalog WHERE species = :species")
    fun observeCatalogForSpecies(species: String): Flow<List<VaccineCatalogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCatalogItems(items: List<VaccineCatalogEntity>)
}
