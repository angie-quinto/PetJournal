package com.example.petjournal.core.backup

import android.content.Context
import android.net.Uri
import android.database.Cursor
import androidx.room.RoomDatabase
import java.util.concurrent.Callable
import androidx.sqlite.db.SupportSQLiteDatabase
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonNull
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.io.File
import java.io.IOException
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

object PetJournalBackupService {
    private const val FORMAT_VERSION = 1
    private const val MAX_DATABASE_JSON_BYTES = 64L * 1024 * 1024
    private const val MAX_SINGLE_ATTACHMENT_BYTES = 100L * 1024 * 1024
    private const val MAX_TOTAL_ATTACHMENT_BYTES = 1024L * 1024 * 1024
    private const val MAX_ENTRIES = 10_000
    private const val BLOB_KEY = "__base64_blob"
    private val gson: Gson = GsonBuilder().serializeNulls().create()

    fun export(context: Context, database: RoomDatabase, destination: Uri) {
        val supportDatabase = database.openHelper.writableDatabase
        val tables = database.runInTransaction(Callable { readTables(supportDatabase) })
        val dbJson = JsonObject().apply {
            addProperty("schemaVersion", database.openHelper.writableDatabase.version)
            add("tables", tables)
        }
        val dbBytes = gson.toJson(dbJson).toByteArray(Charsets.UTF_8)
        if (dbBytes.size > MAX_DATABASE_JSON_BYTES) {
            throw IOException("The database is too large to export safely.")
        }
        val manifest = JsonObject().apply {
            addProperty("formatVersion", FORMAT_VERSION)
            addProperty("schemaVersion", database.openHelper.writableDatabase.version)
            addProperty("appVersion", appVersion(context))
            addProperty("exportedAt", System.currentTimeMillis())
            add("recordCounts", JsonObject().apply {
                tables.entrySet().forEach { (table, rows) -> addProperty(table, rows.asJsonArray.size()) }
            })
        }
        val fileEntries = collectReferencedFiles(context, tables)
        context.contentResolver.openOutputStream(destination)?.use { output ->
            ZipOutputStream(output).use { zip ->
                zip.putNextEntry(ZipEntry("manifest.json"))
                zip.write(gson.toJson(manifest).toByteArray(Charsets.UTF_8))
                zip.closeEntry()
                zip.putNextEntry(ZipEntry("database.json"))
                zip.write(dbBytes)
                zip.closeEntry()
                fileEntries.forEach { (name, file) ->
                    zip.putNextEntry(ZipEntry(name))
                    file.inputStream().use { it.copyTo(zip) }
                    zip.closeEntry()
                }
            }
        } ?: throw IOException("Unable to open the selected backup destination.")
    }

    fun validateBackup(context: Context, database: RoomDatabase, source: Uri) {
        val supportDatabase = database.openHelper.writableDatabase
        val tables = tableNames(supportDatabase)
        var manifest: JsonObject? = null
        var databaseJson: JsonObject? = null
        val extractedFiles = mutableSetOf<String>()
        var attachmentBytes = 0L
        val input = context.contentResolver.openInputStream(source)
            ?: throw IOException("Unable to open the selected backup.")
        ZipInputStream(input).use { zip ->
            var entryCount = 0
            while (true) {
                val entry = zip.nextEntry ?: break
                entryCount++
                if (entryCount > MAX_ENTRIES) throw IOException("The backup contains too many files.")
                if (entry.isDirectory) {
                    zip.closeEntry()
                    continue
                }
                when (entry.name) {
                    "manifest.json" -> {
                        if (manifest != null) throw IOException("The backup contains duplicate manifests.")
                        manifest = JsonParser.parseString(readLimited(zip, MAX_DATABASE_JSON_BYTES)).asJsonObject
                    }
                    "database.json" -> {
                        if (databaseJson != null) throw IOException("The backup contains duplicate database files.")
                        databaseJson = JsonParser.parseString(readLimited(zip, MAX_DATABASE_JSON_BYTES)).asJsonObject
                    }
                    else -> {
                        val relativePath = safeArchiveFilePath(entry.name)
                            ?: throw IOException("The backup contains an unsafe file path.")
                        if (!extractedFiles.add(relativePath)) throw IOException("The backup contains duplicate file paths.")
                        attachmentBytes += drainLimited(zip, MAX_SINGLE_ATTACHMENT_BYTES)
                        if (attachmentBytes > MAX_TOTAL_ATTACHMENT_BYTES) {
                            throw IOException("The backup attachments exceed the supported size.")
                        }
                    }
                }
                zip.closeEntry()
            }
        }
        val validatedManifest = manifest ?: throw IOException("The backup manifest is missing.")
        val archive = databaseJson ?: throw IOException("The backup data is missing.")
        if (validatedManifest.get("formatVersion")?.asInt != FORMAT_VERSION) {
            throw IOException("This backup format version is not supported.")
        }
        val schemaVersion = supportDatabase.version
        if (validatedManifest.get("schemaVersion")?.asInt != schemaVersion ||
            archive.get("schemaVersion")?.asInt != schemaVersion
        ) {
            throw IOException("This backup was created with an incompatible database version.")
        }
        val archiveTables = archive.getAsJsonObject("tables")
            ?: throw IOException("The backup database is malformed.")
        if (archiveTables.keySet() != tables.toSet()) {
            throw IOException("The backup does not contain the expected PetJournal tables.")
        }
        validateReferencedFiles(
            validateRows(archiveTables, tables, supportDatabase),
            extractedFiles,
            context.filesDir
        )
    }

