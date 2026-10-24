package com.example.petjournal

import android.app.Application
import androidx.room.Room
import com.example.petjournal.core.database.PetJournalDatabase
import com.example.petjournal.data.repository.*

class PetJournalApp : Application() {

    val database by lazy {
        Room.databaseBuilder(
            applicationContext,
            PetJournalDatabase::class.java,
            "pet_journal.db"
        )
            .addMigrations(PetJournalDatabase.MIGRATION_1_2)
            .build()
    }

    val petRepository by lazy { PetRepositoryImpl(database.petDao()) }
    val healthRepository by lazy { HealthRepositoryImpl(database.healthEventDao(), database.weightDao()) }
    val journalRepository by lazy { JournalRepositoryImpl(database.journalDao()) }
    val reminderRepository by lazy { ReminderRepositoryImpl(database.reminderDao()) }
    val healthRecordRepository by lazy { HealthRecordRepositoryImpl(database.healthRecordDao()) }
    val attachmentRepository by lazy { AttachmentRepositoryImpl(applicationContext, database.attachmentDao()) }

    override fun onCreate() {
        super.onCreate()
    }
}
