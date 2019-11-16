package com.faust.m.flashcardm.framework.db.room.definition

import android.content.Context
import androidx.room.*
import com.faust.m.flashcardm.core.domain.CardContentType
import com.faust.m.flashcardm.core.domain.CardContentType.BACK
import com.faust.m.flashcardm.core.domain.CardContentType.FRONT
import com.faust.m.flashcardm.framework.db.room.model.*
import java.util.*

const val DATABASE_PATH = "flash_database"

@Database(
    entities = [BookletEntity::class, CardEntity::class, CardContentEntity::class],
    version = 3
)
@TypeConverters(DateConverter::class, CardContentTypeConverter::class)
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
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
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

class CardContentTypeConverter {

    @TypeConverter
    fun toCardContentType(value: String?): CardContentType? {
        return when(value) {
            "front" -> FRONT
            "back" -> BACK
            else -> BACK
        }
    }

    @TypeConverter
    fun fromCardContentType(cardContentType: CardContentType?): String? {
        return when(cardContentType) {
            FRONT -> "front"
            BACK -> "back"
            else -> "back"
        }
    }
}
