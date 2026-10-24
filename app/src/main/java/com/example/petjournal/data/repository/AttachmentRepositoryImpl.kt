package com.example.petjournal.data.repository

import android.content.Context
import android.net.Uri
import com.example.petjournal.data.local.dao.AttachmentDao
import com.example.petjournal.data.local.entity.AttachmentEntity
import com.example.petjournal.domain.model.JournalAttachment
import com.example.petjournal.domain.repository.AttachmentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.File
import java.io.IOException
import java.util.UUID

class AttachmentRepositoryImpl(
    context: Context,
    private val attachmentDao: AttachmentDao
) : AttachmentRepository {
    private val appContext = context.applicationContext
    private val attachmentDirectory = File(appContext.filesDir, "attachments")

    override fun observeForPet(petId: String): Flow<List<JournalAttachment>> =
        attachmentDao.observeAttachments(petId).map { records -> records.map(::toDomain) }

    override suspend fun importJournalAttachment(
        petId: String,
        journalEntryId: String,
        uri: Uri
    ): JournalAttachment {
        val mimeType = appContext.contentResolver.getType(uri)
            ?.takeIf { it.startsWith("image/") }
            ?: throw IOException("The selected file is not a supported image.")
        val extension = mimeType.substringAfter('/').filter(Char::isLetterOrDigit).take(8).ifBlank { "img" }
        if (!attachmentDirectory.exists() && !attachmentDirectory.mkdirs()) {
            throw IOException("Unable to create the app's attachment folder.")
        }
        val id = UUID.randomUUID().toString()
        val relativePath = "attachments/$id.$extension"
        val destination = File(appContext.filesDir, relativePath)
        val temporary = File(attachmentDirectory, "$id.tmp")
        val input = appContext.contentResolver.openInputStream(uri)
            ?: throw IOException("The selected image could not be opened.")
        try {
            input.use { source ->
                temporary.outputStream().use { target -> source.copyTo(target) }
            }
            if (!temporary.renameTo(destination)) {
                throw IOException("Unable to save the selected image.")
            }
        } catch (error: IOException) {
            temporary.delete()
            throw error
        }

        val entity = AttachmentEntity(
            id = id,
            petId = petId,
            journalEntryId = journalEntryId,
            relativeFilePath = relativePath,
            mimeType = mimeType,
            createdAt = System.currentTimeMillis()
        )
        try {
            attachmentDao.insertAttachment(entity)
        } catch (error: RuntimeException) {
            destination.delete()
            throw error
        }
        return toDomain(entity)
    }

    override suspend fun deleteJournalAttachment(id: String) {
        val attachment = attachmentDao.getAttachmentById(id) ?: return
        attachmentDao.softDeleteAttachment(id, System.currentTimeMillis())
        deleteManagedFile(attachment.relativeFilePath)
    }

    override suspend fun deleteAttachmentsForEntry(journalEntryId: String) {
        attachmentDao.getAttachmentsForEntry(journalEntryId).forEach { attachment ->
            deleteJournalAttachment(attachment.id)
        }
    }

    private fun deleteManagedFile(relativePath: String) {
        val root = appContext.filesDir.canonicalFile
        val file = File(appContext.filesDir, relativePath).canonicalFile
        if (file.path.startsWith(root.path + File.separator)) file.delete()
    }

    private fun toDomain(entity: AttachmentEntity) = JournalAttachment(
        id = entity.id,
        petId = entity.petId,
        journalEntryId = entity.journalEntryId.orEmpty(),
        filePath = File(appContext.filesDir, entity.relativeFilePath).absolutePath,
        mimeType = entity.mimeType,
        caption = entity.caption
    )

}
