package com.faust.m.flashcardm.framework.db.room.model

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import java.util.*

@Dao
interface CardContentDao: BaseDao<CardContentEntity> {

    @Query("SELECT * FROM $CardContentTableName WHERE card_id = :cardId")
    fun getAllCardContentsForCard(cardId: Long): List<CardContentEntity>

    @Query("""SELECT *
        FROM $CardContentTableName
        WHERE card_id IN (
            SELECT card_id 
            FROM $CardTableName
            WHERE booklet_id = :bookletId
        )
    """)
    fun getLiveCardContentsForBooklet(bookletId: Long): LiveData<List<CardContentEntity>>
}

@Dao
interface CardDao: BaseDao<CardEntity> {

    @Query("UPDATE $CardTableName SET created_at=:createdAt WHERE card_id=:id")
    fun updateCreatedAt(createdAt: Date, id: Long): Int

    @Query("SELECT * FROM $CardTableName")
    fun getAllCards(): List<CardEntity>

    @Query("SELECT * FROM $CardTableName")
    fun getLiveCards(): LiveData<List<CardEntity>>

    @Query("""SELECT *
        FROM $CardTableName
        WHERE booklet_id = :bookletId""")
    fun getLiveCardsForBooklet(bookletId: Long): LiveData<List<CardEntity>>

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
}
