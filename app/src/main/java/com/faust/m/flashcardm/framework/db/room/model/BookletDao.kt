package com.faust.m.flashcardm.framework.db.room.model

import androidx.room.Dao
import androidx.room.Query

@Dao
interface BookletDao: BaseDao<BookletEntity> {

    @Query("SELECT * FROM $BookletTableName")
    fun getAllBooklets(): List<BookletEntity>
}
