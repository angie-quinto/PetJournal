package com.example.petjournal.core.backup

import androidx.room.RoomDatabase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object VetReportService {
    fun createReport(database: RoomDatabase, petId: String): String {
        val db = database.openHelper.readableDatabase
        val report = StringBuilder()
        db.query("SELECT * FROM pets WHERE id = ?", arrayOf(petId)).use { pet ->
            if (!pet.moveToFirst()) throw IllegalArgumentException("The selected pet could not be found.")
            report.appendLine("PET JOURNAL — VETERINARY SUMMARY")
            report.appendLine("Generated: ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())}")
            report.appendLine()
            report.appendLine("Pet: ${pet.stringOrNull("name") ?: "Unknown"}")
            report.appendLine("Species: ${pet.stringOrNull("species") ?: "Unknown"}")
            pet.stringOrNull("breed")?.let { report.appendLine("Breed: $it") }
            pet.stringOrNull("sex")?.let { report.appendLine("Sex: $it") }
            pet.stringOrNull("birthDate")?.let { report.appendLine("Birth date: $it") }
            pet.stringOrNull("microchipNumber")?.let { report.appendLine("Microchip: $it") }
            pet.stringOrNull("medicalNotes")?.let { report.appendLine("Medical notes: $it") }
        }

        report.appendLine()
        report.appendLine("HEALTH EVENTS")
        var healthCount = 0
        db.query(
            "SELECT type, title, eventDate, notes, providerName, nextDueDate FROM health_events WHERE petId = ? AND deletedAt IS NULL ORDER BY eventDate DESC",
            arrayOf(petId)
        ).use { cursor ->
            while (cursor.moveToNext()) {
                healthCount++
                report.appendLine("- ${cursor.stringOrNull("eventDate") ?: "Date not recorded"} · ${cursor.stringOrNull("title") ?: "Untitled"} (${cursor.stringOrNull("type") ?: "OTHER"})")
                cursor.stringOrNull("providerName")?.let { report.appendLine("  Provider: $it") }
                cursor.stringOrNull("nextDueDate")?.let { report.appendLine("  Next due: $it") }
                cursor.stringOrNull("notes")?.let { report.appendLine("  Notes: $it") }
            }
        }
        db.query(
            "SELECT type, title, occurredAt, notes, veterinarianOrClinic FROM health_records WHERE petId = ? AND deletedAt IS NULL ORDER BY occurredAt DESC",
            arrayOf(petId)
        ).use { cursor ->
            while (cursor.moveToNext()) {
                healthCount++
                report.appendLine("- ${cursor.stringOrNull("occurredAt") ?: "Date not recorded"} · ${cursor.stringOrNull("title") ?: "Untitled"} (${cursor.stringOrNull("type") ?: "OTHER"})")
                cursor.stringOrNull("veterinarianOrClinic")?.let { report.appendLine("  Provider: $it") }
                cursor.stringOrNull("notes")?.let { report.appendLine("  Notes: $it") }
            }
        }
        if (healthCount == 0) report.appendLine("No health events recorded.")

        report.appendLine()
        report.appendLine("WEIGHT HISTORY")
        var weightCount = 0
        db.query(
            "SELECT weightGrams, measuredAt, notes FROM weight_records WHERE petId = ? AND deletedAt IS NULL ORDER BY measuredAt DESC",
            arrayOf(petId)
        ).use { cursor ->
            while (cursor.moveToNext()) {
                weightCount++
                val kg = cursor.getLong(cursor.getColumnIndexOrThrow("weightGrams")) / 1000.0
                val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    .format(Date(cursor.getLong(cursor.getColumnIndexOrThrow("measuredAt"))))
                report.appendLine("- $date · ${"%.2f".format(Locale.getDefault(), kg)} kg")
                cursor.stringOrNull("notes")?.let { report.appendLine("  Notes: $it") }
            }
        }
        if (weightCount == 0) report.appendLine("No weight measurements recorded.")

        report.appendLine()
        report.appendLine("CARE REMINDERS")
        var reminderCount = 0
        db.query(
            "SELECT title, dueAt, recurrenceType, status, notes FROM care_reminders WHERE petId = ? ORDER BY dueAt DESC",
            arrayOf(petId)
        ).use { cursor ->
            while (cursor.moveToNext()) {
                reminderCount++
                val due = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                    .format(Date(cursor.getLong(cursor.getColumnIndexOrThrow("dueAt"))))
                report.appendLine("- $due · ${cursor.stringOrNull("title") ?: "Untitled"} (${cursor.stringOrNull("status") ?: "UNKNOWN"})")
                cursor.stringOrNull("recurrenceType")?.takeIf { it != "NONE" }?.let { report.appendLine("  Repeats: $it") }
                cursor.stringOrNull("notes")?.let { report.appendLine("  Notes: $it") }
            }
        }
        if (reminderCount == 0) report.appendLine("No care reminders recorded.")

        report.appendLine()
        report.appendLine("JOURNAL")
        var entryCount = 0
        db.query(
            "SELECT title, eventDate, category, content FROM journal_entries WHERE petId = ? AND deletedAt IS NULL ORDER BY eventDate DESC",
            arrayOf(petId)
        ).use { cursor ->
            while (cursor.moveToNext()) {
                entryCount++
                report.appendLine("- ${cursor.stringOrNull("eventDate") ?: "Date not recorded"} · ${cursor.stringOrNull("title") ?: "Untitled"} (${cursor.stringOrNull("category") ?: "OTHER"})")
                cursor.stringOrNull("content")?.let { report.appendLine("  $it") }
            }
        }
        if (entryCount == 0) report.appendLine("No journal entries recorded.")
        return report.toString()
    }

    private fun android.database.Cursor.stringOrNull(column: String): String? {
        val index = getColumnIndexOrThrow(column)
        return if (isNull(index)) null else getString(index)
    }
}
