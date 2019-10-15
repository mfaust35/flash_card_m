package com.faust.m.flashcardm.framework.db.room.definition

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.platform.app.InstrumentationRegistry
import com.faust.m.flashcardm.framework.db.room.model.CardTableName
import org.assertj.core.api.Assertions
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

private const val TEST_DB_NAME = "migration-test"

@RunWith(AndroidJUnit4ClassRunner::class)
class DatabaseVersion3Test {

    @get:Rule
    val migrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        FlashRoomDatabase::class.java.canonicalName,
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    fun migrateVersion2To3KeepsCards() {
        // Given a database with version 2 schema and 1 card
        migrationTestHelper.createDatabase(TEST_DB_NAME, 2).apply {
            execSQL("INSERT INTO $CardTableName VALUES (0, 200, 10, 1)")
        }
        // When applying migration from version 2 to 3
        val db = migrationTestHelper.runMigrationsAndValidate(
            TEST_DB_NAME, 3, true, MIGRATION_2_3
        )

        // The card has been migrated with default created_at set to last_seen
        db.query("SELECT * FROM $CardTableName").apply {
            assertThatRowNumberIsEqualTo(1)
            moveToNext()
            Assertions.assertThat(getIntFrom("rating")).isEqualTo(0)
            Assertions.assertThat(getDateFrom("last_seen")).isEqualTo(Date(200))
            Assertions.assertThat(getDateFrom("created_at")).isEqualTo(Date(200))
            Assertions.assertThat(getLongFrom("booklet_id")).isEqualTo(10)
            Assertions.assertThat(getLongFrom("card_id")).isEqualTo(1)
        }
    }
}