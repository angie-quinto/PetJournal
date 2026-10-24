package com.example.petjournal.feature.health

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.petjournal.domain.model.HealthEvent
import com.example.petjournal.domain.model.HealthEventInput
import com.example.petjournal.domain.model.VaccinationDetail
import com.example.petjournal.domain.model.DewormingDetail
import com.example.petjournal.domain.model.MedicationDetail
import com.example.petjournal.domain.model.VetVisitDetail
import com.example.petjournal.domain.model.SymptomDetail
import com.example.petjournal.domain.model.Pet
import com.example.petjournal.domain.model.WeightInput
import com.example.petjournal.domain.model.WeightRecord
import com.example.petjournal.domain.model.WeightConversion
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

private val healthTypes = listOf(
    "VACCINATION", "DEWORMING", "MEDICATION", "VET_VISIT", "SYMPTOM", "TREATMENT", "OTHER"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthScreen(
    pet: Pet?,
    pets: List<Pet>,
    selectedPetId: String,
    onSelectPet: (String) -> Unit,
    events: List<HealthEvent>,
    weights: List<WeightRecord>,
    onSaveEvent: (HealthEventInput) -> Unit,
    onDeleteEvent: (String) -> Unit,
    onRecordWeight: (WeightInput) -> Unit
) {
    var showEventDialog by rememberSaveable { mutableStateOf(false) }
    var showWeightDialog by rememberSaveable { mutableStateOf(false) }
    var selectedTab by rememberSaveable { mutableStateOf("HEALTH") }
    var editingEvent by remember { mutableStateOf<HealthEvent?>(null) }

    Scaffold(
        topBar = { TopAppBar(title = { Text(pet?.let { "${it.name}'s health" } ?: "Health & weight") }) },
        floatingActionButton = {
            if (pet != null) {
                FloatingActionButton(
                    onClick = {
                        if (selectedTab == "HEALTH") showEventDialog = true else showWeightDialog = true
                    }
                ) {
                    Icon(Icons.Default.Add, contentDescription = if (selectedTab == "HEALTH") "Add health record" else "Record weight")
                }
            }
        }
    ) { padding ->
        if (pet == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Add a pet to start tracking health and weight.")
            }
        } else {
            Column(Modifier.fillMaxSize().padding(padding)) {
                if (pets.size > 1) {
                    ScrollableTabRow(
                        selectedTabIndex = pets.indexOfFirst { it.id == selectedPetId }.coerceAtLeast(0),
                        edgePadding = 16.dp
                    ) {
                        pets.forEach { availablePet ->
                            Tab(
                                selected = availablePet.id == selectedPetId,
                                onClick = { onSelectPet(availablePet.id) },
                                text = { Text(availablePet.name) }
                            )
                        }
                    }
                }
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(selectedTab == "HEALTH", { selectedTab = "HEALTH" }, label = { Text("Health") })
                    FilterChip(selectedTab == "WEIGHT", { selectedTab = "WEIGHT" }, label = { Text("Weight") })
                }

                if (selectedTab == "HEALTH") {
                    if (events.isEmpty()) {
                        EmptyHealthState("Keep vaccinations, vet visits, medication, and other care in one place.")
                    } else {
                        LazyColumn(
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(events, key = { it.id }) { event ->
                                HealthEventCard(
                                    event,
                                    onEdit = { editingEvent = event },
                                    onDelete = { onDeleteEvent(event.id) }
                                )
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(Modifier.padding(16.dp)) {
                                    Text("Weight trend", style = MaterialTheme.typography.titleMedium)
                                    Spacer(Modifier.height(12.dp))
                                    WeightTrendChart(weights, Modifier.fillMaxWidth().height(160.dp))
                                }
                            }
                        }
                        if (weights.isEmpty()) {
                            item { Text("No measurements yet. Record your pet's first weight.", Modifier.padding(8.dp)) }
                        } else {
                            items(weights, key = { it.id }) { weight ->
                                WeightCard(weight, pet.weightUnitPreference)
                            }
                        }
                    }
                }
            }
        }
    }

    if ((showEventDialog || editingEvent != null) && pet != null) {
        HealthEventDialog(
            petId = pet.id,
            existingEvent = editingEvent,
            onDismiss = {
                showEventDialog = false
                editingEvent = null
            },
            onSave = {
                onSaveEvent(it)
                showEventDialog = false
                editingEvent = null
            }
        )
    }
    if (showWeightDialog && pet != null) {
        WeightDialog(
            pet = pet,
            onDismiss = { showWeightDialog = false },
            onSave = {
                onRecordWeight(it)
                showWeightDialog = false
            }
        )
    }
}

