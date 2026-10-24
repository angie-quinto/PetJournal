package com.example.petjournal.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.petjournal.core.database.PetJournalDatabase
import com.example.petjournal.data.local.entity.PetEntity
import com.example.petjournal.data.repository.HealthRepositoryImpl
import com.example.petjournal.domain.model.HealthEventInput
import com.example.petjournal.domain.model.SymptomDetail
import com.example.petjournal.domain.model.VaccinationDetail
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class HealthRepositoryTest {
    @Test
    fun healthEventsSaveAndUpdateSpecializedDetailsTransactionally() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val database = Room.inMemoryDatabaseBuilder(context, PetJournalDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        database.petDao().insertPet(PetEntity("pet-1", "Mochi", "DOG", createdAt = 1L, updatedAt = 1L))
        val repository = HealthRepositoryImpl(database.healthEventDao(), database.weightDao())

        val eventId = repository.saveEvent(
            HealthEventInput(
                petId = "pet-1",
                type = "VACCINATION",
                title = "Core vaccine",
                eventDate = "2026-10-21",
                vaccinationDetail = VaccinationDetail("Core vaccine", "1 dose", "batch-1", "Clinic")
            )
        )
        val original = repository.observeEvents("pet-1").first().single()
        assertEquals("Core vaccine", original.vaccinationDetail?.vaccineName)

        repository.saveEvent(
            HealthEventInput(
                id = eventId,
                petId = "pet-1",
                type = "VACCINATION",
                title = "Updated vaccine visit",
                eventDate = "2026-10-22"
            )
        )
        val edited = repository.observeEvents("pet-1").first().single()
        assertEquals(original.createdAt, edited.createdAt)
        assertEquals("Core vaccine", edited.vaccinationDetail?.vaccineName)

        repository.saveEvent(
            HealthEventInput(
                id = eventId,
                petId = "pet-1",
                type = "SYMPTOM",
                title = "Cough",
                eventDate = "2026-10-23",
                symptomDetail = SymptomDetail("Cough", "MILD", null, null)
            )
        )
        val changedType = repository.observeEvents("pet-1").first().single()
        assertNull(changedType.vaccinationDetail)
        assertEquals("Cough", changedType.symptomDetail?.description)
        database.close()
    }
}
