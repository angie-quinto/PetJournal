package com.example.petjournal.domain.repository

import com.example.petjournal.domain.model.CareReminder
import com.example.petjournal.domain.model.ReminderInput
import kotlinx.coroutines.flow.Flow

interface ReminderRepository {
    fun observeReminders(): Flow<List<CareReminder>>
    fun observeRemindersForPet(petId: String): Flow<List<CareReminder>>
    suspend fun saveReminder(input: ReminderInput): String
    suspend fun completeReminder(id: String, completedAt: Long): CareReminder?
    suspend fun rescheduleReminder(id: String, dueAt: Long)
}
