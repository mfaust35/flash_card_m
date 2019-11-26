package com.faust.m.flashcardm.framework.db.room.definition

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.platform.app.InstrumentationRegistry
import com.faust.m.flashcardm.core.domain.ONE_DAY_IN_MS
import com.faust.m.flashcardm.framework.db.room.model.CardTableName
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private const val TEST_DB_NAME = "migration-test"

@RunWith(AndroidJUnit4ClassRunner::class)
class DatabaseVersion4Test {

    private lateinit var dbAfterMigration: SupportSQLiteDatabase

    @get:Rule
    val migrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        FlashRoomDatabase::class.java.canonicalName,
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    fun migrateVersion3To4MigrateRows() {
        givenDatabaseWithSchemaVersion3And1Card()

        whenIRunMigrationFromVersion3To4()

        // The card has been migrated
        dbAfterMigration.query("SELECT * FROM $CardTableName").apply {
            assertThatRowNumberIsEqualTo(1)
        }
    }

    @Test
    fun migrateVersion3To4KeepsAttributeRating() {
        givenDatabaseWithSchemaVersion3And1Card()

        whenIRunMigrationFromVersion3To4()

        // The card has same rating value
        dbAfterMigration.query("SELECT * FROM $CardTableName").apply {
            moveToNext()
            assertThat(getIntFrom("rating")).isEqualTo(1)
        }
    }

    @Test
    fun migrateVersion3To4KeepsAttributeCreatedAt() {
        givenDatabaseWithSchemaVersion3And1Card()

        whenIRunMigrationFromVersion3To4()

        // The card has same created_at value
        dbAfterMigration.query("SELECT * FROM $CardTableName").apply {
            moveToNext()
            assertThat(getLongFrom("created_at")).isEqualTo(3000)
        }
    }

    @Test
    fun migrateVersion3To4KeepsAttributeBookletId() {
        givenDatabaseWithSchemaVersion3And1Card()

        whenIRunMigrationFromVersion3To4()

        // The card has same booklet_id value
        dbAfterMigration.query("SELECT * FROM $CardTableName").apply {
            moveToNext()
            assertThat(getLongFrom("booklet_id")).isEqualTo(3)
        }
    }

    @Test
    fun migrateVersion3To4KeepsAttributeCardId() {
        givenDatabaseWithSchemaVersion3And1Card()

        whenIRunMigrationFromVersion3To4()

        // The card has same card_id value
        dbAfterMigration.query("SELECT * FROM $CardTableName").apply {
            moveToNext()
            assertThat(getLongFrom("card_id")).isEqualTo(20)
        }
    }

    @Test
    fun migrateVersion3To4CreateAttributeNextReviewFromAttributeLastSeenPlus1Day() {
        givenDatabaseWithSchemaVersion3And1Card()

        whenIRunMigrationFromVersion3To4()

        // The card has next_review value = last_seen + 1 day
        dbAfterMigration.query("SELECT * FROM $CardTableName").apply {
            moveToNext()
            assertThat(getLongFrom("next_review")).isEqualTo(200 + ONE_DAY_IN_MS)
        }
    }

    @Test
    fun migrateVersion3To4CreateAttributeUpdateAtFromAttributeCreatedAt() {
        givenDatabaseWithSchemaVersion3And1Card()

        whenIRunMigrationFromVersion3To4()

        // The card has updated_at = created_at
        dbAfterMigration.query("SELECT * FROM $CardTableName").apply {
            moveToNext()
            assertThat(getLongFrom("updated_at")).isEqualTo(3000)
        }
    }


    private fun givenDatabaseWithSchemaVersion3And1Card() =
        migrationTestHelper.createDatabase(TEST_DB_NAME, 3).apply {
            execSQL("INSERT INTO $CardTableName VALUES (1, 200, 3000, 3, 20)")
        }

    private fun whenIRunMigrationFromVersion3To4() {
        dbAfterMigration = migrationTestHelper.runMigrationsAndValidate(
            TEST_DB_NAME, 4, true, MIGRATION_3_4
        )
    }
}