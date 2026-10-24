package com.example.petjournal.domain.model

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

object ReminderRecurrence {
    fun nextDueAt(
        recurrenceType: String,
        scheduledAt: Long,
        completedAt: Long,
        zoneId: ZoneId = ZoneId.systemDefault(),
        recurrenceRule: String? = null
    ): Long? {
        if (recurrenceType == "NONE") return null
        val scheduledDateTime = Instant.ofEpochMilli(scheduledAt).atZone(zoneId)
        val scheduledDate = scheduledDateTime.toLocalDate()
        val completedDate = Instant.ofEpochMilli(completedAt).atZone(zoneId).toLocalDate()
        val candidate = when (recurrenceType) {
            "DAILY" -> {
                val days = ChronoUnit.DAYS.between(scheduledDate, completedDate)
                scheduledDate.plusDays((days + 1).coerceAtLeast(1))
            }
            "WEEKLY" -> {
                val weeks = ChronoUnit.WEEKS.between(scheduledDate, completedDate)
                scheduledDate.plusWeeks((weeks + 1).coerceAtLeast(1))
            }
            "MONTHLY" -> {
                val anchorDay = recurrenceRule
                    ?.takeIf { it.startsWith("DAY_OF_MONTH:") }
                    ?.substringAfter(':')
                    ?.toIntOrNull()
                    ?.coerceIn(1, 31)
                    ?: scheduledDate.dayOfMonth
                val months = ChronoUnit.MONTHS.between(
                    scheduledDate.withDayOfMonth(1),
                    completedDate.withDayOfMonth(1)
                )
                val targetMonth = scheduledDate.withDayOfMonth(1).plusMonths(months.coerceAtLeast(0))
                var next = targetMonth.withDayOfMonth(anchorDay.coerceAtMost(targetMonth.lengthOfMonth()))
                if (!next.isAfter(completedDate)) {
                    val followingMonth = targetMonth.plusMonths(1)
                    next = followingMonth.withDayOfMonth(anchorDay.coerceAtMost(followingMonth.lengthOfMonth()))
                }
                next
            }
            else -> return null
        }
        return candidate.atTime(scheduledDateTime.toLocalTime()).atZone(zoneId).toInstant().toEpochMilli()
    }
}
