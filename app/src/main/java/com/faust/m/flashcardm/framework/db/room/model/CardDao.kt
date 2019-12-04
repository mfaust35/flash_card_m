package com.faust.m.flashcardm.framework.db.room.model

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import androidx.room.RawQuery
import androidx.sqlite.db.SupportSQLiteQuery

@Dao
interface CardContentDao: BaseDao<CardContentEntity> {

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

    @Query("SELECT * FROM $CardTableName")
    fun getLiveCards(): LiveData<List<CardEntity>>

    @RawQuery(observedEntities = [CardEntity::class])
    fun getLiveCardsFilteredViaQuery(query: SupportSQLiteQuery): LiveData<List<CardEntity>>

    @Query("""SELECT *
        FROM $CardTableName
        WHERE booklet_id IN (:bookletIds)
        ORDER BY card_id ASC
    """)
    fun getAllCardsShellsForBooklets(bookletIds: List<Long>): List<CardEntity>
}
