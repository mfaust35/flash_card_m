package com.faust.m.flashcardm.framework.db.room

import com.faust.m.core.LongSparseArrayList
import com.faust.m.core.data.CardDataSource
import com.faust.m.core.domain.Card
import com.faust.m.core.domain.CardContent
import com.faust.m.flashcardm.framework.db.room.definition.FlashRoomDatabase
import com.faust.m.flashcardm.framework.db.room.model.CardContentDao
import com.faust.m.flashcardm.framework.db.room.model.CardContentEntity
import com.faust.m.flashcardm.framework.db.room.model.CardDao
import com.faust.m.flashcardm.framework.db.room.model.CardEntity

class CardRoomDataSource(private val database: FlashRoomDatabase,
                         private val cardDao: CardDao = database.cardDao(),
                         private val cardContentDao: CardContentDao = database.cardContentDao()):
    CardDataSource {

    override fun add(card: Card): Card = database.runInTransaction<Card> {
        // Save the card first
        val cardCopy: Card = cardDao.add(card.toEntityModel()).let { card.copy(id = it) }

        // Then save the cardContent
        card.content.values.flatten().toList().forEach {
            cardContentDao.add(it.toEntityModel(cardCopy.id)).run {
                cardCopy.add(it.copy(id = this))
            }
        }
        cardCopy
    }

    override fun update(card: Card): Card =
        cardDao.update(card.toEntityModel()).let { card.copy(id = it.toLong()) }

    override fun updateCardContent(card: Card): Card = database.runInTransaction<Card> {
        cardDao.updateCreatedAt(card.createdAt, card.id)
        card.content.values.flatten().toList().forEach {
            cardContentDao.update(it.toEntityModel())
        }
        card
    }


    override fun getAllCardsForBooklet(bookletId: Long): List<Card> =
        cardDao.getAllCardsForBooklet(bookletId).map {
            Card(it.rating, it.lastSeen, it.createdAt, buildCardEntities(it.id), it.bookletId, it.id)
        }

    private fun buildCardEntities(cardId: Long): HashMap<String, MutableList<CardContent>> {
        val cardContentEntities =
            cardContentDao.getAllCardContentsForCard(cardId)
        val content = HashMap<String, MutableList<CardContent>>()
        for (cardContentEntity in cardContentEntities) {
            content
                .getOrPut(cardContentEntity.type) { mutableListOf() }
                .add(cardContentEntity.toDomainModel())
        }
        return content
    }

    override fun getAllCardShellsForBooklets(bookletIds: List<Long>): LongSparseArrayList<Card> =
        LongSparseArrayList<Card>(bookletIds.size).apply {
            cardDao.getAllCardsShellsForBooklets(bookletIds).forEach { cardShell ->
                addOrPutInEmptyList(cardShell.bookletId, cardShell.toDomainModel())
            }
        }

    override fun countCardsForBooklets(bookletIds: List<Long>): Map<Long, Int> =
        cardDao.countCardsForBooklets(bookletIds).map { it.bookletId to it.count }.toMap()


    private fun CardEntity.toDomainModel(): Card =
        Card(rating, lastSeen, createdAt, bookletId = bookletId, id = id)

    private fun CardContentEntity.toDomainModel(): CardContent =
        CardContent(value, type, cardId, id)

    private fun Card.toEntityModel(): CardEntity =
        CardEntity(rating, lastSeen, createdAt, bookletId, id)

    private fun CardContent.toEntityModel(newCardId: Long = -1): CardContentEntity =
        CardContentEntity(value, type, if (-1L != newCardId) newCardId else cardId, id)
}
