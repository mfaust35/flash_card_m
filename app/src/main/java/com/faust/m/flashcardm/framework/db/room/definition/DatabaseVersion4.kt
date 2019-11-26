package com.faust.m.flashcardm.framework.db.room.definition

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.faust.m.flashcardm.core.domain.ONE_DAY_IN_MS

/**
 * Migrate from:
 * version 3 - have table Card with definition:
 *      rating, last_seen, created_at, booklet_id, id
 * to
 * version 4 - alter table Card to definition:
 *      rating, next_review, updated_at, created_at, booklet_id, id
 * Diff = remove column last_seen, add column next_review, add column updated_at
 */
val MIGRATION_3_4 = object : Migration(3, 4) {

    override fun migrate(database: SupportSQLiteDatabase) {
        queryCreateTempTable().executeOn(database)
        queryCopyAndTransformRowsFromOldTableIntoTempTable().executeOn(database)
        queryDropOldTable().executeOn(database)
        queryCreateNewTable().executeOn(database)
        queryCreateIndexOnNewTable().executeOn(database)
        queryCopyFromTempTableIntoNewTable().executeOn(database)
        queryDropTempTable().executeOn(database)
    }

    private fun queryCreateTempTable() =
        """ CREATE TEMPORARY TABLE `cards_backup` (
                `rating` INTEGER NOT NULL,
                `next_review` INTEGER NOT NULL,
                `updated_at` INTEGER NOT NULL,
                `created_at` INTEGER NOT NULL,
                `booklet_id` INTEGER NOT NULL,
                `card_id` INTEGER NOT NULL
            )
        """.trimIndent()

    // Transform rows =
    //  + ---> next_review = last_seen + 1 day
    //  + ---> updated_at = created_at
    //  + ---> created_at = created_at
    private fun queryCopyAndTransformRowsFromOldTableIntoTempTable() =
        """ INSERT INTO `cards_backup` (rating, next_review, updated_at, created_at, booklet_id, card_id)
                SELECT 
                    rating,
                    (last_seen + $ONE_DAY_IN_MS ) AS next_review,
                    created_at AS updated_at,
                    created_at AS created_at,
                    booklet_id,
                    card_id
                 FROM `cards`
        """.trimIndent()

    private fun queryDropOldTable() = "DROP TABLE `cards`"

    private fun queryCreateNewTable() =
        """ CREATE TABLE IF NOT EXISTS `cards` (
                `rating` INTEGER NOT NULL,
                `next_review` INTEGER NOT NULL,
                `updated_at` INTEGER NOT NULL,
                `created_at` INTEGER NOT NULL,
                `booklet_id` INTEGER NOT NULL,
                `card_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                FOREIGN KEY(`booklet_id`) REFERENCES `booklets`(`booklet_id`) ON UPDATE NO ACTION ON DELETE CASCADE 
            )
        """.trimIndent()

    private fun queryCreateIndexOnNewTable() =
        "CREATE INDEX IF NOT EXISTS `booklet_id_idx` ON `cards` (`booklet_id`)"

    private fun queryCopyFromTempTableIntoNewTable() =
        "INSERT INTO `cards` SELECT * FROM `cards_backup`"

    private fun queryDropTempTable() = "DROP TABLE `cards_backup`"

    private fun String.executeOn(database: SupportSQLiteDatabase) =
        database.execSQL(this)
}
