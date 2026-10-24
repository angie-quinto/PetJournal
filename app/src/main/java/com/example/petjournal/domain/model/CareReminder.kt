package com.example.petjournal.domain.model

data class CareReminder(
    val id: String,
    val petId: String,
    val healthEventId: String?,
    val title: String,
    val notes: String?,
    val dueAt: Long,
    val recurrenceType: String,
    val recurrenceRule: String?,
    val status: String,
    val completedAt: Long?,
    val notificationEnabled: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)

data class ReminderInput(
    val petId: String,
    val healthEventId: String? = null,
    val title: String,
    val notes: String? = null,
    val dueAt: Long,
    val recurrenceType: String = "NONE",
    val recurrenceRule: String? = null,
    val notificationEnabled: Boolean = false,
    val id: String? = null
)
