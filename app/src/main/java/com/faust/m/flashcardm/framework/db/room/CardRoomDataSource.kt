package com.faust.m.flashcardm.framework.db.room

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.faust.m.flashcardm.core.LongSparseArrayList
import com.faust.m.flashcardm.core.data.CardDataSource
import com.faust.m.flashcardm.core.domain.*
import com.faust.m.flashcardm.framework.db.room.definition.FlashRoomDatabase
import com.faust.m.flashcardm.framework.db.room.model.CardContentDao
import com.faust.m.flashcardm.framework.db.room.model.CardContentEntity
import com.faust.m.flashcardm.framework.db.room.model.CardDao
import com.faust.m.flashcardm.framework.db.room.model.CardEntity
import java.util.*

class CardRoomDataSource(private val database: FlashRoomDatabase,
                         private val cardDao: CardDao = database.cardDao(),
                         private val cardContentDao: CardContentDao = database.cardContentDao()):
    CardDataSource {

    override fun add(card: Card): Card = database.runInTransaction<Card> {
        // Save the card first
        val cardCopy: Card = cardDao.add(card.toEntityModel()).let {
            card.copy(id = it, content = EnumMap(CardContentType::class.java))
        }

        // Then save the cardContent
        card.content.values.flatten().toList().forEach {
            val contentForCardCopy = it.copy(cardId = cardCopy.id)
            cardContentDao.add(contentForCardCopy.toEntityModel()).run {
                cardCopy.add(contentForCardCopy.copy(id = this))
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

    private fun buildCardEntities(cardId: Long): EnumMap<CardContentType, MutableList<CardContent>> {
        val cardContentEntities =
            cardContentDao.getAllCardContentsForCard(cardId)
        val content = EnumMap<CardContentType, MutableList<CardContent>>(CardContentType::class.java)
        for (cardContentEntity in cardContentEntities) {
            content
                .getOrPut(cardContentEntity.type) { mutableListOf() }
                .add(cardContentEntity.toDomainModel())
        }
        return content
    }

    override fun getLiveDeckForBooklet(bookletId: Long): LiveData<Deck> =
        Transformations.map(cardDao.getLiveCardsForBooklet(bookletId), ::mapCardEntitiesToDeck)

    override fun getLiveDeck(): LiveData<Deck> =
        Transformations.map(cardDao.getLiveCards(), ::mapCardEntitiesToDeck)

    override fun getAllCardShellsForBooklets(bookletIds: List<Long>): LongSparseArrayList<Card> =
        LongSparseArrayList<Card>(bookletIds.size).apply {
            cardDao.getAllCardsShellsForBooklets(bookletIds).forEach { cardShell ->
                addOrPutInEmptyList(cardShell.bookletId, cardShell.toDomainModel())
            }
        }

    override fun countCardsForBooklets(bookletIds: List<Long>): Map<Long, Int> =
        cardDao.countCardsForBooklets(bookletIds).map { it.bookletId to it.count }.toMap()

    /**
     * Will modify X card in booklet so that they will be inReview again. This method will ignore
     * all cards that already in review state. It will always try to modify the card will the
     * lower rating first. Modifications include updating `createdAt` and / or `rating`, so that
     * the card will appear as in review
     * @param count: the number of card to modify
     * @param bookletId: will only select card in this booklet
     */
    override fun resetForReview(count: Int, bookletId: Long): Int {
        val cardShells =
            cardDao.getAllCardsShellsForBooklets(listOf(bookletId)).map { it.toDomainModel() }
                .filterNot(Card::needReview)
        val cardToReset = mutableListOf<Card>()
        var ratingToAdd = -1
        // Take cards starting by lowest rating until we reach the number of cards to reset
        while (cardToReset.count() < count && ++ratingToAdd <= 5) {
            cardToReset.addAll(cardShells.filter { it.rating == ratingToAdd })
        }
        val totalPossibleCard = cardToReset.count()
        if (totalPossibleCard <= 0) return 0
        // Take the first X ones and reset date and/or rating
        val updateAt = Date()
        var resetCount = 0
        cardToReset
            .shuffled()
            .slice(0 until count.coerceAtMost(totalPossibleCard))
            .forEach { cEntity ->
                val cardCopy = when(cEntity.rating) {
                    5 -> cEntity.copy(createdAt = updateAt, rating = 4)
                    else -> cEntity.copy(createdAt = updateAt)
                }
                cardDao.update(cardCopy.toEntityModel())
                resetCount ++
            }
        return resetCount
    }

    override fun deleteCard(card: Card): Int =
        cardDao.delete(card.toEntityModel())


    private fun CardEntity.toDomainModel(): Card =
        Card(rating, lastSeen, createdAt, bookletId = bookletId, id = id)

    private fun CardContentEntity.toDomainModel(): CardContent =
        CardContent(value, type, cardId, id)

    private fun Card.toEntityModel(): CardEntity =
        CardEntity(rating, lastSeen, createdAt, bookletId, id)

    private fun CardContent.toEntityModel(newCardId: Long = -1): CardContentEntity =
        CardContentEntity(value, type, if (-1L != newCardId) newCardId else cardId, id)

    private fun mapCardEntitiesToDeck(cardEntities: List<CardEntity>): Deck =
        cardEntities.map {c -> c.toDomainModel()}.toDeck()
}