@Composable
private fun EmptyHealthState(message: String) {
    Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Your pet's health, organized", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun HealthEventCard(event: HealthEvent, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(healthTypeLabel(event.type), style = MaterialTheme.typography.titleMedium)
                    Text(
                        "${event.type.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() }} · ${event.eventDate}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit ${healthTypeLabel(event.type)} record")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete ${healthTypeLabel(event.type)} record", tint = MaterialTheme.colorScheme.error)
                }
            }
            event.providerName?.takeIf(String::isNotBlank)?.let {
                Text("Care provider: $it", style = MaterialTheme.typography.bodyMedium)
            }
            val detailSummary = listOfNotNull(
                event.vaccinationDetail?.let { "Vaccine: ${it.vaccineName}" },
                event.dewormingDetail?.let { "Product: ${it.productName}" },
                event.medicationDetail?.let { "Medication: ${it.medicationName}" },
                event.vetVisitDetail?.let { "Clinic: ${it.clinicName}" },
                event.symptomDetail?.let { "Symptom: ${it.description} · ${it.severity.lowercase()}" }
            ).joinToString(" · ")
            if (detailSummary.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(detailSummary, style = MaterialTheme.typography.bodySmall)
            }
            event.notes?.takeIf(String::isNotBlank)?.let {
                Spacer(Modifier.height(6.dp))
                Text(it, style = MaterialTheme.typography.bodyMedium)
            }
            event.nextDueDate?.takeIf(String::isNotBlank)?.let {
                Spacer(Modifier.height(6.dp))
                Text("Next due $it", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun WeightCard(weight: WeightRecord, unit: String) {
    val value = WeightConversion.fromGrams(weight.weightGrams, unit, if (unit.equals("LB", true) || unit.equals("LBS", true)) 1 else 2)
    val label = if (unit.equals("LB", ignoreCase = true) || unit.equals("LBS", ignoreCase = true)) "lb" else "kg"
    val date = remember(weight.measuredAt) {
        SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(weight.measuredAt))
    }
    Card(Modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("$value $label", style = MaterialTheme.typography.titleMedium)
                Text(date, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            weight.notes?.takeIf(String::isNotBlank)?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f).padding(start = 16.dp))
            }
        }
    }
}

