package com.example.petjournal.feature.journal

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.petjournal.domain.model.JournalEntryInput
import com.example.petjournal.domain.model.Pet
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddJournalEntryScreen(
    pets: List<Pet>,
    initialPetId: String,
    attachmentError: String?,
    onNavigateBack: () -> Unit,
    onSaveEntry: (JournalEntryInput, List<Uri>) -> Unit
) {
    var targetPetId by remember { mutableStateOf(initialPetId.ifBlank { pets.firstOrNull()?.id ?: "" }) }
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("MEMORY") }
    val today = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
    var eventDate by remember { mutableStateOf(today) }

    var titleError by remember { mutableStateOf(false) }

    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    val selectedPhotos = remember { mutableStateListOf<Uri>() }
    val photoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(maxItems = 8)
    ) { selected ->
        selectedPhotos.clear()
        selectedPhotos.addAll(selected)
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        eventDate = formatter.format(Date(millis))
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Journal Entry") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(text = "Select Pet", style = MaterialTheme.typography.titleSmall)
            ScrollableTabRow(
                selectedTabIndex = pets.indexOfFirst { it.id == targetPetId }.coerceAtLeast(0),
                modifier = Modifier.fillMaxWidth()
            ) {
                pets.forEach { pet ->
                    Tab(
                        selected = pet.id == targetPetId,
                        onClick = { targetPetId = pet.id },
                        text = { Text(pet.name) }
                    )
                }
            }

            OutlinedTextField(
                value = title,
                onValueChange = {
                    title = it
                    titleError = it.isBlank()
                },
                label = { Text("Title *") },
                isError = titleError,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = eventDate,
                onValueChange = {},
                readOnly = true,
                label = { Text("Date (YYYY-MM-DD)") },
                shape = RoundedCornerShape(16.dp),
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Select Date")
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDatePicker = true }
            )

            var categoryMenuExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = categoryMenuExpanded,
                onExpandedChange = { categoryMenuExpanded = it }
            ) {
                OutlinedTextField(
                    value = category.lowercase().replaceFirstChar { it.uppercase() },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Category") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(categoryMenuExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable)
                )
                ExposedDropdownMenu(
                    expanded = categoryMenuExpanded,
                    onDismissRequest = { categoryMenuExpanded = false }
                ) {
                    listOf("MEMORY", "MILESTONE", "OBSERVATION", "OTHER").forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option.lowercase().replaceFirstChar { it.uppercase() }) },
                            onClick = {
                                category = option
                                categoryMenuExpanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("Content / Notes *") },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
                minLines = 4
            )

            attachmentError?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            OutlinedButton(
                onClick = {
                    photoPicker.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (selectedPhotos.isEmpty()) "Add photos" else "Choose photos (${selectedPhotos.size})")
            }
            if (selectedPhotos.isNotEmpty()) {
                Text(
                    "${selectedPhotos.size} photo${if (selectedPhotos.size == 1) "" else "s"} will be copied into PetJournal.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    if (title.isBlank() || content.isBlank() || targetPetId.isBlank()) {
                        titleError = title.isBlank()
                    } else {
                        val input = JournalEntryInput(
                            petId = targetPetId,
                            title = title.trim(),
                            content = content.trim(),
                            category = category,
                            eventDate = eventDate.trim()
                        )
                        onSaveEntry(input, selectedPhotos.toList())
                    }
                },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Entry")
            }
        }
    }
}
