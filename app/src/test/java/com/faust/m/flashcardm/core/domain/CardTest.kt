package com.faust.m.flashcardm.core.domain

import com.faust.m.flashcardm.core.domain.CardContentType.BACK
import com.faust.m.flashcardm.core.domain.CardContentType.FRONT
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.util.*

class CardTest {

    // Default cards and cardContent
    // Booklet1                 id(1          )
    // + -- Card1B1             id(+ -- 1     )
    //      + -- CardContent1B1 id(     + -- 1)
    //      + -- CardContent2B1 id(     + -- 2)
    // + -- Card2B1             id(+ -- 2     )
    // Booklet3                 id(3          )
    // + -- Card3B3             id(+ -- 3     )
    //      + -- CardContent3B3 id(     + -- 3)
    private val card1B1: Card = Card(bookletId = 1, id = 1)
    private val card2B1: Card = Card(bookletId = 1, id = 2)
    private val card3B3: Card = Card(bookletId = 3, id = 3)

    private val content1B1: CardContent = CardContent(cardId = 1, id = 1)
    private val content2B1: CardContent = CardContent(cardId = 1, id = 2)
    private val content3B3: CardContent = CardContent(cardId = 3, id = 3)


    @Test
    fun testMapDecksGivenEmptyDeckShouldReturnEmptyMap() {
        // Given an empty deck
        val emptyDeck = Deck()

        emptyDeck.mapDecksByBookletId().let { result ->
            assertThat(result).isEmpty()
        }
    }

    @Test
    fun testMapDecksGivenDeckWithOneBookletShouldReturnMapWithOneBooklet() {
        // Given a deck with a card in booklet1
        val deck = listOf(card1B1).toDeck()

        deck.mapDecksByBookletId().let { result ->

            // Result map contain one bookletId, with a deck of previous card
            assertThat(result).hasSize(1)
            assertThat(result[1]).containsExactly(card1B1)
        }

    }

    @Test
    fun testMapDecksGivenDeckWithTwoBookletsShouldReturnMapWithTwoBooklets() {
        // Given a deck with 1 card in booklet1 and 1 card in booklet3
        val deck = listOf(card1B1, card3B3).toDeck()

        deck.mapDecksByBookletId().let { result ->

            // Result map contain 2 bookletIds, with decks containing previous cards
            assertThat(result).hasSize(2)
            assertThat(result[1]).containsExactly(card1B1)
            assertThat(result[3]).containsExactly(card3B3)
        }
    }

    @Test
    fun testMapDecksGivenDeckWithMultipleCardsForSameBookletShouldReturnMapWithDeck() {
        // Given a deck with 2 card in booklet1
        val deck = listOf(card1B1, card2B1).toDeck()

        deck.mapDecksByBookletId().let { result ->
            // Result map contain 1 bookletId. with a deck of 2 previous cards
            assertThat(result).hasSize(1)
            assertThat(result[1]).containsExactlyInAnyOrder(card1B1, card2B1)
        }
    }

    @Test
    fun testCountNewCardShouldNotCountCardWithRatingSuperiorTo0() {
        // Given a deck with a card with rating superior to 0 (= not new)
        val deck = listOf(Card(rating = 2)).toDeck()

        deck.countNewCard().let { result ->
            assertThat(result).isEqualTo(0)
        }
    }


    @Test
    fun testCountNewCardShouldCountCardWithRating0() {
        // Given a deck with 2 cards with rating 0 (= new) and another
        val deck = listOf(
            Card(rating = 0), Card(rating = 0), Card(rating = 2)
        ).toDeck()

        deck.countNewCard().let { result ->
            assertThat(result).isEqualTo(2)
        }
    }

    @Test
    fun testCountTrainingCardShouldNotCountCardWithRatingInferiorTo1OrSuperiorTo4() {
        // Given a deck with a card with rating superior to 4 (= not training)
        val deck = listOf(Card(rating = 5), Card(rating = 0)).toDeck()

        deck.countTrainingCard().let { result ->
            assertThat(result).isEqualTo(0)
        }
    }

    @Test
    fun testCountTrainingCardShouldCountCardWithRatingBetween1And4() {
        // Given a deck with 2 card with rating 3 (= training) and another
        val deck = listOf(
            Card(rating = 3), Card(rating = 3), Card(rating = 5)
        ).toDeck()

        deck.countTrainingCard().let { result ->
            assertThat(result).isEqualTo(2)
        }
    }

