package com.example.petjournal.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.petjournal.data.local.entity.JournalEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface JournalDao {
    @Query("SELECT * FROM journal_entries WHERE petId = :petId AND deletedAt IS NULL ORDER BY eventDate DESC, createdAt DESC")
    fun observeEntries(petId: String): Flow<List<JournalEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: JournalEntryEntity)

    @Query("UPDATE journal_entries SET deletedAt = :deletedAt WHERE id = :id")
    suspend fun softDeleteEntry(id: String, deletedAt: Long)
}