@Composable
private fun WeightTrendChart(weights: List<WeightRecord>, modifier: Modifier = Modifier) {
    if (weights.size < 2) {
        Box(modifier, contentAlignment = Alignment.Center) {
            Text("Record at least two measurements to see a trend.", style = MaterialTheme.typography.bodySmall)
        }
        return
    }
    val points = remember(weights) {
        weights.sortedBy { it.measuredAt }.map { it.weightGrams.toFloat() }
    }
    val minValue = points.minOrNull() ?: 0f
    val range = (points.maxOrNull() ?: minValue) - minValue
    val lineColor = MaterialTheme.colorScheme.primary
    Canvas(modifier) {
        val horizontalInset = 12.dp.toPx()
        val verticalInset = 12.dp.toPx()
        val usableWidth = size.width - horizontalInset * 2
        val usableHeight = size.height - verticalInset * 2
        val normalizedRange = if (range == 0f) 1f else range
        val offsets = points.mapIndexed { index, value ->
            Offset(
                x = horizontalInset + usableWidth * index / (points.lastIndex.coerceAtLeast(1)),
                y = size.height - verticalInset - ((value - minValue) / normalizedRange) * usableHeight
            )
        }
        offsets.zipWithNext().forEach { (start, end) ->
            drawLine(
                color = lineColor,
                start = start,
                end = end,
                strokeWidth = 4.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
        offsets.forEach { point ->
            drawCircle(lineColor, radius = 5.dp.toPx(), center = point)
        }
    }
}

@Composable
private fun HealthEventDialog(
    petId: String,
    existingEvent: HealthEvent?,
    onDismiss: () -> Unit,
    onSave: (HealthEventInput) -> Unit
) {
    var type by rememberSaveable(existingEvent?.id) { mutableStateOf(existingEvent?.type ?: healthTypes.first()) }
    var typeMenu by remember { mutableStateOf(false) }
    var date by rememberSaveable(existingEvent?.id) {
        mutableStateOf(existingEvent?.eventDate ?: LocalDate.now().format(DateTimeFormatter.ISO_DATE))
    }
    var notes by rememberSaveable(existingEvent?.id) { mutableStateOf(existingEvent?.notes.orEmpty()) }
    var provider by rememberSaveable(existingEvent?.id) { mutableStateOf(existingEvent?.providerName.orEmpty()) }
    var nextDue by rememberSaveable(existingEvent?.id) { mutableStateOf(existingEvent?.nextDueDate.orEmpty()) }
    var vaccineName by rememberSaveable(existingEvent?.id) { mutableStateOf(existingEvent?.vaccinationDetail?.vaccineName.orEmpty()) }
    var vaccineDose by rememberSaveable(existingEvent?.id) { mutableStateOf(existingEvent?.vaccinationDetail?.doseDescription.orEmpty()) }
    var batchNumber by rememberSaveable(existingEvent?.id) { mutableStateOf(existingEvent?.vaccinationDetail?.batchNumber.orEmpty()) }
    var dewormingProduct by rememberSaveable(existingEvent?.id) { mutableStateOf(existingEvent?.dewormingDetail?.productName.orEmpty()) }
    var activeIngredient by rememberSaveable(existingEvent?.id) { mutableStateOf(existingEvent?.dewormingDetail?.activeIngredient.orEmpty()) }
    var medicationName by rememberSaveable(existingEvent?.id) { mutableStateOf(existingEvent?.medicationDetail?.medicationName.orEmpty()) }
    var medicationInstructions by rememberSaveable(existingEvent?.id) { mutableStateOf(existingEvent?.medicationDetail?.instructions.orEmpty()) }
    var clinicName by rememberSaveable(existingEvent?.id) { mutableStateOf(existingEvent?.vetVisitDetail?.clinicName.orEmpty()) }
    var visitReason by rememberSaveable(existingEvent?.id) { mutableStateOf(existingEvent?.vetVisitDetail?.reason.orEmpty()) }
    var symptomDescription by rememberSaveable(existingEvent?.id) { mutableStateOf(existingEvent?.symptomDetail?.description.orEmpty()) }
    var symptomSeverity by rememberSaveable(existingEvent?.id) { mutableStateOf(existingEvent?.symptomDetail?.severity ?: "MILD") }
    var severityMenu by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existingEvent == null) "Add health record" else "Edit health record") },
        text = {
            Column(
                Modifier.fillMaxWidth().heightIn(max = 480.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box {
                    TextButton(onClick = { typeMenu = true }) { Text("Type: ${type.replace('_', ' ')}") }
                    DropdownMenu(expanded = typeMenu, onDismissRequest = { typeMenu = false }) {
                        healthTypes.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() }) },
                                onClick = { type = option; typeMenu = false }
                            )
                        }
                    }
                }
                OutlinedTextField(date, { date = it }, label = { Text("Date (YYYY-MM-DD)") }, singleLine = true)
                OutlinedTextField(provider, { provider = it }, label = { Text("Veterinarian or clinic") }, singleLine = true)
                OutlinedTextField(nextDue, { nextDue = it }, label = { Text("Next due date (optional)") }, singleLine = true)
                when (type) {
                    "VACCINATION" -> {
                        OutlinedTextField(vaccineName, { vaccineName = it }, label = { Text("Vaccine name") }, singleLine = true)
                        OutlinedTextField(vaccineDose, { vaccineDose = it }, label = { Text("Dose / notes") }, singleLine = true)
                        OutlinedTextField(batchNumber, { batchNumber = it }, label = { Text("Batch number") }, singleLine = true)
                    }
                    "DEWORMING" -> {
                        OutlinedTextField(dewormingProduct, { dewormingProduct = it }, label = { Text("Product") }, singleLine = true)
                        OutlinedTextField(activeIngredient, { activeIngredient = it }, label = { Text("Active ingredient") }, singleLine = true)
                    }
                    "MEDICATION" -> {
                        OutlinedTextField(medicationName, { medicationName = it }, label = { Text("Medication") }, singleLine = true)
                        OutlinedTextField(medicationInstructions, { medicationInstructions = it }, label = { Text("Instructions") }, minLines = 2)
                    }
                    "VET_VISIT" -> {
                        OutlinedTextField(clinicName, { clinicName = it }, label = { Text("Clinic") }, singleLine = true)
                        OutlinedTextField(visitReason, { visitReason = it }, label = { Text("Reason for visit") }, minLines = 2)
                    }
                    "SYMPTOM" -> {
                        OutlinedTextField(symptomDescription, { symptomDescription = it }, label = { Text("Symptom") }, minLines = 2)
                        Box {
                            TextButton(onClick = { severityMenu = true }) { Text("Severity: ${symptomSeverity.lowercase()}") }
                            DropdownMenu(expanded = severityMenu, onDismissRequest = { severityMenu = false }) {
                                listOf("MILD", "MODERATE", "SEVERE").forEach { severity ->
                                    DropdownMenuItem(
                                        text = { Text(severity.lowercase().replaceFirstChar { it.uppercase() }) },
                                        onClick = { symptomSeverity = severity; severityMenu = false }
                                    )
                                }
                            }
                        }
                    }
                }
                OutlinedTextField(notes, { notes = it }, label = { Text("Notes") }, minLines = 3)
            }
        },
        confirmButton = {
            TextButton(
                enabled = isIsoDate(date),
                onClick = {
                    onSave(
                        HealthEventInput(
                            petId = petId,
                            type = type,
                            title = healthTypeLabel(type),
                            eventDate = date,
                            id = existingEvent?.id,
                            notes = notes.trim().ifBlank { null },
                            providerName = provider.trim().ifBlank { null },
                            nextDueDate = nextDue.trim().takeIf(::isIsoDate),
                            vaccinationDetail = if (type == "VACCINATION" && vaccineName.isNotBlank()) {
                                VaccinationDetail(vaccineName.trim(), vaccineDose.trim().ifBlank { null }, batchNumber.trim().ifBlank { null }, provider.trim().ifBlank { null })
                            } else null,
                            dewormingDetail = if (type == "DEWORMING" && dewormingProduct.isNotBlank()) {
                                DewormingDetail(dewormingProduct.trim(), activeIngredient.trim().ifBlank { null }, null, null)
                            } else null,
                            medicationDetail = if (type == "MEDICATION" && medicationName.isNotBlank()) {
                                MedicationDetail(medicationName.trim(), medicationInstructions.trim(), date, null, "ACTIVE")
                            } else null,
                            vetVisitDetail = if (type == "VET_VISIT" && clinicName.isNotBlank()) {
                                VetVisitDetail(clinicName.trim(), visitReason.trim(), notes.trim().ifBlank { null }, null)
                            } else null,
                            symptomDetail = if (type == "SYMPTOM" && symptomDescription.isNotBlank()) {
                                SymptomDetail(symptomDescription.trim(), symptomSeverity, date, null)
                            } else null
                        )
                    )
                }
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun WeightDialog(pet: Pet, onDismiss: () -> Unit, onSave: (WeightInput) -> Unit) {
    var amount by rememberSaveable { mutableStateOf("") }
    var notes by rememberSaveable { mutableStateOf("") }
    val isPounds = pet.weightUnitPreference.equals("LB", ignoreCase = true) ||
        pet.weightUnitPreference.equals("LBS", ignoreCase = true)
    val unit = if (isPounds) "lb" else "kg"
    val parsedGrams = remember(amount, isPounds) { WeightConversion.toGrams(amount, unit) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Record weight") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Weight ($unit)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notes (optional)") })
            }
        },
        confirmButton = {
            TextButton(
                enabled = parsedGrams != null,
                onClick = {
                    parsedGrams?.let {
                        onSave(WeightInput(pet.id, it, System.currentTimeMillis(), notes.trim().ifBlank { null }))
                    }
                }
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

private fun isIsoDate(value: String): Boolean = runCatching {
    LocalDate.parse(value, DateTimeFormatter.ISO_DATE)
}.isSuccess

private fun healthTypeLabel(type: String): String =
    type.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() }