    @Test
    fun testCountFamiliarCardShouldNotCountCardWithRatingInferiorTo5() {
        // Given a deck with a card with rating inferior to 5 (= not familiar)
        val deck = listOf(Card(rating = 1)).toDeck()

        deck.countFamiliarCard().let { result ->
            assertThat(result).isEqualTo(0)
        }
    }

    @Test
    fun testCountFamiliarCardShouldCountCardWithRating5() {
        // Given a deck with 2 cards with rating 5(= familiar) and another
        val deck = listOf(
            Card(rating = 5), Card(rating = 5), Card(rating = 1)
        ).toDeck()

        deck.countFamiliarCard().let { result ->
            assertThat(result).isEqualTo(2)
        }
    }

    @Test
    fun testCountToReviewCardShouldNotCountCardWithRatingFive() {
        // Given a deck with a card of rating 5 and nextReview yesterday
        val card = Card(rating = 5, nextReview = yesterday())
        val deck = listOf(card).toDeck()

        deck.countToReviewCard().let { result ->
            assertThat(result).isEqualTo(0)
        }
    }

    @Test
    fun testCountToReviewCardShouldNotCountCardWithNextReviewAfterNow() {
        // Given a deck with a card of rating 2 and nextReview tomorrow
        val card = Card(rating = 2, nextReview = tomorrow(), createdAt = Date(300))
        val deck = listOf(card).toDeck()

        deck.countToReviewCard().let { result ->
            assertThat(result).isEqualTo(0)
        }
    }

    @Test
    fun testCountToReviewCardShouldCountCardRatingLowerThanFiveAndNextReviewBeforeNow() {
        // Given a deck with a card of rating 2 and nextReview yesterday
        val card = Card(rating = 2, nextReview = yesterday())
        val deck = listOf(card).toDeck()

        deck.countToReviewCard().let { result ->
            assertThat(result).isEqualTo(1)
        }
    }

    @Test
    fun testCardEqualsShouldBeTrueWhenRosterContainSameCardContent() {
        val card = Card(
            rating = 2,
            nextReview = Date(30),
            updatedAt = Date(70),
            createdAt = Date(20),
            roster = mutableListOf(CardContent(
                value = "to learn",
                type = FRONT,
                cardId = 35,
                id = 22
            )).toRoster(),
            bookletId = 14,
            id = 35
        )
        val identicalCard = Card(
            rating = 2,
            nextReview = Date(30),
            updatedAt = Date(70),
            createdAt = Date(20),
            roster = mutableListOf(CardContent(
                value = "to learn",
                type = FRONT,
                cardId = 35,
                id = 22
            )).toRoster(),
            bookletId = 14,
            id = 35
        )

        assertThat(card).isEqualTo(identicalCard)
    }

    @Test
    fun testCardEqualShouldBeFalseWhenRosterContainDifferentCardContent() {
        val card = Card(
            rating = 2,
            nextReview = Date(30),
            updatedAt = Date(70),
            createdAt = Date(20),
            roster = mutableListOf(CardContent(
                value = "to learn",
                type = FRONT,
                cardId = 35,
                id = 22
            )).toRoster(),
            bookletId = 14,
            id = 35
        )
        val identicalCard = Card(
            rating = 2,
            nextReview = Date(30),
            updatedAt = Date(70),
            createdAt = Date(20),
            roster = mutableListOf(CardContent(
                value = "to learn",
                type = BACK,
                cardId = 35,
                id = 22
            )).toRoster(),
            bookletId = 14,
            id = 35
        )

        assertThat(card).isNotEqualTo(identicalCard)
    }

    @Test
    fun testMapRosterGivenEmptyRosterShouldReturnEmptyMap() {
        // Given an empty roster
        val emptyRoster = listOf<CardContent>().toRoster()

        emptyRoster.mapRosterByCardId().let { result ->
            assertThat(result).isEmpty()
        }
    }

    @Test
    fun testMapRosterGivenRosterWithOneCardShouldReturnMapWithOneCardId() {
        // Given a roster with a cardContent in card1
        val roster = listOf(content1B1).toRoster()

        roster.mapRosterByCardId().let { result ->
            // Result map contain one cardId, with a roster of previous cardContent
            assertThat(result).hasSize(1)
            assertThat(result[1]).containsExactly(content1B1)
        }
    }

