package com.example.petjournal.feature.journal

import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.example.petjournal.domain.model.JournalAttachment
import com.example.petjournal.domain.model.JournalEntry
import com.example.petjournal.domain.model.Pet

@Composable
fun JournalScreen(
    pets: List<Pet>,
    selectedPetId: String,
    onSelectPet: (String) -> Unit,
    entries: List<JournalEntry>,
    attachments: List<JournalAttachment>,
    attachmentError: String?,
    onAddEntryClick: () -> Unit,
    onDeleteEntry: (String) -> Unit,
    onDeleteAttachment: (String) -> Unit,
    onClearAttachmentError: () -> Unit
) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onAddEntryClick) {
                Icon(Icons.Default.Add, contentDescription = "Add Journal Entry")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            attachmentError?.let { error ->
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(error, Modifier.weight(1f), color = MaterialTheme.colorScheme.onErrorContainer)
                        TextButton(onClick = onClearAttachmentError) { Text("Dismiss") }
                    }
                }
            }
            if (pets.isNotEmpty()) {
                ScrollableTabRow(
                    selectedTabIndex = pets.indexOfFirst { it.id == selectedPetId }.coerceAtLeast(0),
                    modifier = Modifier.fillMaxWidth()
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

            if (entries.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Favorite,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = "No journal entries for this pet.", style = MaterialTheme.typography.bodyLarge)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = onAddEntryClick) {
                            Text("Add Memory or Milestone")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(entries) { entry ->
                        JournalCard(
                            entry = entry,
                            attachments = attachments.filter { it.journalEntryId == entry.id },
                            onDelete = { onDeleteEntry(entry.id) },
                            onDeleteAttachment = onDeleteAttachment
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun JournalCard(
    entry: JournalEntry,
    attachments: List<JournalAttachment>,
    onDelete: () -> Unit,
    onDeleteAttachment: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = entry.title,
                    style = MaterialTheme.typography.titleMedium
                )
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete Entry",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                AssistChip(
                    onClick = {},
                    label = { Text(entry.category) }
                )
                Text(
                    text = entry.eventDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = entry.content,
                style = MaterialTheme.typography.bodyMedium
            )
            if (attachments.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(attachments, key = { it.id }) { attachment ->
                        val bitmap = remember(attachment.filePath) {
                            android.graphics.BitmapFactory.decodeFile(attachment.filePath)
                        }
                        Box {
                            if (bitmap != null) {
                                Image(
                                    bitmap = bitmap.asImageBitmap(),
                                    contentDescription = attachment.caption ?: "Journal photo",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(120.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                )
                            } else {
                                Surface(
                                    modifier = Modifier.size(120.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("Photo unavailable", style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                            IconButton(
                                onClick = { onDeleteAttachment(attachment.id) },
                                modifier = Modifier.align(Alignment.TopEnd)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Remove photo", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }
}