    fun restore(context: Context, database: RoomDatabase, source: Uri): Boolean {
        val supportDatabase = database.openHelper.writableDatabase
        val tables = tableNames(supportDatabase)
        val restoreId = UUID.randomUUID().toString()
        val stagingDirectory = File(context.filesDir, ".restore-$restoreId")
        val restoreDirectory = File(context.filesDir, "restored_$restoreId")
        if (!stagingDirectory.mkdirs()) throw IOException("Unable to prepare a safe restore location.")
        var manifest: JsonObject? = null
        var databaseJson: JsonObject? = null
        val extractedFiles = mutableSetOf<String>()
        var attachmentBytes = 0L
        var transactionCommitted = false
        var existingFiles: Set<String> = emptySet()
        try {
            val input = context.contentResolver.openInputStream(source)
                ?: throw IOException("Unable to open the selected backup.")
            ZipInputStream(input).use { zip ->
                var entryCount = 0
                while (true) {
                    val entry = zip.nextEntry ?: break
                    entryCount += 1
                    if (entryCount > MAX_ENTRIES) throw IOException("The backup contains too many files.")
                    if (entry.isDirectory) {
                        zip.closeEntry()
                        continue
                    }
                    when (entry.name) {
                        "manifest.json" -> {
                            if (manifest != null) throw IOException("The backup contains duplicate manifests.")
                            manifest = JsonParser.parseString(readLimited(zip, MAX_DATABASE_JSON_BYTES)).asJsonObject
                        }
                        "database.json" -> {
                            if (databaseJson != null) throw IOException("The backup contains duplicate database files.")
                            databaseJson = JsonParser.parseString(readLimited(zip, MAX_DATABASE_JSON_BYTES)).asJsonObject
                        }
                        else -> {
                            val relativePath = safeArchiveFilePath(entry.name)
                                ?: throw IOException("The backup contains an unsafe file path.")
                            if (!extractedFiles.add(relativePath)) throw IOException("The backup contains duplicate file paths.")
                            val target = File(stagingDirectory, relativePath).canonicalFile
                            if (!target.path.startsWith(stagingDirectory.canonicalPath + File.separator)) {
                                throw IOException("The backup contains an unsafe file path.")
                            }
                            target.parentFile?.mkdirs()
                            val bytesWritten = copyLimited(zip, target, MAX_SINGLE_ATTACHMENT_BYTES)
                            attachmentBytes += bytesWritten
                            if (attachmentBytes > MAX_TOTAL_ATTACHMENT_BYTES) {
                                throw IOException("The backup attachments exceed the supported size.")
                            }
                        }
                    }
                    zip.closeEntry()
                }
            }

            val validatedManifest = manifest ?: throw IOException("The backup manifest is missing.")
            val archive = databaseJson ?: throw IOException("The backup data is missing.")
            if (validatedManifest.get("formatVersion")?.asInt != FORMAT_VERSION) {
                throw IOException("This backup format version is not supported.")
            }
            if (validatedManifest.get("schemaVersion")?.asInt != database.openHelper.writableDatabase.version ||
                archive.get("schemaVersion")?.asInt != database.openHelper.writableDatabase.version
            ) {
                throw IOException("This backup was created with an incompatible database version.")
            }
            val archiveTables = archive.getAsJsonObject("tables")
                ?: throw IOException("The backup database is malformed.")
            if (archiveTables.keySet() != tables.toSet()) {
                throw IOException("The backup does not contain the expected PetJournal tables.")
            }
            val snapshot = validateRows(archiveTables, tables, supportDatabase)
            validateReferencedFiles(snapshot, extractedFiles, context.filesDir)

            if (!stagingDirectory.renameTo(restoreDirectory)) {
                throw IOException("Unable to stage the restored attachments.")
            }

            try {
                supportDatabase.beginTransaction()
                existingFiles = readExistingFileReferences(supportDatabase)
                val insertionOrder = dependencyOrder(supportDatabase, tables)
                insertionOrder.asReversed().forEach { table ->
                    supportDatabase.execSQL("DELETE FROM `${quoteIdentifier(table)}`")
                }
                insertionOrder.forEach { table ->
                    val rows = snapshot.getAsJsonArray(table)
                    rows.forEach { row ->
                        insertRow(
                            supportDatabase,
                            table,
                            row.asJsonObject,
                            restoreDirectory,
                            context.filesDir
                        )
                    }
                }
                supportDatabase.setTransactionSuccessful()
            } finally {
                supportDatabase.endTransaction()
            }
            transactionCommitted = true
            return existingFiles.all { deleteManagedFile(context.filesDir, it, restoreDirectory) }
        } catch (error: Exception) {
            if (!transactionCommitted) {
                restoreDirectory.deleteRecursively()
                if (error is IOException) throw error
                throw IOException("The backup could not be restored. Existing app data was left unchanged.", error)
            }
            throw IOException("The backup was restored, but local file cleanup did not finish.", error)
        } finally {
            stagingDirectory.deleteRecursively()
        }
    }

