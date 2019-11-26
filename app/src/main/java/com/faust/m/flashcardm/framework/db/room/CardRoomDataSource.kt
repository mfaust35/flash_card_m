package com.faust.m.flashcardm.framework.db.room

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Transformations
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
            card.copy(id = it, roster = Roster())
        }

        // Then save the cardContent
        card.roster.forEach {
            val contentForCardCopy = it.copy(cardId = cardCopy.id)
            cardContentDao.add(contentForCardCopy.toEntityModel()).run {
                cardCopy.add(contentForCardCopy.copy(id = this))
            }
        }
        cardCopy
    }

    override fun update(card: Card): Card = card.also {
        cardDao.update(it.toEntityModel())
    }

    override fun updateCardWithContent(card: Card): Card = database.runInTransaction<Card> {
        cardDao.update(card.toEntityModel())
        card.roster.map { it.toEntityModel() }.toTypedArray().let { params ->
            cardContentDao.updateAll(*params)
        }
        card
    }

    override fun getLiveDeckForBooklet(bookletId: Long,
                                       attachCardContent: Boolean,
                                       filterToReviewCard: Boolean): LiveData<Deck>  {
        val result = MediatorLiveData<Deck>()
        val cardSource = cardDao.getLiveCardsForBooklet(bookletId)

        if (!attachCardContent) {
            result.addSource(cardSource) { cardEntities ->
                result.value =
                    mergeRostersToCardEntities(cardEntities, filterToReviewCard = filterToReviewCard)
            }
        }
        else {
            val contentSource =
                cardContentDao.getLiveCardContentsForBooklet(bookletId)

            fun combineData() {
                val cardEntities = cardSource.value ?: return
                val contentEntities = contentSource.value?.map { c -> c.toDomainModel() }?.toRoster()
                    ?.mapRosterByCardId() ?: return

                result.value = mergeRostersToCardEntities(cardEntities, contentEntities, filterToReviewCard)
            }

            result.addSource(cardSource) { combineData() }
            result.addSource(contentSource) { combineData() }
        }

        return result
    }

    private fun mergeRostersToCardEntities(cardEntities: List<CardEntity>,
                                           rosters: Map<Long, Roster> = HashMap(),
                                           filterToReviewCard: Boolean): Deck {
        val cards = cardEntities.map { c ->
            val roster = rosters[c.id] ?: Roster()
            c.toDomainModel(roster)
        }
        return if (filterToReviewCard) cards.filter(Card::needReview).toDeck()
        else cards.toDeck()
    }

    override fun getLiveDeck(): LiveData<Deck> =
        Transformations.map(cardDao.getLiveCards()) { it.toDeck() }

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
                    5 -> cEntity.copy(nextReview = Date(), createdAt = updateAt, rating = 4)
                    else -> cEntity.copy(nextReview = Date(), createdAt = updateAt)
                }
                cardDao.update(cardCopy.toEntityModel())
                resetCount ++
            }
        return resetCount
    }

    override fun deleteCards(cards: List<Card>): Int =
        cards.map { c -> c.toEntityModel() }.toTypedArray().let { cardEntities ->
            return cardDao.deleteAll(*cardEntities)
        }

    private fun CardEntity.toDomainModel(roster: Roster = Roster()): Card =
        Card(rating, nextReview, updatedAt, createdAt, roster = roster, bookletId = bookletId, id = id)

    private fun CardContentEntity.toDomainModel(): CardContent =
        CardContent(value, type, cardId, id)

    private fun Card.toEntityModel(): CardEntity =
        CardEntity(rating, nextReview, updatedAt, createdAt, bookletId, id)

    private fun CardContent.toEntityModel(newCardId: Long = -1): CardContentEntity =
        CardContentEntity(value, type, if (-1L != newCardId) newCardId else cardId, id)

    private fun List<CardEntity>.toDeck(): Deck =
        this.map { it.toDomainModel() }.toDeck()
}
