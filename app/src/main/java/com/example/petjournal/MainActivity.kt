package com.example.petjournal

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.petjournal.domain.model.CareReminder
import com.example.petjournal.feature.dashboard.DashboardScreen
import com.example.petjournal.feature.backup.DataToolsScreen
import com.example.petjournal.feature.health.HealthScreen
import com.example.petjournal.feature.health.HealthViewModel
import com.example.petjournal.feature.journal.AddJournalEntryScreen
import com.example.petjournal.feature.journal.JournalScreen
import com.example.petjournal.feature.journal.JournalViewModel
import com.example.petjournal.feature.pets.PetAddEditScreen
import com.example.petjournal.feature.pets.PetListScreen
import com.example.petjournal.feature.pets.PetViewModel
import com.example.petjournal.feature.reminders.ReminderScheduler
import com.example.petjournal.feature.reminders.ReminderScreen
import com.example.petjournal.feature.reminders.ReminderViewModel
import com.example.petjournal.feature.timeline.TimelineRecord
import com.example.petjournal.feature.timeline.TimelineScreen
import com.example.petjournal.ui.theme.PetJournalTheme
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as PetJournalApp
        setContent {
            PetJournalTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainAppNavHost(app)
                }
            }
        }
    }
}

@Composable
fun MainAppNavHost(app: PetJournalApp) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val petViewModel: PetViewModel = viewModel(
        factory = PetViewModel.provideFactory(app.petRepository)
    )
    val reminderViewModel: ReminderViewModel = viewModel(
        factory = ReminderViewModel.provideFactory(app.reminderRepository)
    )

    val pets by petViewModel.pets.collectAsState()
    val reminders by reminderViewModel.reminders.collectAsState()

    var pendingReminder by remember { mutableStateOf<CareReminder?>(null) }
    val reminderScheduler = remember { ReminderScheduler(context) }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        pendingReminder?.let { reminder ->
            if (granted) reminderScheduler.schedule(reminder)
        }
        pendingReminder = null
    }
    val syncReminderNotification: (CareReminder) -> Unit = { reminder ->
        if (
            reminder.notificationEnabled &&
            Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            pendingReminder = reminder
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            reminderScheduler.schedule(reminder)
        }
    }

    var selectedJournalPetId by rememberSaveable { mutableStateOf("") }
    val currentPetId = if (selectedJournalPetId.isNotBlank() && pets.any { it.id == selectedJournalPetId }) {
        selectedJournalPetId
    } else {
        pets.firstOrNull()?.id ?: ""
    }

    val journalViewModel: JournalViewModel = viewModel(
        key = "journal:$currentPetId",
        factory = JournalViewModel.provideFactory(app.journalRepository, app.attachmentRepository, currentPetId)
    )
    val journalEntries by journalViewModel.entries.collectAsState()
    val journalAttachments by journalViewModel.attachments.collectAsState()
    val attachmentError by journalViewModel.attachmentError.collectAsState()
    val healthViewModel: HealthViewModel = viewModel(
        key = "health:$currentPetId",
        factory = HealthViewModel.provideFactory(app.healthRepository, currentPetId)
    )
    val healthEvents by healthViewModel.events.collectAsState()
    val weightHistory by healthViewModel.weightHistory.collectAsState()
    val currentPet = pets.find { it.id == currentPetId }
    val timelineRecords = remember(currentPetId, healthEvents, weightHistory, journalEntries, currentPet) {
        buildList {
            healthEvents.forEach { event ->
                add(
                    TimelineRecord(
                        id = event.id,
                        type = "HEALTH",
                        title = event.type.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() },
                        date = event.eventDate,
                        details = listOfNotNull(event.type.replace('_', ' '), event.providerName, event.notes)
                            .filter(String::isNotBlank)
                            .joinToString(" · "),
                        sortTimestamp = isoDateToEpoch(event.eventDate)
                    )
                )
            }
            weightHistory.forEach { weight ->
                val pounds = currentPet?.weightUnitPreference.equals("LB", ignoreCase = true) ||
                    currentPet?.weightUnitPreference.equals("LBS", ignoreCase = true)
                val amount = if (pounds) weight.weightGrams / 453.59237 else weight.weightGrams / 1000.0
                val date = Instant.ofEpochMilli(weight.measuredAt).atZone(ZoneId.systemDefault()).toLocalDate().toString()
                add(
                    TimelineRecord(
                        id = weight.id,
                        type = "WEIGHT",
                        title = String.format(java.util.Locale.getDefault(), "%.1f %s", amount, if (pounds) "lb" else "kg"),
                        date = date,
                        details = weight.notes.orEmpty(),
                        sortTimestamp = weight.measuredAt
                    )
                )
            }
            journalEntries.forEach { entry ->
                add(
                    TimelineRecord(
                        id = entry.id,
                        type = "JOURNAL",
                        title = entry.title,
                        date = entry.eventDate,
                        details = listOf(entry.category, entry.content).filter(String::isNotBlank).joinToString(" · "),
                        sortTimestamp = isoDateToEpoch(entry.eventDate)
                    )
                )
            }
        }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "home"

    val showBottomBar = currentRoute in listOf("home", "pets", "journal", "care", "health", "timeline")

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Home") },
                        selected = currentRoute == "home",
                        onClick = { navController.navigate("home") { popUpTo("home") } }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Favorite, contentDescription = "Health") },
                        label = { Text("Health") },
                        selected = currentRoute == "health",
                        onClick = { navController.navigate("health") { popUpTo("home") } }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Person, contentDescription = "Pets") },
                        label = { Text("Pets") },
                        selected = currentRoute == "pets",
                        onClick = { navController.navigate("pets") { popUpTo("home") } }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Journal") },
                        label = { Text("Journal") },
                        selected = currentRoute == "journal",
                        onClick = { navController.navigate("journal") { popUpTo("home") } }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Favorite, contentDescription = "Care") },
                        label = { Text("Care") },
                        selected = currentRoute == "care",
                        onClick = { navController.navigate("care") { popUpTo("home") } }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") {
                DashboardScreen(
                    pets = pets,
                    reminders = reminders.filter { it.status == "ACTIVE" },
                    onNavigateToPets = { navController.navigate("pets") },
                    onCompleteReminder = { id ->
                        reminderViewModel.completeReminder(id) { updated ->
                            if (updated != null) syncReminderNotification(updated)
                        }
                    },
                    onNavigateToTimeline = { navController.navigate("timeline") },
                    onNavigateToHealth = { navController.navigate("health") },
                    onNavigateToTools = { navController.navigate("tools") }
                )
            }
            composable("tools") {
                DataToolsScreen(
                    pets = pets,
                    database = app.database,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable("health") {
                HealthScreen(
                    pet = currentPet,
                    pets = pets,
                    selectedPetId = currentPetId,
                    onSelectPet = { petId -> selectedJournalPetId = petId },
                    events = healthEvents,
                    weights = weightHistory,
                    onSaveEvent = { input -> healthViewModel.saveEvent(input) {} },
                    onDeleteEvent = { id -> healthViewModel.deleteEvent(id) },
                    onRecordWeight = { input -> healthViewModel.recordWeight(input) {} }
                )
            }
            composable("timeline") {
                TimelineScreen(
                    pets = pets,
                    selectedPetId = currentPetId,
                    onSelectPet = { petId -> selectedJournalPetId = petId },
                    records = timelineRecords,
                    onAddHealth = { navController.navigate("health") },
                    onAddJournal = { navController.navigate("journal_add") },
                    onDelete = { record ->
                        when (record.type) {
                            "HEALTH" -> healthViewModel.deleteEvent(record.id)
                            "JOURNAL" -> journalViewModel.deleteEntry(record.id)
                        }
                    }
                )
            }
            composable("pets") {
                PetListScreen(
                    pets = pets,
                    onAddPetClick = { navController.navigate("pet_add") },
                    onEditPetClick = { petId -> navController.navigate("pet_edit/$petId") }
                )
            }
            composable("pet_add") {
                PetAddEditScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onSavePet = { input ->
                        petViewModel.createPet(input) {
                            navController.popBackStack()
                        }
                    }
                )
            }
            composable(
                route = "pet_edit/{petId}",
                arguments = listOf(navArgument("petId") { type = NavType.StringType })
            ) { backStackEntry ->
                val petId = backStackEntry.arguments?.getString("petId")
                val pet = pets.find { it.id == petId }
                PetAddEditScreen(
                    existingPet = pet,
                    onNavigateBack = { navController.popBackStack() },
                    onSavePet = { input ->
                        if (petId != null) {
                            petViewModel.updatePet(petId, input) {
                                navController.popBackStack()
                            }
                        }
                    },
                    onDeletePet = if (petId != null) {
                        {
                            petViewModel.archivePet(petId)
                            navController.popBackStack()
                        }
                    } else null
                )
            }
            composable("journal") {
                if (pets.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Please add a pet first to view journal entries.")
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = { navController.navigate("pets") }) {
                                Text("Go to Pets")
                            }
                        }
                    }
                } else {
                    JournalScreen(
                        pets = pets,
                        selectedPetId = currentPetId,
                        onSelectPet = { petId -> selectedJournalPetId = petId },
                        entries = journalEntries,
                        attachments = journalAttachments,
                        attachmentError = attachmentError,
                        onAddEntryClick = { navController.navigate("journal_add") },
                        onDeleteEntry = { id -> journalViewModel.deleteEntry(id) },
                        onDeleteAttachment = { id -> journalViewModel.deleteAttachment(id) },
                        onClearAttachmentError = { journalViewModel.clearAttachmentError() }
                    )
                }
            }
            composable("journal_add") {
                AddJournalEntryScreen(
                    pets = pets,
                    initialPetId = currentPetId,
                    attachmentError = attachmentError,
                    onNavigateBack = { navController.popBackStack() },
                    onSaveEntry = { input, photos ->
                        journalViewModel.saveEntry(input, photos) {
                            navController.popBackStack()
                        }
                    }
                )
            }
            composable("care") {
                ReminderScreen(
                    pet = currentPet,
                    reminders = reminders,
                    onSave = { input ->
                        reminderViewModel.saveReminder(input) { id ->
                            val now = System.currentTimeMillis()
                            syncReminderNotification(
                                CareReminder(
                                    id = id,
                                    petId = input.petId,
                                    healthEventId = input.healthEventId,
                                    title = input.title,
                                    notes = input.notes,
                                    dueAt = input.dueAt,
                                    recurrenceType = input.recurrenceType,
                                    recurrenceRule = input.recurrenceRule,
                                    status = "ACTIVE",
                                    completedAt = null,
                                    notificationEnabled = input.notificationEnabled,
                                    createdAt = now,
                                    updatedAt = now
                                )
                            )
                        }
                    },
                    onComplete = { reminder ->
                        reminderViewModel.completeReminder(reminder.id) { updated ->
                            if (updated != null) syncReminderNotification(updated)
                            else reminderScheduler.cancel(reminder.id)
                        }
                    }
                )
            }
        }
    }
}

private fun isoDateToEpoch(date: String): Long = runCatching {
    LocalDate.parse(date).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
}.getOrDefault(0L)
