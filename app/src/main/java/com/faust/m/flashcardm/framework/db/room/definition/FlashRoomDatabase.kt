package com.faust.m.flashcardm.framework.db.room.definition

import android.content.Context
import androidx.room.*
import com.faust.m.flashcardm.framework.db.room.model.*
import java.util.*

const val DATABASE_PATH = "flash_database"

@Database(
    entities = [BookletEntity::class, CardEntity::class, CardContentEntity::class],
    version = 2
)
@TypeConverters(DateConverter::class)
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
                .addMigrations(MIGRATION_1_2)
                .build()
    }
}

class DateConverter {

    @TypeConverter
    fun toDate(dateLong: Long?): Date? {
        return dateLong?.let { Date(dateLong) }
    }

    @TypeConverter
    fun fromDate(date: Date?): Long? {
        return date?.let { date.time }
    }
}
