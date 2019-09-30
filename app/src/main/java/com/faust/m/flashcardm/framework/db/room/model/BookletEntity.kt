package com.faust.m.flashcardm.framework.db.room.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

const val BookletTableName = "booklets"

@Entity(tableName = BookletTableName)
data class BookletEntity (
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "booklet_id")
    @PrimaryKey(autoGenerate = true) var id: Long = 0
)
