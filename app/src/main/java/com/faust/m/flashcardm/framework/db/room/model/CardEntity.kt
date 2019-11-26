package com.faust.m.flashcardm.framework.db.room.model

import androidx.room.*
import com.faust.m.flashcardm.core.domain.CardContentType
import java.util.*

const val CardContentTableName = "card_contents"
const val CardTableName = "cards"

@Entity(
    tableName = CardContentTableName,
    indices = [Index(value = ["card_id"], name = "card_id_idx")],
    foreignKeys = [
        ForeignKey(entity = CardEntity::class,
            parentColumns = ["card_id"],
            childColumns = ["card_id"],
            onDelete = ForeignKey.CASCADE)
    ]
)
data class CardContentEntity (
    @ColumnInfo(name = "value") val value: String,
    @ColumnInfo(name = "card_type") val type: CardContentType,
    @ColumnInfo(name = "card_id") val cardId: Long,
    @ColumnInfo(name = "card_content_id")
    @PrimaryKey(autoGenerate = true) val id: Long = 0
)

/**
 * Rating is a int between 0..5 indicating how much this card is know: 0 means the user haven't
 * learned this card, 5 means the user know this card perfectly
 */
@Entity(
    tableName = CardTableName,
    indices = [Index(value = ["booklet_id"], name = "booklet_id_idx")],
    foreignKeys = [
        ForeignKey(entity = BookletEntity::class,
            parentColumns = ["booklet_id"],
            childColumns = ["booklet_id"],
            onDelete = ForeignKey.CASCADE)
    ]
)
data class CardEntity (
    @ColumnInfo(name = "rating") val rating: Int,
    @ColumnInfo(name = "next_review") val nextReview: Date,
    @ColumnInfo(name = "updated_at") val updatedAt: Date,
    @ColumnInfo(name = "created_at") val createdAt: Date,
    @ColumnInfo(name = "booklet_id") val bookletId: Long,
    @ColumnInfo(name = "card_id")
    @PrimaryKey(autoGenerate = true) val id: Long = 0
)
