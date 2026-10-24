package com.example.petjournal.core.database

import androidx.room.Database
import androidx.room.migration.Migration
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.petjournal.data.local.dao.*
import com.example.petjournal.data.local.entity.*

@Database(
    entities = [
        PetEntity::class,
        HealthEventEntity::class,
        VaccinationDetailEntity::class,
        DewormingDetailEntity::class,
        MedicationDetailEntity::class,
        VetVisitDetailEntity::class,
        SymptomDetailEntity::class,
        WeightRecordEntity::class,
        JournalEntryEntity::class,
        CareReminderEntity::class,
        AttachmentEntity::class,
        HealthRecordEntity::class,
        HealthVaccinationDetailEntity::class,
        HealthMedicationDetailEntity::class,
        ReminderOccurrenceEntity::class,
        VaccineCatalogEntity::class
    ],
    version = 2,
    exportSchema = true
)
abstract class PetJournalDatabase : RoomDatabase() {
    abstract fun petDao(): PetDao
    abstract fun healthEventDao(): HealthEventDao
    abstract fun weightDao(): WeightDao
    abstract fun journalDao(): JournalDao
    abstract fun reminderDao(): ReminderDao
    abstract fun attachmentDao(): AttachmentDao
    abstract fun healthRecordDao(): HealthRecordDao
    abstract fun vaccineCatalogDao(): VaccineCatalogDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `health_records` (`id` TEXT NOT NULL, `petId` TEXT NOT NULL, `type` TEXT NOT NULL, `title` TEXT NOT NULL, `occurredAt` TEXT, `notes` TEXT, `veterinarianOrClinic` TEXT, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, `deletedAt` INTEGER, PRIMARY KEY(`id`), FOREIGN KEY(`petId`) REFERENCES `pets`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)"
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_health_records_petId` ON `health_records` (`petId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_health_records_type` ON `health_records` (`type`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_health_records_occurredAt` ON `health_records` (`occurredAt`)")
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `vaccination_record_details` (`healthRecordId` TEXT NOT NULL, `vaccineCatalogId` TEXT, `customVaccineName` TEXT, `nextDueAt` TEXT, `batchNumber` TEXT, PRIMARY KEY(`healthRecordId`), FOREIGN KEY(`healthRecordId`) REFERENCES `health_records`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)"
                )
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `medication_record_details` (`healthRecordId` TEXT NOT NULL, `medicationName` TEXT NOT NULL, `instructions` TEXT, `startDate` TEXT NOT NULL, `endDate` TEXT, PRIMARY KEY(`healthRecordId`), FOREIGN KEY(`healthRecordId`) REFERENCES `health_records`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)"
                )
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `reminder_occurrences` (`id` TEXT NOT NULL, `reminderId` TEXT NOT NULL, `scheduledAt` INTEGER NOT NULL, `status` TEXT NOT NULL, `completedAt` INTEGER, `actualActionAt` INTEGER, `notes` TEXT, PRIMARY KEY(`id`), FOREIGN KEY(`reminderId`) REFERENCES `care_reminders`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)"
                )
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `vaccine_catalog` (`id` TEXT NOT NULL, `species` TEXT NOT NULL, `name` TEXT NOT NULL, `category` TEXT NOT NULL, `guidanceNote` TEXT NOT NULL, `sourceUrl` TEXT NOT NULL, `lastVerifiedAt` TEXT NOT NULL, PRIMARY KEY(`id`))"
                )
            }
        }
    }
}
