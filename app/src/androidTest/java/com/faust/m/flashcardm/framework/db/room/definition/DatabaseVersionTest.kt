package com.faust.m.flashcardm.framework.db.room.definition

import android.database.Cursor
import org.assertj.core.api.Assertions
import java.util.*

fun Cursor.getIntFrom(columnName: String) = getInt(getColumnIndex(columnName))
fun Cursor.getLongFrom(columnName: String) = getLong(getColumnIndex(columnName))
fun Cursor.getDateFrom(columnName: String) = Date(getLongFrom(columnName))

internal fun Cursor.assertThatRowNumberIsEqualTo(expected: Int) {
    Assertions.assertThat(count).`as`("Number of rows in cursor").isEqualTo(expected)
}