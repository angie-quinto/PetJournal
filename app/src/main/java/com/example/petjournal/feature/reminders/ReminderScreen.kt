package com.example.petjournal.feature.reminders

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.petjournal.domain.model.CareReminder
import com.example.petjournal.domain.model.Pet
import com.example.petjournal.domain.model.ReminderInput
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.text.SimpleDateFormat
import java.util.Locale

private val recurrenceOptions = listOf("NONE", "DAILY", "WEEKLY", "MONTHLY")
private val reminderDateTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderScreen(
    pet: Pet?,
    reminders: List<CareReminder>,
    onSave: (ReminderInput) -> Unit,
    onComplete: (CareReminder) -> Unit
) {
    var showDialog by rememberSaveable { mutableStateOf(false) }
    var editingReminder by remember { mutableStateOf<CareReminder?>(null) }
    var showAll by rememberSaveable { mutableStateOf(false) }
    val visibleReminders = reminders
        .filter { it.petId == pet?.id }
        .filter { showAll || it.status == "ACTIVE" }
        .sortedWith(compareBy<CareReminder> { it.status != "ACTIVE" }.thenBy { it.dueAt })

    Scaffold(
        topBar = { TopAppBar(title = { Text("Care & reminders") }) },
        floatingActionButton = {
            if (pet != null) {
                FloatingActionButton(onClick = { showDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add care reminder")
                }
            }
        }
    ) { padding ->
        if (pet == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Add a pet before creating care reminders.")
            }
        } else {
            Column(Modifier.fillMaxSize().padding(padding)) {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(!showAll, { showAll = false }, label = { Text("Upcoming") })
                    FilterChip(showAll, { showAll = true }, label = { Text("All reminders") })
                }
                if (visibleReminders.isEmpty()) {
                    Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("A little care goes a long way", style = MaterialTheme.typography.titleLarge)
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Add a reminder for medication, grooming, checkups, or anything your pet needs.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(visibleReminders, key = { it.id }) { reminder ->
                            ReminderCard(
                                reminder = reminder,
                                onEdit = { editingReminder = reminder },
                                onComplete = { onComplete(reminder) }
                            )
                        }
                    }
                }
            }
        }
    }

    if ((showDialog || editingReminder != null) && pet != null) {
        ReminderEditorDialog(
            pet = pet,
            reminder = editingReminder,
            onDismiss = {
                showDialog = false
                editingReminder = null
            },
            onSave = {
                onSave(it)
                showDialog = false
                editingReminder = null
            }
        )
    }
}

@Composable
private fun ReminderCard(reminder: CareReminder, onEdit: () -> Unit, onComplete: () -> Unit) {
    val dateLabel = remember(reminder.dueAt) {
        SimpleDateFormat("EEE, MMM d · h:mm a", Locale.getDefault()).format(Date(reminder.dueAt))
    }
    Card(Modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(reminder.title, style = MaterialTheme.typography.titleMedium)
                Text(dateLabel, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                reminder.notes?.takeIf(String::isNotBlank)?.let {
                    Spacer(Modifier.height(4.dp))
                    Text(it, style = MaterialTheme.typography.bodyMedium)
                }
                if (reminder.recurrenceType != "NONE") {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        reminder.recurrenceType.lowercase().replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (reminder.status != "ACTIVE") {
                    Text("Completed", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
                }
            }
            if (reminder.status == "ACTIVE") {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit ${reminder.title}")
                }
                IconButton(onClick = onComplete) {
                    Icon(Icons.Default.Check, contentDescription = "Complete ${reminder.title}", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
private fun ReminderEditorDialog(
    pet: Pet,
    reminder: CareReminder?,
    onDismiss: () -> Unit,
    onSave: (ReminderInput) -> Unit
) {
    val zone = remember { ZoneId.systemDefault() }
    val existingDateTime = remember(reminder?.id) {
        reminder?.dueAt?.let { Instant.ofEpochMilli(it).atZone(zone).toLocalDateTime() }
    }
    var title by rememberSaveable(reminder?.id) { mutableStateOf(reminder?.title.orEmpty()) }
    var notes by rememberSaveable(reminder?.id) { mutableStateOf(reminder?.notes.orEmpty()) }
    var dueDate by rememberSaveable(reminder?.id) {
        mutableStateOf(existingDateTime?.toLocalDate()?.format(DateTimeFormatter.ISO_DATE) ?: LocalDate.now().format(DateTimeFormatter.ISO_DATE))
    }
    var dueTime by rememberSaveable(reminder?.id) { mutableStateOf(existingDateTime?.toLocalTime()?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: "09:00") }
    var recurrence by rememberSaveable(reminder?.id) { mutableStateOf(reminder?.recurrenceType ?: "NONE") }
    var recurrenceMenu by remember { mutableStateOf(false) }
    var notificationsEnabled by rememberSaveable(reminder?.id) { mutableStateOf(reminder?.notificationEnabled ?: false) }
    val dueAt = remember(dueDate, dueTime) {
        runCatching {
            LocalDateTime.parse("$dueDate $dueTime", reminderDateTimeFormat)
                .atZone(zone)
                .toInstant()
                .toEpochMilli()
        }.getOrNull()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (reminder == null) "New reminder" else "Edit reminder") },
        text = {
            Column(
                Modifier.fillMaxWidth().heightIn(max = 480.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("For ${pet.name}", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                OutlinedTextField(title, { title = it }, label = { Text("What needs doing?") }, singleLine = true)
                OutlinedTextField(dueDate, { dueDate = it }, label = { Text("Due date (YYYY-MM-DD)") }, singleLine = true)
                OutlinedTextField(
                    dueTime,
                    { dueTime = it },
                    label = { Text("Time (24-hour, HH:mm)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                Box {
                    TextButton(onClick = { recurrenceMenu = true }) {
                        Text("Repeat: ${recurrence.lowercase().replaceFirstChar { it.uppercase() }}")
                    }
                    DropdownMenu(expanded = recurrenceMenu, onDismissRequest = { recurrenceMenu = false }) {
                        recurrenceOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option.lowercase().replaceFirstChar { it.uppercase() }) },
                                onClick = { recurrence = option; recurrenceMenu = false }
                            )
                        }
                    }
                }
                OutlinedTextField(notes, { notes = it }, label = { Text("Notes (optional)") }, minLines = 2)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = notificationsEnabled, onCheckedChange = { notificationsEnabled = it })
                    Text("Notify me on this device")
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = title.isNotBlank() && dueAt != null && dueAt > System.currentTimeMillis(),
                onClick = {
                    dueAt?.let {
                        onSave(
                            ReminderInput(
                                petId = pet.id,
                                title = title.trim(),
                                notes = notes.trim().ifBlank { null },
                                dueAt = it,
                                recurrenceType = recurrence,
                                recurrenceRule = if (recurrence == "MONTHLY") {
                                    val anchor = reminder?.recurrenceRule
                                        ?.takeIf { rule -> rule.startsWith("DAY_OF_MONTH:") }
                                        ?.substringAfter(':')
                                        ?.toIntOrNull()
                                        ?: runCatching { LocalDate.parse(dueDate).dayOfMonth }.getOrDefault(1)
                                    "DAY_OF_MONTH:$anchor"
                                } else null,
                                notificationEnabled = notificationsEnabled,
                                id = reminder?.id
                            )
                        )
                    }
                }
            ) { Text("Save reminder") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
