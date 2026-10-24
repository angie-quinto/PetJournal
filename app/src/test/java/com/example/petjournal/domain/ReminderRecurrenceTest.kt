package com.example.petjournal.domain

import com.example.petjournal.domain.model.ReminderRecurrence
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneId

class ReminderRecurrenceTest {
    private val zone = ZoneId.of("UTC")

    @Test
    fun lateDailyCompletionKeepsTheOriginalScheduleAndTime() {
        val due = LocalDateTime.of(2026, 5, 1, 9, 30).atZone(zone).toInstant().toEpochMilli()
        val completed = LocalDateTime.of(2026, 5, 4, 15, 0).atZone(zone).toInstant().toEpochMilli()

        val next = ReminderRecurrence.nextDueAt("DAILY", due, completed, zone)

        assertEquals(
            LocalDateTime.of(2026, 5, 5, 9, 30).atZone(zone).toInstant().toEpochMilli(),
            next
        )
    }

    @Test
    fun monthlyRecurrenceRetainsEndOfMonthAnchor() {
        val due = LocalDateTime.of(2026, 1, 31, 8, 0).atZone(zone).toInstant().toEpochMilli()
        val completed = LocalDateTime.of(2026, 1, 31, 10, 0).atZone(zone).toInstant().toEpochMilli()

        val next = ReminderRecurrence.nextDueAt("MONTHLY", due, completed, zone, "DAY_OF_MONTH:31")

        assertEquals(
            LocalDateTime.of(2026, 2, 28, 8, 0).atZone(zone).toInstant().toEpochMilli(),
            next
        )
        val following = ReminderRecurrence.nextDueAt("MONTHLY", next!!, next, zone, "DAY_OF_MONTH:31")
        assertEquals(
            LocalDateTime.of(2026, 3, 31, 8, 0).atZone(zone).toInstant().toEpochMilli(),
            following
        )
    }

    @Test
    fun oneTimeReminderHasNoNextOccurrence() {
        assertNull(ReminderRecurrence.nextDueAt("NONE", 100L, 200L, zone))
    }
}
