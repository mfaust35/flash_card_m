package com.faust.m.flashcardm.framework.db.room.model

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query

@Dao
interface BookletDao: BaseDao<BookletEntity> {

    @Query("UPDATE $BookletTableName SET name=:newName WHERE booklet_id=:bookletId")
    fun updateName(newName: String, bookletId: Long): Int

    @Query("SELECT * FROM $BookletTableName WHERE booklet_id=:bookletId")
    fun getBooklet(bookletId: Long): BookletEntity?

    @Query("SELECT * FROM $BookletTableName WHERE booklet_id=:bookletId")
    fun getLiveBooklet(bookletId: Long): LiveData<BookletEntity?>

    @Query("SELECT * FROM $BookletTableName")
    fun getAllBooklets(): List<BookletEntity>

    @Query("SELECT * FROM $BookletTableName")
    fun getLiveBooklets(): LiveData<List<BookletEntity>>
}
