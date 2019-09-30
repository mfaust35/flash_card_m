package com.faust.m.flashcardm.framework.db.room.model

import android.database.Cursor
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.faust.m.flashcardm.framework.db.room.definition.FlashRoomDatabase
import org.junit.After
import org.junit.Before

abstract class BaseDaoTest {

    private lateinit var database: FlashRoomDatabase

    @Before
    fun setup() {
        // using an in-memory database because the information stored here disappears when the
        // process is killed
        database = Room
            .inMemoryDatabaseBuilder(
                ApplicationProvider.getApplicationContext(),
                FlashRoomDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        onDatabaseCreated(database)
    }

    abstract fun onDatabaseCreated(database: FlashRoomDatabase)

    @After
    fun tearDown() {
        database.close()
    }
}

fun Cursor.getStringFrom(columnName: String): String = getString(getColumnIndex(columnName))
fun Cursor.getLongFrom(columnName: String) = getLong(getColumnIndex(columnName))
