package com.example.petjournal.data

import android.content.Context
import android.net.Uri
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.petjournal.core.backup.PetJournalBackupService
import com.example.petjournal.core.database.PetJournalDatabase
import com.example.petjournal.data.local.entity.AttachmentEntity
import com.example.petjournal.data.local.entity.PetEntity
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File
import java.io.IOException
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

@RunWith(RobolectricTestRunner::class)
class PetJournalBackupTest {
    @Test
    fun backupRoundTripReplacesExistingPetRecords() {
        runBlocking {
            val context = ApplicationProvider.getApplicationContext<Context>()
            val suffix = UUID.randomUUID().toString()
            val source = openDatabase(context, "backup-source-$suffix.db")
            val target = openDatabase(context, "backup-target-$suffix.db")
            val sourcePhoto = File(context.filesDir, "pet_images/source-$suffix.jpg").apply {
                parentFile?.mkdirs()
                writeText("pet photo bytes")
            }
            val sourceAttachment = File(context.filesDir, "attachments/journal-$suffix.jpg").apply {
                parentFile?.mkdirs()
                writeText("journal photo bytes")
            }
            val oldPhoto = File(context.filesDir, "pet_images/old-$suffix.jpg").apply { writeText("old photo") }
            val oldAttachment = File(context.filesDir, "attachments/old-$suffix.jpg").apply { writeText("old attachment") }
            source.petDao().insertPet(pet("backup-pet", "Mochi", sourcePhoto.absolutePath))
            source.attachmentDao().insertAttachment(
                AttachmentEntity(
                    id = "source-attachment",
                    petId = "backup-pet",
                    journalEntryId = "entry-1",
                    relativeFilePath = "attachments/${sourceAttachment.name}",
                    mimeType = "image/jpeg",
                    createdAt = 10L
                )
            )
            target.petDao().insertPet(pet("existing-pet", "Existing", oldPhoto.absolutePath))
            target.attachmentDao().insertAttachment(
                AttachmentEntity(
                    id = "old-attachment",
                    petId = "existing-pet",
                    journalEntryId = "old-entry",
                    relativeFilePath = "attachments/${oldAttachment.name}",
                    mimeType = "image/jpeg",
                    createdAt = 10L
                )
            )
            val archive = File(context.cacheDir, "backup-$suffix.zip")

            PetJournalBackupService.export(context, source, Uri.fromFile(archive))
            val cleaned = PetJournalBackupService.restore(context, target, Uri.fromFile(archive))

            assertEquals(true, cleaned)
            val restoredPet = target.petDao().getPetById("backup-pet")
            assertEquals("Mochi", restoredPet?.name)
            assertEquals(null, target.petDao().getPetById("existing-pet"))
            assertEquals(true, File(restoredPet!!.photoPath!!).isFile)
            val restoredAttachment = target.attachmentDao().getAttachmentsForEntry("entry-1").single()
            assertEquals(true, File(context.filesDir, restoredAttachment.relativeFilePath).isFile)
            assertFalse(oldPhoto.exists())
            assertFalse(oldAttachment.exists())

            source.close()
            target.close()
            context.deleteDatabase("backup-source-$suffix.db")
            context.deleteDatabase("backup-target-$suffix.db")
            archive.delete()
            sourcePhoto.delete()
            sourceAttachment.delete()
        }
    }

    @Test
    fun malformedArchiveDoesNotReplaceExistingRecords() {
        runBlocking {
            val context = ApplicationProvider.getApplicationContext<Context>()
            val suffix = UUID.randomUUID().toString()
            val target = openDatabase(context, "invalid-restore-$suffix.db")
            target.petDao().insertPet(pet("keep-pet", "Keep"))
            val archive = File(context.cacheDir, "invalid-$suffix.zip")
            ZipOutputStream(archive.outputStream()).use { zip ->
                zip.putNextEntry(ZipEntry("../../escape"))
                zip.write("no".toByteArray())
                zip.closeEntry()
            }

            assertThrows(IOException::class.java) {
                PetJournalBackupService.restore(context, target, Uri.fromFile(archive))
            }
            assertEquals("Keep", target.petDao().getPetById("keep-pet")?.name)
            assertFalse(File(context.filesDir, "escape").exists())

            target.close()
            context.deleteDatabase("invalid-restore-$suffix.db")
            archive.delete()
        }
    }

    private fun openDatabase(context: Context, name: String) =
        Room.databaseBuilder(context, PetJournalDatabase::class.java, name)
            .addMigrations(PetJournalDatabase.MIGRATION_1_2)
            .allowMainThreadQueries()
            .build()

    private fun pet(id: String, name: String, photoPath: String? = null) = PetEntity(
        id = id,
        name = name,
        species = "DOG",
        photoPath = photoPath,
        createdAt = 1L,
        updatedAt = 1L
    )
}
