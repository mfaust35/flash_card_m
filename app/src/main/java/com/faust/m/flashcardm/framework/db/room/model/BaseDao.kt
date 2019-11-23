package com.faust.m.flashcardm.framework.db.room.model

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Update

@Dao
interface BaseDao<T> {

    @Insert
    fun addAll(vararg objects: T): List<Long>

    @Insert
    fun add(value: T): Long

    @Update
    fun update(value: T): Int

    @Update
    fun updateAll(vararg objects: T): Int

    @Delete
    fun delete(value: T): Int

    @Delete
    fun deleteAll(vararg values: T): Int
}
