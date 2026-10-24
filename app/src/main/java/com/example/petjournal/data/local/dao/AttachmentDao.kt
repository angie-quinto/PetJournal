package com.example.petjournal.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.petjournal.data.local.entity.AttachmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AttachmentDao {
    @Query("SELECT * FROM attachments WHERE petId = :petId AND deletedAt IS NULL")
    fun observeAttachments(petId: String): Flow<List<AttachmentEntity>>

    @Query("SELECT * FROM attachments WHERE journalEntryId = :journalEntryId AND deletedAt IS NULL")
    suspend fun getAttachmentsForEntry(journalEntryId: String): List<AttachmentEntity>

    @Query("SELECT * FROM attachments WHERE id = :id AND deletedAt IS NULL")
    suspend fun getAttachmentById(id: String): AttachmentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttachment(attachment: AttachmentEntity)

    @Query("UPDATE attachments SET deletedAt = :deletedAt WHERE id = :id")
    suspend fun softDeleteAttachment(id: String, deletedAt: Long)
}
