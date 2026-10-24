package com.example.petjournal.feature.backup

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.room.RoomDatabase
import com.example.petjournal.core.backup.PetJournalBackupService
import com.example.petjournal.core.backup.VetReportService
import com.example.petjournal.domain.model.Pet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun DataToolsScreen(
    pets: List<Pet>,
    database: RoomDatabase,
    onNavigateBack: () -> Unit
) {
    var selectedPetId by rememberSaveable { mutableStateOf("") }
    val selectedPet = pets.firstOrNull { it.id == selectedPetId } ?: pets.firstOrNull()
    var pendingRestore by remember { mutableStateOf<Uri?>(null) }
    var working by rememberSaveable { mutableStateOf(false) }
    var feedback by rememberSaveable { mutableStateOf<String?>(null) }
    var isError by rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current

    val exportBackup = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/zip")) { uri ->
        if (uri != null) {
            working = true
            feedback = null
            scope.launch {
                runCatching { withContext(Dispatchers.IO) { PetJournalBackupService.export(context, database, uri) } }
                    .onSuccess { feedback = "Your backup was exported."; isError = false }
                    .onFailure { feedback = it.message ?: "The backup could not be exported."; isError = true }
                working = false
            }
        }
    }
    val importBackup = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            working = true
            feedback = null
            scope.launch {
                runCatching {
                    withContext(Dispatchers.IO) { PetJournalBackupService.validateBackup(context, database, uri) }
                }.onSuccess {
                    pendingRestore = uri
                    feedback = "Backup validated. Review the restore warning before replacing data."
                    isError = false
                }.onFailure {
                    feedback = it.message ?: "This backup could not be validated."
                    isError = true
                }
                working = false
            }
        }
    }
    val exportReport = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/plain")) { uri ->
        if (uri != null && selectedPet != null) {
            working = true
            feedback = null
            val petId = selectedPet.id
            scope.launch {
                runCatching {
                    val report = withContext(Dispatchers.IO) { VetReportService.createReport(database, petId) }
                    withContext(Dispatchers.IO) {
                        context.contentResolver.openOutputStream(uri)?.use {
                            it.write(report.toByteArray(Charsets.UTF_8))
                        } ?: throw IOException("Unable to open the selected report destination.")
                    }
                }.onSuccess { feedback = "The veterinary summary was exported."; isError = false }
                    .onFailure { feedback = it.message ?: "The report could not be exported."; isError = true }
                working = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Backup & reports") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Your records stay on this device. Backups are created only when you choose where to save them.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Favorite, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text("Create a backup", style = MaterialTheme.typography.titleMedium)
                    Text("Export a versioned archive of your pet records and local photos.")
                    Button(
                        onClick = { exportBackup.launch("PetJournal-backup.zip") },
                        enabled = !working
                    ) { Text("Export backup") }
                }
            }
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Favorite, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text("Restore a backup", style = MaterialTheme.typography.titleMedium)
                    Text("PetJournal checks the archive before changing any current records.")
                    OutlinedButton(
                        onClick = { importBackup.launch(arrayOf("application/zip", "application/octet-stream")) },
                        enabled = !working
                    ) { Text("Choose backup") }
                }
            }
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Favorite, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text("Veterinary summary", style = MaterialTheme.typography.titleMedium)
                    Text("Create a readable, shareable text report for your pet's visit.")
                    if (pets.isNotEmpty()) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            pets.forEach { pet ->
                                androidx.compose.material3.FilterChip(
                                    selected = selectedPet?.id == pet.id,
                                    onClick = { selectedPetId = pet.id },
                                    label = { Text(pet.name) }
                                )
                            }
                        }
                    } else {
                        Text("Add a pet before creating a report.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Button(
                        onClick = {
                            selectedPet?.let { exportReport.launch("${it.name}-vet-summary.txt") }
                        },
                        enabled = !working && selectedPet != null
                    ) { Text("Export summary") }
                }
            }
            if (working) CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))
            feedback?.let {
                Text(
                    it,
                    color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }

    pendingRestore?.let { uri ->
        AlertDialog(
            onDismissRequest = { pendingRestore = null },
            title = { Text("Replace app data?") },
            text = {
                Text("This validated backup will replace the records currently stored on this device. This cannot be undone.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        pendingRestore = null
                        working = true
                        feedback = null
                        scope.launch {
                            runCatching {
                                withContext(Dispatchers.IO) { PetJournalBackupService.restore(context, database, uri) }
                            }.onSuccess { cleaned ->
                                feedback = if (cleaned) "Backup restored successfully." else "Backup restored; some old local photos could not be cleaned up."
                                isError = !cleaned
                            }
                                .onFailure { feedback = it.message ?: "The backup could not be restored."; isError = true }
                            working = false
                        }
                    }
                ) { Text("Replace data") }
            },
            dismissButton = { TextButton(onClick = { pendingRestore = null }) { Text("Cancel") } }
        )
    }
}
