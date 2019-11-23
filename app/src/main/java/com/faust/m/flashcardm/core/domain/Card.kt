package com.faust.m.flashcardm.core.domain

import com.faust.m.flashcardm.core.domain.Card.RatingLevel.*
import com.faust.m.flashcardm.core.domain.CardContentType.BACK
import com.faust.m.flashcardm.core.domain.CardContentType.FRONT
import java.util.*
import kotlin.collections.HashMap

enum class CardContentType { FRONT, BACK }

data class CardContent (
    val value: String = "",
    val type: CardContentType = FRONT,
    val cardId: Long = 0,
    val id: Long = 0
)

/**
 * Redefine MutableList<CardContent> as Roster
 * A Roster can contain cardContent that do not belong to the same card
 */
class Roster(private val cardContents: MutableList<CardContent>) :
    MutableList<CardContent> by cardContents {

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
    val lastSeen: Date = Date(),
    val createdAt: Date = lastSeen,
    val roster: Roster = Roster(),
    val bookletId: Long = 0,
    val id: Long = 0
) {

    fun add(cardContent: CardContent): Card = apply {
        roster.add(cardContent)
    }

    fun frontAsTextOrNull() = roster.firstOrNull { c -> c.type == FRONT }?.value

    fun backAsTextOrNull() = roster.firstOrNull { c -> c.type == BACK }?.value

    fun addFrontAsText(text: String) = add(CardContent(text, FRONT))

    fun addBackAsText(text: String) = add(CardContent(text, BACK))

    fun editFrontAsText(newValue: String) = editAsText(newValue, FRONT)

    private fun editAsText(newValue: String, type: CardContentType) =
        roster.firstOrNull { c -> c.type == type }?.run {
            roster.remove(this)
            roster.add(this.copy(value = newValue))
        }

    fun editBackAsText(newValue: String) = editAsText(newValue, BACK)

    fun copyWithoutContent() = this.copy(roster = Roster())

    /**
     * To be eligible for review, a card must have a rating inferior to 5
     * (5 means the card is learned), and it must have not have been reviewed today
     */
    fun needReview(): Boolean {
        return ratingLevel() != FAMILIAR && needReviewToday()
    }

    fun ratingLevel() = when(rating) {
        0 -> NEW
        in 1..4 -> TRAINING
        else -> FAMILIAR
    }

    internal fun hasRatingLevel(ratingLevel: RatingLevel) = ratingLevel() == ratingLevel

    /**
     * If lastSeen = createdAt, this card can need review
     * Else, this card only need review if last seen is not today
     */
    private fun needReviewToday(): Boolean {
        if (this.lastSeen == this.createdAt)
            return true
        if (this.lastSeen.before(this.createdAt))
            return true
        Calendar.getInstance(Locale.getDefault()).let { today: Calendar ->
            today.set(Calendar.HOUR, 0)
            today.set(Calendar.MINUTE, 0)
            today.set(Calendar.SECOND, 0)
            today.set(Calendar.MILLISECOND, 0)
            return today.after(Calendar.getInstance(Locale.getDefault()).apply { time = lastSeen })
        }
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
