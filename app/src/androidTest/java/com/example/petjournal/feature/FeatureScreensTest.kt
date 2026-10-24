package com.example.petjournal.feature

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.runtime.mutableStateOf
import com.example.petjournal.domain.model.HealthEvent
import com.example.petjournal.domain.model.Pet
import com.example.petjournal.domain.model.WeightRecord
import com.example.petjournal.domain.model.Pet as PetModel
import com.example.petjournal.feature.health.HealthScreen
import com.example.petjournal.feature.reminders.ReminderScreen
import com.example.petjournal.feature.timeline.TimelineScreen
import com.example.petjournal.ui.theme.PetJournalTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4

@RunWith(AndroidJUnit4::class)
class FeatureScreensTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun healthScreenExplainsHowToBeginWithoutPets() {
        composeRule.setContent {
            PetJournalTheme {
                HealthScreen(
                    pet = null,
                    pets = emptyList(),
                    selectedPetId = "",
                    onSelectPet = {},
                    events = emptyList<HealthEvent>(),
                    weights = emptyList<WeightRecord>(),
                    onSaveEvent = {},
                    onDeleteEvent = {},
                    onRecordWeight = {}
                )
            }
        }
        composeRule.onNodeWithText("Add a pet to start tracking health and weight.").assertIsDisplayed()
    }

    @Test
    fun healthScreenLetsUserSelectAnotherPet() {
        val pets = listOf(testPet("pet-1", "Mochi"), testPet("pet-2", "Nori"))
        val selectedPetId = mutableStateOf(pets.first().id)
        composeRule.setContent {
            val selected = selectedPetId.value
            PetJournalTheme {
                HealthScreen(
                    pet = pets.first { it.id == selected },
                    pets = pets,
                    selectedPetId = selected,
                    onSelectPet = { selectedPetId.value = it },
                    events = emptyList(),
                    weights = emptyList(),
                    onSaveEvent = {},
                    onDeleteEvent = {},
                    onRecordWeight = {}
                )
            }
        }

        composeRule.onNodeWithText("Nori").performClick()
        assert(selectedPetId.value == "pet-2")
    }

    @Test
    fun careScreenExplainsThatPetIsRequired() {
        composeRule.setContent {
            PetJournalTheme {
                ReminderScreen(
                    pet = null,
                    reminders = emptyList(),
                    onSave = {},
                    onComplete = {}
                )
            }
        }
        composeRule.onNodeWithText("Add a pet before creating care reminders.").assertIsDisplayed()
    }

    @Test
    fun timelineShowsItsEmptyState() {
        composeRule.setContent {
            PetJournalTheme {
                TimelineScreen(
                    pets = emptyList<Pet>(),
                    selectedPetId = "",
                    onSelectPet = {},
                    records = emptyList(),
                    onAddHealth = {},
                    onAddJournal = {},
                    onDelete = {}
                )
            }
        }
        composeRule.onNodeWithText("Pet timeline").assertIsDisplayed()
        composeRule.onNodeWithText("Your pet's health, weight, and memories will appear here.").assertIsDisplayed()
    }

    private fun testPet(id: String, name: String) = PetModel(
        id = id,
        name = name,
        species = "CAT",
        customSpecies = null,
        breed = null,
        sex = null,
        birthDate = null,
        birthDatePrecision = "EXACT",
        adoptionDate = null,
        weightUnitPreference = "KG",
        photoPath = null,
        microchipNumber = null,
        medicalNotes = null,
        createdAt = 1L,
        updatedAt = 1L,
        archivedAt = null
    )
}
