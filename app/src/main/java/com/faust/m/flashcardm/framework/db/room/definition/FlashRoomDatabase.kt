package com.faust.m.flashcardm.framework.db.room.definition

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.faust.m.flashcardm.framework.db.room.model.*

const val DATABASE_PATH = "flash_database"

@Database(
    entities = [BookletEntity::class, CardEntity::class, CardContentEntity::class],
    version = 1
)
abstract class FlashRoomDatabase: RoomDatabase() {

    abstract fun bookletDao(): BookletDao
    abstract fun cardDao(): CardDao
    abstract fun cardContentDao(): CardContentDao

    /**
     * Allow building of a new database instance easily
     */
    companion object {
        @Volatile
        private var INSTANCE: FlashRoomDatabase? = null

        fun getInstance(context: Context): FlashRoomDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE
                    ?: buildDatabase(
                        context
                    ).also { INSTANCE = it }
            }

        private fun buildDatabase(context: Context): FlashRoomDatabase =
            Room.databaseBuilder(context, FlashRoomDatabase::class.java, DATABASE_PATH)
                .build()
    }
}
