package com.faust.m.flashcardm.framework.db.room.model

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert

@Dao
interface BaseDao<T> {

    @Insert
    fun addAll(vararg objects: T): List<Long>

    @Insert
    fun add(value: T): Long

    @Delete
    fun delete(value: T): Int
}
