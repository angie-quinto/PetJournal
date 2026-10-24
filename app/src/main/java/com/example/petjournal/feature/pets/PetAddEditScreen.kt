package com.example.petjournal.feature.pets

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.petjournal.domain.model.Pet
import com.example.petjournal.domain.model.PetInput
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetAddEditScreen(
    existingPet: Pet? = null,
    onNavigateBack: () -> Unit,
    onSavePet: (PetInput) -> Unit,
    onDeletePet: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val isEditMode = existingPet != null

    var name by remember { mutableStateOf(existingPet?.name ?: "") }
    var species by remember { mutableStateOf(existingPet?.species ?: "Dog") }
    var breed by remember { mutableStateOf(existingPet?.breed ?: "Aspin") }
    var sex by remember { mutableStateOf(existingPet?.sex ?: "Male") }
    var birthDate by remember { mutableStateOf(existingPet?.birthDate ?: "") }
    var weightUnit by remember { mutableStateOf(existingPet?.weightUnitPreference ?: "KG") }
    var microchip by remember { mutableStateOf(existingPet?.microchipNumber ?: "") }
    var notes by remember { mutableStateOf(existingPet?.medicalNotes ?: "") }
    var photoPath by remember { mutableStateOf(existingPet?.photoPath ?: "") }

    var nameError by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showCropDialog by remember { mutableStateOf(false) }

    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    var speciesExpanded by remember { mutableStateOf(false) }
    var breedExpanded by remember { mutableStateOf(false) }
    var sexExpanded by remember { mutableStateOf(false) }

    val dogBreeds = listOf("Aspin", "Golden Retriever", "Shih Tzu", "Poodle", "Labrador", "German Shepherd", "Chihuahua", "Other")
    val catBreeds = listOf("Puspin", "Persian", "Siamese", "Maine Coon", "British Shorthair", "Other")
    val availableBreeds = if (species == "Dog") dogBreeds else if (species == "Cat") catBreeds else listOf("Other")

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            photoPath = saveImageToInternalStorage(context, selectedUri)
            showCropDialog = true
        }
    }

    if (showCropDialog && photoPath.isNotBlank()) {
        PhotoCropDialog(
            photoPath = photoPath,
            onDismiss = { showCropDialog = false },
            onConfirm = { croppedBitmap ->
                photoPath = saveBitmapToInternalStorage(context, croppedBitmap)
                showCropDialog = false
            }
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Pet") },
            text = { Text("Are you sure you want to delete ${existingPet?.name ?: "this pet"}? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        onDeletePet?.invoke()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        birthDate = formatter.format(Date(millis))
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
                title = { Text(if (isEditMode) "Edit Pet" else "Add Pet") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (isEditMode && onDeletePet != null) {
                        IconButton(onClick = { showDeleteConfirm = true }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete Pet",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Photo Picker Avatar
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .clickable {
                        if (photoPath.isNotBlank()) {
                            showCropDialog = true
                        } else {
                            photoPickerLauncher.launch(
                                androidx.activity.result.PickVisualMediaRequest(
                                    ActivityResultContracts.PickVisualMedia.ImageOnly
                                )
                            )
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                if (photoPath.isNotBlank()) {
                    val bitmap = remember(photoPath) {
                        try {
                            android.graphics.BitmapFactory.decodeFile(photoPath)
                        } catch (_: Exception) {
                            null
                        }
                    }
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Pet Photo",
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            Icons.Default.Favorite,
                            contentDescription = "Add Photo",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                } else {
                    Icon(
                        Icons.Default.Favorite,
                        contentDescription = "Add Photo",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (photoPath.isBlank()) "Tap to upload photo" else "Tap to adjust / change photo",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable {
                        photoPickerLauncher.launch(
                            androidx.activity.result.PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.ImageOnly
                            )
                        )
                    }
                )
                if (photoPath.isNotBlank()) {
                    Text(
                        text = "Crop / Fit",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.clickable { showCropDialog = true }
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    nameError = it.isBlank()
                },
                label = { Text("Pet Name *") },
                isError = nameError,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )

            // Species Dropdown
            ExposedDropdownMenuBox(
                expanded = speciesExpanded,
                onExpandedChange = { speciesExpanded = !speciesExpanded }
            ) {
                OutlinedTextField(
                    value = species,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Species *") },
                    shape = RoundedCornerShape(16.dp),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = speciesExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                )
                ExposedDropdownMenu(
                    expanded = speciesExpanded,
                    onDismissRequest = { speciesExpanded = false }
                ) {
                    listOf("Dog", "Cat", "Bird", "Rabbit", "Other").forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                species = option
                                breed = if (option == "Dog") "Aspin" else if (option == "Cat") "Puspin" else "Other"
                                speciesExpanded = false
                            }
                        )
                    }
                }
            }

            // Breed Dropdown
            ExposedDropdownMenuBox(
                expanded = breedExpanded,
                onExpandedChange = { breedExpanded = !breedExpanded }
            ) {
                OutlinedTextField(
                    value = breed,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Breed") },
                    shape = RoundedCornerShape(16.dp),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = breedExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                )
                ExposedDropdownMenu(
                    expanded = breedExpanded,
                    onDismissRequest = { breedExpanded = false }
                ) {
                    availableBreeds.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                breed = option
                                breedExpanded = false
                            }
                        )
                    }
                }
            }

            // Sex Dropdown
            ExposedDropdownMenuBox(
                expanded = sexExpanded,
                onExpandedChange = { sexExpanded = !sexExpanded }
            ) {
                OutlinedTextField(
                    value = sex,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Sex / Gender") },
                    shape = RoundedCornerShape(16.dp),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sexExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                )
                ExposedDropdownMenu(
                    expanded = sexExpanded,
                    onDismissRequest = { sexExpanded = false }
                ) {
                    listOf("Male", "Female", "Unknown").forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                sex = option
                                sexExpanded = false
                            }
                        )
                    }
                }
            }

            // Birth Date Picker Field
            OutlinedTextField(
                value = birthDate,
                onValueChange = {},
                readOnly = true,
                label = { Text("Birth Date (YYYY-MM-DD)") },
                shape = RoundedCornerShape(16.dp),
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Select Birth Date")
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDatePicker = true }
            )

            OutlinedTextField(
                value = microchip,
                onValueChange = { microchip = it },
                label = { Text("Microchip Number") },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Medical Notes / Allergies") },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    if (name.isBlank()) {
                        nameError = true
                    } else {
                        val input = PetInput(
                            name = name.trim(),
                            species = species.trim(),
                            breed = breed.ifBlank { null },
                            sex = sex.ifBlank { null },
                            birthDate = birthDate.ifBlank { null },
                            weightUnitPreference = weightUnit,
                            photoPath = photoPath.ifBlank { null },
                            microchipNumber = microchip.ifBlank { null },
                            medicalNotes = notes.ifBlank { null }
                        )
                        onSavePet(input)
                    }
                },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isEditMode) "Save Changes" else "Save Pet")
            }
        }
    }
}

