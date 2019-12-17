package com.faust.m.flashcardm.core.domain

import com.faust.m.flashcardm.core.domain.Card.RatingLevel.*
import com.faust.m.flashcardm.core.domain.CardContentType.BACK
import com.faust.m.flashcardm.core.domain.CardContentType.FRONT
import java.util.*
import kotlin.collections.HashMap

const val ONE_DAY_IN_MS = 86400000L

enum class CardContentType { FRONT, BACK }

data class CardContent (
    val value: String = "",
    val type: CardContentType = FRONT,
    val cardId: Long = 0,
    val id: Long = 0
)

interface IRoster: MutableList<CardContent> {

    fun firstTextValue(type: CardContentType): CardContent? = firstOrNull { it.type == type }
}

/**
 * Redefine MutableList<CardContent> as Roster
 * A Roster can contain cardContent that do not belong to the same card
 */
class Roster(private val cardContents: MutableList<CardContent>) :
    IRoster, MutableList<CardContent> by cardContents {

    constructor(): this(mutableListOf())

    fun mapRosterByCardId(): Map<Long, Roster> {
        val result: HashMap<Long, Roster> = HashMap()
        cardContents.forEach { result.add(it) }
        return result
    }

    private fun HashMap<Long, Roster>.add(cardContent: CardContent) {
        var roster = this[cardContent.cardId]
        if (roster == null) {
            roster = Roster(mutableListOf())
            this[cardContent.cardId] = roster
        }
        roster.add(cardContent)
    }

    override fun equals(other: Any?): Boolean {
        return cardContents == other
    }

    override fun hashCode(): Int {
        return cardContents.hashCode()
    }
}

fun List<CardContent>.toRoster(): Roster = Roster(this.toMutableList())


data class Card (
    val rating: Int = 0,
    val nextReview: Date = Date(),
    val updatedAt: Date = nextReview,
    val createdAt: Date = nextReview,
    val roster: Roster = Roster(),
    val bookletId: Long = 0,
    val id: Long = 0
) {

    fun frontAsTextOrNull() = roster.firstTextValue(FRONT)?.value

    fun frontAsTextOrEmpty() = frontAsTextOrNull() ?: ""

    fun backAsTextOrNull() = roster.firstTextValue(BACK)?.value

    fun backAsTextOrEmpty() = backAsTextOrNull() ?: ""

    fun updateTextValuesForNextReview(front: String, back: String, nextReview: Date): Card {
        updateTextContentIfExist(FRONT, front) ?: roster.add(CardContent(front, FRONT))
        updateTextContentIfExist(BACK, back) ?: roster.add(CardContent(back, BACK))
        return this.copy(rating = 0, nextReview = nextReview, updatedAt = Date())
    }

    private fun updateTextContentIfExist(type: CardContentType, value: String) =
        roster.firstTextValue(type)?.let { content ->
            roster.set(roster.indexOf(content), content.copy(value = value))
        }

    /**
     * Increment the rating of this card, and set the nextReview in 1 day
     */
    fun incrementLearnedLevel(): Card {
        return this.copy(rating = this.rating + 1, nextReview = oneDayFromNow())
    }

    /**
     * To be eligible for review, a card must have a rating inferior to 5
     * (5 means the card is learned), and its nextReview should be before now
     */
    fun needReview(): Boolean {
        return ratingLevel() != FAMILIAR && nextReview.before(now)
    }

    fun ratingLevel() = when(rating) {
        0 -> NEW
        in 1..4 -> TRAINING
        else -> FAMILIAR
    }

    internal fun hasRatingLevel(ratingLevel: RatingLevel) = ratingLevel() == ratingLevel

    private val now = Date()

    private fun oneDayFromNow(): Date {
        return Date(now.time + ONE_DAY_IN_MS)
    }

    enum class RatingLevel { NEW, TRAINING, FAMILIAR }
}

/**
 * Redefine MutableList<Card> as a Deck
 * A Deck can contains cards that do not belong to the same booklet
 */
class Deck(cards: MutableList<Card>): MutableList<Card> by cards {

    constructor(): this(mutableListOf())

    fun countNewCard() = filter { it.hasRatingLevel(NEW) }.size
    fun countTrainingCard() = filter { it.hasRatingLevel(TRAINING) }.size
    fun countFamiliarCard() = filter { it.hasRatingLevel(FAMILIAR) }.size
    fun countToReviewCard() = filter { it.needReview() }.size

    fun mapDecksByBookletId(): Map<Long, Deck> {
        val result: HashMap<Long, Deck> = HashMap()
        this.forEach { result.add(it) }
        return result
    }

    private fun HashMap<Long, Deck>.add(card: Card) {
        var deck = this[card.bookletId]
        if (deck == null) {
            deck = Deck(mutableListOf())
            this[card.bookletId] = deck
        }
        deck.add(card)
    }
}

fun List<Card>.toDeck(): Deck = Deck(this.toMutableList())
