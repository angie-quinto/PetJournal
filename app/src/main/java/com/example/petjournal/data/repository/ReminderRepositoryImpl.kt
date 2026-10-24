package com.example.petjournal.data.repository

import com.example.petjournal.data.local.dao.ReminderDao
import com.example.petjournal.data.local.entity.CareReminderEntity
import com.example.petjournal.domain.model.CareReminder
import com.example.petjournal.domain.model.ReminderRecurrence
import com.example.petjournal.domain.model.ReminderInput
import com.example.petjournal.domain.repository.ReminderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class ReminderRepositoryImpl(
    private val reminderDao: ReminderDao
) : ReminderRepository {

    override fun observeReminders(): Flow<List<CareReminder>> {
        return reminderDao.observeReminders().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun observeRemindersForPet(petId: String): Flow<List<CareReminder>> {
        return reminderDao.observeRemindersForPet(petId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun saveReminder(input: ReminderInput): String {
        val id = input.id ?: UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        val createdAt = input.id?.let { reminderDao.getReminderById(it)?.createdAt } ?: now
        val entity = CareReminderEntity(
            id = id,
            petId = input.petId,
            healthEventId = input.healthEventId,
            title = input.title,
            notes = input.notes,
            dueAt = input.dueAt,
            recurrenceType = input.recurrenceType,
            recurrenceRule = input.recurrenceRule,
            status = "ACTIVE",
            completedAt = null,
            notificationEnabled = input.notificationEnabled,
            createdAt = createdAt,
            updatedAt = now
        )
        reminderDao.insertReminder(entity)
        return id
    }

    override suspend fun completeReminder(id: String, completedAt: Long): CareReminder? {
        val reminder = reminderDao.getReminderById(id) ?: return null
        val nextDueAt = ReminderRecurrence.nextDueAt(
            reminder.recurrenceType,
            reminder.dueAt,
            completedAt,
            recurrenceRule = reminder.recurrenceRule
        )
        reminderDao.updateReminderAfterCompletion(
            id = id,
            status = if (nextDueAt == null) "COMPLETED" else "ACTIVE",
            completedAt = if (nextDueAt == null) completedAt else null,
            dueAt = nextDueAt ?: reminder.dueAt,
            updatedAt = completedAt
        )
        return reminder.copy(
            status = if (nextDueAt == null) "COMPLETED" else "ACTIVE",
            completedAt = if (nextDueAt == null) completedAt else null,
            dueAt = nextDueAt ?: reminder.dueAt,
            updatedAt = completedAt
        ).toDomain()
    }

    override suspend fun rescheduleReminder(id: String, dueAt: Long) {
        reminderDao.rescheduleReminder(id, dueAt)
    }

    private fun CareReminderEntity.toDomain(): CareReminder {
        return CareReminder(
            id = id,
            petId = petId,
            healthEventId = healthEventId,
            title = title,
            notes = notes,
            dueAt = dueAt,
            recurrenceType = recurrenceType,
            recurrenceRule = recurrenceRule,
            status = status,
            completedAt = completedAt,
            notificationEnabled = notificationEnabled,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

}
