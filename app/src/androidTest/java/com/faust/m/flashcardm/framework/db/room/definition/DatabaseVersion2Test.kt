package com.faust.m.flashcardm.framework.db.room.definition

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.platform.app.InstrumentationRegistry
import com.faust.m.flashcardm.framework.db.room.model.CardTableName
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

private const val TEST_DB_NAME = "migration-test"

@RunWith(AndroidJUnit4ClassRunner::class)
class DatabaseVersion2Test {

    @get:Rule
    val migrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        FlashRoomDatabase::class.java.canonicalName,
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    fun migrateVersion1To2KeepCards() {
        // Given a database with version 1 schema and 1 card
        migrationTestHelper.createDatabase(TEST_DB_NAME, 1).apply {
            execSQL("INSERT INTO $CardTableName VALUES (10, 1)")
        }

        // When applying migration from version 1 to 2
        val db = migrationTestHelper.runMigrationsAndValidate(
            TEST_DB_NAME, 2, true, MIGRATION_1_2
        )

        // Then card has been migrated with default date set to 0
        db.query("SELECT * FROM $CardTableName").apply {
            assertThatRowNumberIsEqualTo(1)
            moveToNext()
            assertThat(getIntFrom("rating")).isEqualTo(0)
            assertThat(getDateFrom("last_seen")).isEqualTo(Date(0))
            assertThat(getLongFrom("booklet_id")).isEqualTo(10)
            assertThat(getLongFrom("card_id")).isEqualTo(1)
        }
    }
}