    private fun readTables(database: SupportSQLiteDatabase): JsonObject {
        val result = JsonObject()
        val names = tableNames(database)
        names.forEach { table ->
            val rows = JsonArray()
            database.query("SELECT * FROM `${quoteIdentifier(table)}`").use { cursor ->
                while (cursor.moveToNext()) {
                    val row = JsonObject()
                    cursor.columnNames.forEachIndexed { index, column ->
                        row.add(column, cursorValue(cursor, index))
                    }
                    rows.add(row)
                }
            }
            result.add(table, rows)
        }
        return result
    }

    private fun tableNames(database: SupportSQLiteDatabase): List<String> {
        val names = mutableListOf<String>()
        database.query(
            "SELECT name FROM sqlite_master WHERE type = 'table' AND name NOT LIKE 'sqlite_%' AND name NOT IN ('room_master_table', 'android_metadata') ORDER BY name"
        ).use { cursor ->
            while (cursor.moveToNext()) names += cursor.getString(0)
        }
        return names
    }

    private fun cursorValue(cursor: Cursor, index: Int): JsonElement = when (cursor.getType(index)) {
        Cursor.FIELD_TYPE_NULL -> JsonNull.INSTANCE
        Cursor.FIELD_TYPE_INTEGER -> gson.toJsonTree(cursor.getLong(index))
        Cursor.FIELD_TYPE_FLOAT -> gson.toJsonTree(cursor.getDouble(index))
        Cursor.FIELD_TYPE_BLOB -> JsonObject().apply {
            addProperty(BLOB_KEY, android.util.Base64.encodeToString(cursor.getBlob(index), android.util.Base64.NO_WRAP))
        }
        else -> gson.toJsonTree(cursor.getString(index))
    }

    private fun collectReferencedFiles(context: Context, tables: JsonObject): Map<String, File> {
        val paths = mutableSetOf<String>()
        tables.getAsJsonArray("attachments")?.forEach { row ->
            val attachment = row.asJsonObject
            if (attachment.get("deletedAt")?.isJsonNull != false) {
                attachment.get("relativeFilePath")?.asString?.let(paths::add)
            }
        }
        tables.getAsJsonArray("pets")?.forEach { row ->
            row.asJsonObject.get("photoPath")?.takeUnless(JsonElement::isJsonNull)?.asString?.let(paths::add)
        }
        return paths.associate { rawPath ->
            val candidate = File(rawPath).let { if (it.isAbsolute) it else File(context.filesDir, rawPath) }.canonicalFile
            if (!candidate.path.startsWith(context.filesDir.canonicalPath + File.separator) || !candidate.isFile) {
                throw IOException("A referenced photo is missing or outside app storage; the backup was not created.")
            }
            val relative = candidate.relativeTo(context.filesDir.canonicalFile).invariantSeparatorsPath
            "files/$relative" to candidate
        }
    }

