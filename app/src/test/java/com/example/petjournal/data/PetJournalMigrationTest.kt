package com.example.petjournal.data

import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import com.example.petjournal.core.database.PetJournalDatabase
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PetJournalMigrationTest {
    @Test
    fun migratesVersionOneDataWithoutLosingPetProfiles() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val configuration = SupportSQLiteOpenHelper.Configuration.builder(context)
            .name(TEST_DATABASE)
            .callback(object : SupportSQLiteOpenHelper.Callback(1) {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    db.execSQL("CREATE TABLE pets (id TEXT NOT NULL, name TEXT NOT NULL, PRIMARY KEY(id))")
                    db.execSQL("CREATE TABLE care_reminders (id TEXT NOT NULL, PRIMARY KEY(id))")
                    db.execSQL("INSERT INTO pets (id, name) VALUES ('pet-1', 'Mochi')")
                }

                override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
            })
            .build()
        val helper = FrameworkSQLiteOpenHelperFactory().create(configuration)
        val db = helper.writableDatabase
        PetJournalDatabase.MIGRATION_1_2.migrate(db)

        db.query("SELECT name FROM pets WHERE id = 'pet-1'").use { cursor ->
            assertEquals(true, cursor.moveToFirst())
            assertEquals("Mochi", cursor.getString(0))
        }
        db.query("SELECT name FROM sqlite_master WHERE type='table' AND name='health_records'").use { cursor ->
            assertEquals(true, cursor.moveToFirst())
        }
        helper.close()
        context.deleteDatabase(TEST_DATABASE)
    }

    private companion object {
        const val TEST_DATABASE = "pet-journal-migration-test"
    }
}
