package com.faust.m.flashcardm.framework.db.room.model

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Query
import java.util.*

@Dao
interface CardContentDao: BaseDao<CardContentEntity> {

    @Query("SELECT * FROM $CardContentTableName WHERE card_id = :cardId")
    fun getAllCardContentsForCard(cardId: Long): List<CardContentEntity>
}

@Dao
interface CardDao: BaseDao<CardEntity> {

    @Query("UPDATE $CardTableName SET created_at=:createdAt WHERE card_id=:id")
    fun updateCreatedAt(createdAt: Date, id: Long): Int

    @Query("SELECT * FROM $CardTableName")
    fun getAllCards(): List<CardEntity>

    @Query("""SELECT *
        FROM $CardTableName
        WHERE booklet_id = :bookletId""")
    fun getAllCardsForBooklet(bookletId: Long): List<CardEntity>

    @Query("""SELECT *
        FROM $CardTableName
        WHERE booklet_id IN (:bookletIds)
        ORDER BY card_id ASC
    """)
    fun getAllCardsShellsForBooklets(bookletIds: List<Long>): List<CardEntity>

    @Query("""SELECT booklet_id, COUNT(DISTINCT card_id) AS count
        FROM $CardTableName
        WHERE booklet_id IN (:bookletIds)
        GROUP BY booklet_id""")
    fun countCardsForBooklets(bookletIds: List<Long>): List<CardCountEntity>
}

data class CardCountEntity(
    @ColumnInfo(name = "booklet_id") val bookletId: Long,
    @ColumnInfo(name = "count") val count: Int
)