    private fun validateRows(
        archiveTables: JsonObject,
        tableNames: List<String>,
        database: SupportSQLiteDatabase
    ): JsonObject {
        val snapshot = JsonObject()
        tableNames.forEach { table ->
            val columns = mutableSetOf<String>()
            database.query("PRAGMA table_info(`${quoteIdentifier(table)}`)").use { cursor ->
                while (cursor.moveToNext()) columns += cursor.getString(cursor.getColumnIndexOrThrow("name"))
            }
            if (columns.isEmpty()) throw IOException("The database schema for $table could not be read.")
            val rows = archiveTables.getAsJsonArray(table)
                ?: throw IOException("The backup is missing table $table.")
            if (rows.size() > 1_000_000) throw IOException("The backup has too many records.")
            rows.forEach { row ->
                if (!row.isJsonObject) throw IOException("The backup contains a malformed record.")
                val rowColumns = row.asJsonObject.keySet()
                if (rowColumns != columns) {
                    throw IOException(
                        "A backup record in $table does not match this app's schema " +
                            "(missing=${columns - rowColumns}, unknown=${rowColumns - columns})."
                    )
                }
            }
            snapshot.add(table, rows)
        }
        if (tableNames.isEmpty()) throw IOException("The backup does not contain any PetJournal tables.")
        return snapshot
    }

    private fun validateReferencedFiles(
        snapshot: JsonObject,
        archiveFiles: Set<String>,
        filesDirectory: File
    ) {
        val referenced = mutableSetOf<String>()
        snapshot.getAsJsonArray("attachments")?.forEach { row ->
            val attachment = row.asJsonObject
            if (attachment.get("deletedAt")?.isJsonNull != false) {
                attachment.get("relativeFilePath")?.asString?.let { referenced += "files/${safeRelativeFilePath(it)}" }
            }
        }
        snapshot.getAsJsonArray("pets")?.forEach { row ->
            val value = row.asJsonObject.get("photoPath")?.takeUnless(JsonElement::isJsonNull)?.asString
                ?: return@forEach
            val file = File(value)
            if (file.isAbsolute) {
                val canonical = file.canonicalFile
                if (!canonical.path.startsWith(filesDirectory.canonicalPath + File.separator)) {
                    throw IOException("A pet photo path is outside app storage.")
                }
                referenced += "files/${safeRelativeFilePath(canonical.relativeTo(filesDirectory.canonicalFile).invariantSeparatorsPath)}"
            } else {
                referenced += "files/${safeRelativeFilePath(value)}"
            }
        }
        if (archiveFiles.map { "files/$it" }.toSet() != referenced) {
            throw IOException("The backup is missing one or more referenced photos.")
        }
    }

    private fun readExistingFileReferences(database: SupportSQLiteDatabase): Set<String> {
        val paths = mutableSetOf<String>()
        database.query("SELECT relativeFilePath FROM attachments WHERE deletedAt IS NULL").use { cursor ->
            while (cursor.moveToNext()) paths += cursor.getString(0)
        }
        database.query("SELECT photoPath FROM pets WHERE photoPath IS NOT NULL").use { cursor ->
            while (cursor.moveToNext()) paths += cursor.getString(0)
        }
        return paths
    }

    private fun dependencyOrder(database: SupportSQLiteDatabase, tables: List<String>): List<String> {
        val parents = tables.associateWith { table ->
            buildSet {
                database.query("PRAGMA foreign_key_list(`${quoteIdentifier(table)}`)").use { cursor ->
                    while (cursor.moveToNext()) {
                        val parent = cursor.getString(cursor.getColumnIndexOrThrow("table"))
                        if (parent in tables) add(parent)
                    }
                }
            }
        }
        val result = mutableListOf<String>()
        val visiting = mutableSetOf<String>()
        val visited = mutableSetOf<String>()
        fun visit(table: String) {
            if (table in visited) return
            if (!visiting.add(table)) throw IOException("The database contains an unsupported foreign-key cycle.")
            parents[table].orEmpty().forEach(::visit)
            visiting.remove(table)
            visited.add(table)
            result += table
        }
        tables.forEach(::visit)
        return result
    }

