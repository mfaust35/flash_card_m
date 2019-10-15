package com.faust.m.flashcardm.framework.db.room.definition

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.faust.m.flashcardm.framework.db.room.model.CardTableName

/**
 * Migrate from:
 * version 2 - have table Card with column rating, last_seen, booklet_id, card_id
 * to
 * version 3 - alter table Card: add column created_at
 */
val MIGRATION_2_3 = object : Migration(2,3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Add column created_at in table card
        database.execSQL("""
            ALTER TABLE $CardTableName ADD COLUMN created_at INTEGER NOT NULL DEFAULT 0
        """.trimIndent())
        // Set created_at to last_seen by default
        database.execSQL("""
            UPDATE $CardTableName
            SET created_at = $CardTableName.last_seen
        """.trimIndent())
    }
}
