package com.faust.m.flashcardm.framework.db.room.model

import androidx.room.Dao
import androidx.room.Query

@Dao
interface BookletDao: BaseDao<BookletEntity> {

    @Query("UPDATE $BookletTableName SET name=:newName WHERE booklet_id=:bookletId")
    fun updateName(newName: String, bookletId: Long): Int

    @Query("SELECT * FROM $BookletTableName")
    fun getAllBooklets(): List<BookletEntity>
}
