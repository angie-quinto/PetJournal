package com.example.petjournal.feature.timeline

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.petjournal.domain.model.Pet
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

data class TimelineRecord(
    val id: String,
    val type: String,
    val title: String,
    val date: String,
    val details: String,
    val sortTimestamp: Long
)

@Composable
fun TimelineScreen(
    pets: List<Pet>,
    selectedPetId: String,
    onSelectPet: (String) -> Unit,
    records: List<TimelineRecord>,
    onAddHealth: () -> Unit,
    onAddJournal: () -> Unit,
    onDelete: (TimelineRecord) -> Unit
) {
    var query by rememberSaveable { mutableStateOf("") }
    var selectedType by rememberSaveable { mutableStateOf("ALL") }
    var fromDate by rememberSaveable { mutableStateOf("") }
    var toDate by rememberSaveable { mutableStateOf("") }
    val filteredRecords = remember(records, query, selectedType, fromDate, toDate) {
        records.filter { record ->
            val matchesQuery = query.isBlank() ||
                record.title.contains(query, ignoreCase = true) ||
                record.details.contains(query, ignoreCase = true) ||
                record.type.contains(query, ignoreCase = true)
            val matchesType = selectedType == "ALL" || record.type == selectedType
            val matchesStart = fromDate.isBlank() || (record.date >= fromDate && isDate(fromDate))
            val matchesEnd = toDate.isBlank() || (record.date <= toDate && isDate(toDate))
            matchesQuery && matchesType && matchesStart && matchesEnd
        }.sortedByDescending(TimelineRecord::sortTimestamp)
    }

    Column(Modifier.fillMaxSize()) {
        Text(
            "Pet timeline",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )
        if (pets.isNotEmpty()) {
            ScrollableTabRow(
                selectedTabIndex = pets.indexOfFirst { it.id == selectedPetId }.coerceAtLeast(0),
                edgePadding = 16.dp
            ) {
                pets.forEach { pet ->
                    Tab(
                        selected = pet.id == selectedPetId,
                        onClick = { onSelectPet(pet.id) },
                        text = { Text(pet.name) }
                    )
                }
            }
        }
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            label = { Text("Search health, weight, and memories") },
            singleLine = true
        )
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("ALL", "HEALTH", "WEIGHT", "JOURNAL").forEach { type ->
                FilterChip(
                    selected = selectedType == type,
                    onClick = { selectedType = type },
                    label = { Text(type.lowercase().replaceFirstChar { it.uppercase() }) }
                )
            }
        }
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = fromDate,
                onValueChange = { fromDate = it },
                modifier = Modifier.weight(1f),
                label = { Text("From (YYYY-MM-DD)") },
                singleLine = true,
                isError = fromDate.isNotBlank() && !isDate(fromDate)
            )
            OutlinedTextField(
                value = toDate,
                onValueChange = { toDate = it },
                modifier = Modifier.weight(1f),
                label = { Text("To (YYYY-MM-DD)") },
                singleLine = true,
                isError = toDate.isNotBlank() && !isDate(toDate)
            )
        }
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(selected = false, onClick = onAddHealth, label = { Text("+ Health record") })
            FilterChip(selected = false, onClick = onAddJournal, label = { Text("+ Memory") })
        }
        if (filteredRecords.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                Text(
                    if (records.isEmpty()) "Your pet's health, weight, and memories will appear here."
                    else "No records match these search filters.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                contentPadding = androidx.compose.foundation.layout.PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredRecords, key = { "${it.type}-${it.id}" }) { record ->
                    TimelineCard(record) { onDelete(record) }
                }
            }
        }
    }
}

@Composable
private fun TimelineCard(record: TimelineRecord, onDelete: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(record.type.lowercase().replaceFirstChar { it.titlecase(Locale.getDefault()) }, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(4.dp))
                Text(record.title, style = MaterialTheme.typography.titleMedium)
                Text(record.date, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (record.details.isNotBlank()) {
                    Spacer(Modifier.height(6.dp))
                    Text(record.details, style = MaterialTheme.typography.bodyMedium)
                }
            }
            if (record.type == "HEALTH" || record.type == "JOURNAL") {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete ${record.title}", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

private fun isDate(value: String): Boolean = runCatching {
    LocalDate.parse(value, DateTimeFormatter.ISO_DATE)
}.isSuccess
