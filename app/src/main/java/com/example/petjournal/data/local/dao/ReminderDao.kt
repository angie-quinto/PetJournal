package com.example.petjournal.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.petjournal.data.local.entity.CareReminderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {
    @Query("SELECT * FROM care_reminders WHERE id = :id")
    suspend fun getReminderById(id: String): CareReminderEntity?

    @Query("SELECT * FROM care_reminders ORDER BY dueAt ASC")
    fun observeReminders(): Flow<List<CareReminderEntity>>

    @Query("SELECT * FROM care_reminders WHERE petId = :petId ORDER BY dueAt ASC")
    fun observeRemindersForPet(petId: String): Flow<List<CareReminderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: CareReminderEntity)

    @Query("UPDATE care_reminders SET status = :status, completedAt = :completedAt WHERE id = :id")
    suspend fun updateReminderStatus(id: String, status: String, completedAt: Long?)

    @Query("UPDATE care_reminders SET dueAt = :dueAt WHERE id = :id")
    suspend fun rescheduleReminder(id: String, dueAt: Long)

    @Query("UPDATE care_reminders SET status = :status, completedAt = :completedAt, dueAt = :dueAt, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateReminderAfterCompletion(
        id: String,
        status: String,
        completedAt: Long?,
        dueAt: Long,
        updatedAt: Long
    )
}
