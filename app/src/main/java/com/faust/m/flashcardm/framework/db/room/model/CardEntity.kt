package com.faust.m.flashcardm.framework.db.room.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

const val CardContentTableName = "card_contents"
const val CardTableName = "cards"

@Entity(
    tableName = CardContentTableName,
    foreignKeys = [
        ForeignKey(entity = CardEntity::class,
            parentColumns = ["card_id"],
            childColumns = ["card_id"],
            onDelete = ForeignKey.CASCADE)
    ]
)
data class CardContentEntity (
    @ColumnInfo(name = "value") val value: String,
    @ColumnInfo(name = "card_type") val type: String,
    @ColumnInfo(name = "card_id") val cardId: Long,
    @ColumnInfo(name = "card_content_id")
    @PrimaryKey(autoGenerate = true) val id: Long = 0
)

@Entity(
    tableName = CardTableName,
    foreignKeys = [
        ForeignKey(entity = BookletEntity::class,
            parentColumns = ["booklet_id"],
            childColumns = ["booklet_id"],
            onDelete = ForeignKey.CASCADE)
    ]
)
data class CardEntity (
    @ColumnInfo(name = "booklet_id") val bookletId: Long,
    @ColumnInfo(name = "card_id")
    @PrimaryKey(autoGenerate = true) val id: Long = 0
)
