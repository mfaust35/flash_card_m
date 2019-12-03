package com.faust.m.flashcardm.framework.db.room

import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import com.faust.m.flashcardm.core.domain.Filter
import com.faust.m.flashcardm.core.domain.Filter.Numeric
import com.faust.m.flashcardm.core.domain.Filter.Timestamp
import com.faust.m.flashcardm.core.domain.FilterState
import com.faust.m.flashcardm.core.domain.FilterType
import com.faust.m.flashcardm.core.domain.FilterType.*
import com.faust.m.flashcardm.framework.db.room.model.CardEntity.Companion.nameForProperty
import com.faust.m.flashcardm.framework.db.room.model.CardTableName


/**
 * Transform a FilterState into a query that room will understand. Each filters are appended
 * one after the other into a big WHERE statement
 */
fun FilterState.toRoomQuery(): SupportSQLiteQuery {
    if (filters.isEmpty()) return emptyQuery()

    val querySql = initQuerySql()
    var queryArg = initQueryArg()

    for ((index, filter) in filters.withIndex()) {
        if (index != 0) querySql.append("AND ")

        filter.applyToQuery { filterName, filterValue, filterType ->
            querySql
                .append(filterName)
                .appendType(filterType)
            queryArg += filterValue
        }
    }

    return SimpleSQLiteQuery(querySql.toString(), queryArg)
}

private fun emptyQuery() =
    SimpleSQLiteQuery("SELECT * FROM $CardTableName", emptyArray<Any>())

private fun initQuerySql() = StringBuilder("SELECT * FROM $CardTableName WHERE ")

private fun initQueryArg() = emptyArray<Any>()

private fun Filter.applyToQuery(applyTo: (fName: String, fValue: Any, fType: FilterType) -> Unit) {
    when (this) {
        is Numeric -> applyTo(nameForProperty[property], value, filterType)
        is Timestamp -> applyTo(nameForProperty[property], value, filterType)
    }
}

fun StringBuilder.appendType(filterType: FilterType) {
    when (filterType) {
        INFERIOR -> append(" < ? ")
        SUPERIOR -> append(" > ? ")
        EQUAL -> append(" = ? ")
    }
}
