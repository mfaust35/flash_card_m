package com.faust.m.flashcardm.framework.db.room.definition

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.faust.m.flashcardm.framework.db.room.model.CardTableName

/**
 * Migrate from:
 * version 1 - have table Card with column booklet_id and card_id
 * to
 * version 2 - alter table Card: add column rating and last_seen
 */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Add column 'rating' in table card
        database.execSQL("""
            ALTER TABLE $CardTableName ADD COLUMN rating INTEGER NOT NULL DEFAULT 0
        """.trimIndent())
        // Add column 'last_seen' in table card
        database.execSQL("""
            ALTER TABLE $CardTableName ADD COLUMN last_seen INTEGER NOT NULL DEFAULT 0
        """.trimIndent())
    }
}
