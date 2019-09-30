package com.faust.m.flashcardm.framework.db.room.model

import androidx.room.Dao
import androidx.room.Query

@Dao
interface CardContentDao: BaseDao<CardContentEntity> {

    @Query("SELECT * FROM $CardContentTableName WHERE card_id = :cardId")
    fun getAllCardContentsForCard(cardId: Long): List<CardContentEntity>
}

@Dao
interface CardDao: BaseDao<CardEntity> {

    @Query("SELECT * FROM $CardTableName")
    fun getAllCards(): List<CardEntity>

    @Query("SELECT * FROM $CardTableName WHERE booklet_id = :bookletId")
    fun getAllCardsForBooklet(bookletId: Long): List<CardEntity>
}
