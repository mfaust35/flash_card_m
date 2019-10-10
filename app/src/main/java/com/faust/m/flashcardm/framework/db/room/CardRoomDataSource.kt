package com.faust.m.flashcardm.framework.db.room

import com.faust.m.core.data.CardDataSource
import com.faust.m.core.domain.Card
import com.faust.m.core.domain.CardContent
import com.faust.m.flashcardm.framework.db.room.model.CardContentDao
import com.faust.m.flashcardm.framework.db.room.model.CardContentEntity
import com.faust.m.flashcardm.framework.db.room.model.CardDao
import com.faust.m.flashcardm.framework.db.room.model.CardEntity
import java.util.*

class CardRoomDataSource(private val cardDao: CardDao,
                         private val cardContentDao: CardContentDao): CardDataSource {

    override fun add(card: Card): Card {
        // Save the card first
        // Should use a transaction here
        val cardCopy: Card = cardDao.add(card.toEntityModel()).let { card.copy(id = it) }

        // Then save the cardContent
        card.content.values.flatten().toList().forEach {
            cardContentDao.add(it.toEntityModelWithCardId(cardCopy.id)).run {
                cardCopy.add(it.copy(id = this))
            }
        }

        return cardCopy
    }

    override fun getAllCardsForBooklet(bookletId: Long): List<Card> =
        cardDao.getAllCardsForBooklet(bookletId).map {
            Card(buildCardEntities(it.id), it.bookletId, it.id)
        }

    private fun buildCardEntities(cardId: Long): HashMap<String, MutableList<CardContent>> {
        val cardContentEntities =
            cardContentDao.getAllCardContentsForCard(cardId)
        val content = HashMap<String, MutableList<CardContent>>()
        for (cardContentEntity in cardContentEntities) {
            content
                .getOrPut(cardContentEntity.type) { mutableListOf() }
                .add(toDomainModel(cardContentEntity))
        }
        return content
    }

    override fun countCardsForBooklets(bookletIds: List<Long>): Map<Long, Int> =
        cardDao.countCardsForBooklets(bookletIds).map { it.bookletId to it.count }.toMap()

    private fun toDomainModel(cardContentEntity: CardContentEntity): CardContent =
        CardContent(
            cardContentEntity.value,
            cardContentEntity.type,
            cardContentEntity.cardId,
            cardContentEntity.id
        )

    private fun Card.toEntityModel(): CardEntity =
        CardEntity(0, Date(0), bookletId, id)

    private fun CardContent.toEntityModelWithCardId(newCardId: Long): CardContentEntity =
        CardContentEntity(value, type, newCardId, id)
}
