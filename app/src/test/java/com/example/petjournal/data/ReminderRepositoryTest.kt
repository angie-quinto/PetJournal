package com.example.petjournal.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.petjournal.core.database.PetJournalDatabase
import com.example.petjournal.data.local.entity.PetEntity
import com.example.petjournal.data.repository.ReminderRepositoryImpl
import com.example.petjournal.domain.model.ReminderInput
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.LocalDateTime
import java.time.ZoneId

@RunWith(RobolectricTestRunner::class)
class ReminderRepositoryTest {
    @Test
    fun lateCompletionAdvancesRecurringReminderAndKeepsNotificationsOptIn() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val database = Room.inMemoryDatabaseBuilder(context, PetJournalDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        database.petDao().insertPet(PetEntity("pet-1", "Mochi", "DOG", createdAt = 1L, updatedAt = 1L))
        val repository = ReminderRepositoryImpl(database.reminderDao())
        val zone = ZoneId.systemDefault()
        val dueAt = LocalDateTime.now(zone).minusDays(4).withHour(9).withMinute(0)
            .atZone(zone).toInstant().toEpochMilli()
        val completedAt = System.currentTimeMillis()
        val reminderId = repository.saveReminder(
            ReminderInput(
                petId = "pet-1",
                title = "Daily medication",
                dueAt = dueAt,
                recurrenceType = "DAILY"
            )
        )

        val beforeComplete = repository.observeReminders().first().single()
        assertFalse(beforeComplete.notificationEnabled)
        val updated = repository.completeReminder(reminderId, completedAt)
        val stored = repository.observeReminders().first().single()

        assertEquals("ACTIVE", updated?.status)
        assertEquals("ACTIVE", stored.status)
        assertEquals(updated?.dueAt, stored.dueAt)
        assertEquals(true, stored.dueAt > completedAt)
        database.close()
    }
}
