package com.example.petjournal.feature.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.petjournal.domain.model.CareReminder
import com.example.petjournal.domain.model.Pet

@Composable
fun DashboardScreen(
    pets: List<Pet>,
    reminders: List<CareReminder>,
    onNavigateToPets: () -> Unit,
    onCompleteReminder: (String) -> Unit,
    onNavigateToTimeline: () -> Unit,
    onNavigateToHealth: () -> Unit,
    onNavigateToTools: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Your pet's day",
                style = MaterialTheme.typography.headlineMedium
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = if (pets.size == 1) "Caring for ${pets.first().name}" else "Your family of pets",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (pets.isEmpty()) "Add your first pet to start keeping their care history." else "${pets.size} ${if (pets.size == 1) "pet" else "pets"} · All their care, in one place",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = onNavigateToPets) {
                            Text(if (pets.isEmpty()) "Add a pet" else "Manage pets")
                        }
                        if (pets.isNotEmpty()) {
                            TextButton(onClick = onNavigateToTimeline) {
                                Text("View timeline")
                            }
                        }
                    }
                }
            }
        }

        if (pets.isNotEmpty()) {
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedCard(
                        onClick = onNavigateToHealth,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text("Health & weight", style = MaterialTheme.typography.titleSmall)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Track visits and progress",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    OutlinedCard(
                        onClick = onNavigateToTimeline,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text("Memories", style = MaterialTheme.typography.titleSmall)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "See their story so far",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
        item {
            OutlinedCard(
                onClick = onNavigateToTools,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Backups & vet reports", style = MaterialTheme.typography.titleSmall)
                        Text(
                            "Keep a copy or prepare for a visit",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        }
                    Text("Open", color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        item {
            Text(
                text = "Upcoming Care & Reminders",
                style = MaterialTheme.typography.titleMedium
            )
        }

        if (reminders.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No upcoming care reminders.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(reminders) { reminder ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = reminder.title, style = MaterialTheme.typography.titleSmall)
                            if (!reminder.notes.isNullOrBlank()) {
                                Text(text = reminder.notes, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        Button(onClick = { onCompleteReminder(reminder.id) }) {
                            Text("Done")
                        }
                    }
                }
            }
        }
    }
}