    private fun insertRow(
        database: SupportSQLiteDatabase,
        table: String,
        row: JsonObject,
        restoreDirectory: File,
        filesDirectory: File
    ) {
        val columns = row.keySet().toList()
        if (columns.isEmpty()) return
        val values = columns.map { column ->
            val value = row.get(column)
            when {
                table == "attachments" && column == "relativeFilePath" && !value.isJsonNull -> {
                    "${restoreDirectory.name}/${safeRelativeFilePath(value.asString)}"
                }
                table == "pets" && column == "photoPath" && !value.isJsonNull -> {
                    val original = value.asString
                    val relative = if (File(original).isAbsolute) {
                        val file = File(original).canonicalFile
                        val root = filesDirectory.canonicalFile
                        if (!file.path.startsWith(root.path + File.separator)) {
                            throw IOException("A pet photo path is outside app storage.")
                        }
                        safeRelativeFilePath(file.relativeTo(root).invariantSeparatorsPath)
                    } else {
                        safeRelativeFilePath(original)
                    }
                    File(restoreDirectory, relative).absolutePath
                }
                value.isJsonNull -> null
                value.isJsonObject && value.asJsonObject.has(BLOB_KEY) ->
                    android.util.Base64.decode(value.asJsonObject.get(BLOB_KEY).asString, android.util.Base64.DEFAULT)
                value.isJsonPrimitive && value.asJsonPrimitive.isBoolean -> if (value.asBoolean) 1L else 0L
                value.isJsonPrimitive && value.asJsonPrimitive.isNumber ->
                    value.asString.toLongOrNull() ?: value.asString.toDouble()
                else -> value.asString
            }
        }.toTypedArray()
        val placeholders = columns.joinToString(",") { "?" }
        val quotedColumns = columns.joinToString(",") { "`${quoteIdentifier(it)}`" }
        database.execSQL(
            "INSERT INTO `${quoteIdentifier(table)}` ($quotedColumns) VALUES ($placeholders)",
            values
        )
    }

    private fun safeArchiveFilePath(name: String): String? {
        if (!name.startsWith("files/")) return null
        return safeRelativeFilePath(name.removePrefix("files/"))
    }

    private fun safeRelativeFilePath(path: String): String {
        if (path.isBlank() || path.startsWith('/') || path.contains('\\') ||
            path.split('/').any { it.isBlank() || it == "." || it == ".." }
        ) throw IOException("The backup contains an unsafe file path.")
        return path
    }

    private fun quoteIdentifier(value: String): String {
        if (!value.matches(Regex("[A-Za-z0-9_]+"))) throw IOException("The backup contains an invalid database identifier.")
        return value
    }

    private fun readLimited(input: ZipInputStream, maxBytes: Long): String {
        val out = java.io.ByteArrayOutputStream()
        val buffer = ByteArray(8192)
        var total = 0L
        while (true) {
            val count = input.read(buffer)
            if (count < 0) break
            total += count
            if (total > maxBytes) throw IOException("The backup data is too large.")
            out.write(buffer, 0, count)
        }
        return out.toString(Charsets.UTF_8.name())
    }

    private fun copyLimited(input: ZipInputStream, destination: File, maxBytes: Long): Long {
        destination.outputStream().use { output ->
            val buffer = ByteArray(8192)
            var total = 0L
            while (true) {
                val count = input.read(buffer)
                if (count < 0) break
                total += count
                if (total > maxBytes) throw IOException("A backup file exceeds the supported size.")
                output.write(buffer, 0, count)
            }
            return total
        }
    }

    private fun drainLimited(input: ZipInputStream, maxBytes: Long): Long {
        val buffer = ByteArray(8192)
        var total = 0L
        while (true) {
            val count = input.read(buffer)
            if (count < 0) break
            total += count
            if (total > maxBytes) throw IOException("A backup file exceeds the supported size.")
        }
        return total
    }

    private fun deleteManagedFile(filesDirectory: File, storedPath: String, restoredFiles: File): Boolean {
        return try {
            val storedFile = File(storedPath)
            val candidate = (if (storedFile.isAbsolute) storedFile else File(filesDirectory, storedPath)).canonicalFile
            val root = filesDirectory.canonicalFile
            if (!candidate.path.startsWith(root.path + File.separator) ||
                candidate.path.startsWith(restoredFiles.canonicalPath + File.separator)
            ) return false
            candidate.delete() || !candidate.exists()
        } catch (_: IOException) {
            false
        } catch (_: SecurityException) {
            false
        }
    }

    private fun appVersion(context: Context): String =
        context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "unknown"
}