    @Test
    fun testMapRosterGivenRosterWithTwoCardsShouldReturnMapWithTwoCardId() {
        // Given a roster with 1 cardContent in card1 and 1 cardContent in card3
        val roster = listOf(content1B1, content3B3).toRoster()

        roster.mapRosterByCardId().let { result ->
            // Result map should contain 2 cardId, with roster containing correct cardContent
            assertThat(result).hasSize(2)
            assertThat(result[1]).containsExactly(content1B1)
            assertThat(result[3]).containsExactly(content3B3)
        }
    }

    @Test
    fun testMapRosterGivenRosterWithMultipleCardContentForSameCardShouldReturnMapWithFullRoster() {
        // Given a roster with 2 cardContent in card1
        val roster = listOf(content1B1, content2B1).toRoster()

        roster.mapRosterByCardId().let { result ->
            // Result map should contain 1 cardId, with a roster containing both cardContent
            assertThat(result).hasSize(1)
            assertThat(result[1]).containsExactly(content1B1, content2B1)
        }
    }

    @Test
    fun testEfficiencyOfMapDeckByBookletIdOnSmallDeck() {
        // Given a deck with 2 cards without cardContent
        val deck = listOf(card1B1, card3B3).toDeck()

        val timeStart = System.currentTimeMillis()
        deck.mapDecksByBookletId()
        val timeEnd = System.currentTimeMillis()
        val duration = timeEnd - timeStart

        assertThat(duration).isLessThan(1)
    }

    @Test
    fun testEfficiencyOfMapDeckByBookletIdOnLargeDeck() {
        // Given a deck with 500 cards without cardContent
        val deck = generateShuffledLargeList(500)

        val timeStart = System.currentTimeMillis()
        deck.mapDecksByBookletId()
        val timeEnd = System.currentTimeMillis()
        val duration = timeEnd - timeStart

        assertThat(duration).isLessThanOrEqualTo(2)
    }

    @Test
    fun testEfficiencyOfMapDeckByBookletIdOnVeryLargeDeck() {
        // Given a deck with 500 cards without cardContent
        val deck = generateShuffledLargeList(5000)

        val timeStart = System.currentTimeMillis()
        deck.mapDecksByBookletId()
        val timeEnd = System.currentTimeMillis()
        val duration = timeEnd - timeStart

        assertThat(duration).isLessThanOrEqualTo(3)
    }

    @Test
    fun testUpdateTextValuesWhenCardHasNoCardContentFrontShouldAddCardContentFront() {
        val cardWithoutFrontText = Card()
        cardWithoutFrontText.updateTextValues("new_front", "").let { result ->
            assertThat(result.firstTextValue(FRONT)?.value).isEqualTo("new_front")
        }
    }

    @Test
    fun testUpdateTextValuesWhenCardHasNoCardContentBackShouldAddCardContentBack() {
        val cardWithoutFrontText = Card()
        cardWithoutFrontText.updateTextValues("", "new_back").let { result ->
            assertThat(result.firstTextValue(BACK)?.value).isEqualTo("new_back")
        }
    }

    @Test
    fun testUpdateTextValuesWhenCardHasCardContentFrontShouldUpdateCardContentValue() {
        val cardWithCardContent = cardWithCardContent()
        cardWithCardContent.updateTextValues("new", "").let { result ->
            assertThat(result.firstTextValue(FRONT)?.value).isEqualTo("new")
        }
    }

    @Test
    fun testUpdateTextValuesWhenCardHasCardContentBackShouldUpdateCardContentValue() {
        val cardWithCardContent = cardWithCardContent()
        cardWithCardContent.updateTextValues("", "new").let { result ->
            assertThat(result.firstTextValue(BACK)?.value).isEqualTo("new")
        }
    }


    private fun tomorrow() = Date().apply { time += ONE_DAY_IN_MS }
    private fun yesterday() = Date().apply { time -= ONE_DAY_IN_MS }

    private fun generateShuffledLargeList(largeDefinition: Int): Deck {
        val result = Deck()
        repeat(largeDefinition) {
            result.add(Card(
                rating = 0,
                nextReview = Date(30),
                updatedAt = Date(70),
                createdAt = Date(2000),
                bookletId = (it / 10).toLong(),
                id = it.toLong()
            ))
        }
        result.shuffle()
        return result
    }

    private fun cardWithCardContent() =
        Card(roster = mutableListOf(
            CardContent(value = "old_front", type = FRONT),
            CardContent(value = "old_back", type = BACK)
        ).toRoster())
}