@Composable
fun PhotoCropDialog(
    photoPath: String,
    onDismiss: () -> Unit,
    onConfirm: (Bitmap) -> Unit
) {
    val bitmap = remember(photoPath) {
        try {
            android.graphics.BitmapFactory.decodeFile(photoPath)
        } catch (_: Exception) {
            null
        }
    }

    var scale by remember { mutableStateOf(1f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Adjust Pet Photo (Drag & Pinch)") },
        text = {
            if (bitmap != null) {
                Box(
                    modifier = Modifier
                        .size(260.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                scale = (scale * zoom).coerceIn(0.5f, 5f)
                                offsetX += pan.x
                                offsetY += pan.y
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    val imageBitmap = remember(bitmap, scale, offsetX, offsetY) {
                        val size = 500
                        val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
                        val canvas = Canvas(output)
                        val matrix = Matrix().apply {
                            postScale(scale, scale)
                            postTranslate(offsetX + size / 2f - bitmap.width / 2f, offsetY + size / 2f - bitmap.height / 2f)
                        }
                        canvas.drawBitmap(bitmap, matrix, null)
                        output
                    }

                    Image(
                        bitmap = imageBitmap.asImageBitmap(),
                        contentDescription = "Adjusted Photo",
                        modifier = Modifier.fillMaxSize()
                    )
                }
            } else {
                Text("Unable to load image.")
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (bitmap != null) {
                        val size = 500
                        val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
                        val canvas = Canvas(output)
                        val matrix = Matrix().apply {
                            postScale(scale, scale)
                            postTranslate(offsetX + size / 2f - bitmap.width / 2f, offsetY + size / 2f - bitmap.height / 2f)
                        }
                        canvas.drawBitmap(bitmap, matrix, null)
                        onConfirm(output)
                    } else {
                        onDismiss()
                    }
                }
            ) {
                Text("Done")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun saveImageToInternalStorage(context: Context, uri: Uri): String {
    val imagesDir = File(context.filesDir, "pet_images").apply { mkdirs() }
    val file = File(imagesDir, "pet_${System.currentTimeMillis()}.jpg")
    try {
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
        }
    } catch (_: Exception) {
    }
    return file.absolutePath
}

private fun saveBitmapToInternalStorage(context: Context, bitmap: Bitmap): String {
    val imagesDir = File(context.filesDir, "pet_images").apply { mkdirs() }
    val file = File(imagesDir, "pet_crop_${System.currentTimeMillis()}.jpg")
    try {
        FileOutputStream(file).use { output ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, output)
        }
    } catch (_: Exception) {
    }
    return file.absolutePath
}
