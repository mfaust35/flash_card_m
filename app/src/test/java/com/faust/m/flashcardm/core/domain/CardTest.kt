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
        // Given a deck with a card of rating 5 and dateToday > dateLastSeen > dateCreatedAt
        val card = Card(rating = 5, lastSeen = Date(30000), createdAt = Date(10))
        val deck = listOf(card).toDeck()

        deck.countToReviewCard().let { result ->
            assertThat(result).isEqualTo(0)
        }
    }

    @Test
    fun testCountToReviewCardShouldNotCountCardWithDateLastSeenTodayDifferentFromCreateAt() {
        // Given a deck with a card of rating 2 and dateLastSeen is today different from createdAt
        val card = Card(rating = 2, lastSeen = today(), createdAt = Date(300))
        val deck = listOf(card).toDeck()

        deck.countToReviewCard().let { result ->
            assertThat(result).isEqualTo(0)
        }
    }

    @Test
    fun testCountToReviewCardShouldCountCardWithEqualDateLastSeenAndCreatedAt() {
        // Given a deck with a card of rating 2 and dateLastSeen = dateCreatedAt
        val card = Card(rating = 2, lastSeen = Date(300), createdAt = Date(300))
        val deck = listOf(card).toDeck()

        deck.countToReviewCard().let { result ->
            assertThat(result).isEqualTo(1)
        }
    }

    @Test
    fun testCountToReviewCardShouldCountCardWithCreatedAtAfterLastSeen() {
        // Given a deck with card of rating 2 with dateCreatedAt > dateLastSeen
        // Kind of weird naming here, but it means the card has been modified after begin seen
        val card = Card(rating = 2, lastSeen = today(), createdAt = now())
        val deck = listOf(card).toDeck()

        deck.countToReviewCard().let { result ->
            assertThat(result).isEqualTo(1)
        }
    }

    @Test
    fun testCardEqualsShouldBeTrueWhenRosterContainSameCardContent() {
        val card = Card(
            rating = 2,
            lastSeen = Date(30),
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
            lastSeen = Date(30),
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
            lastSeen = Date(30),
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
            lastSeen = Date(30),
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

    private fun now(): Date = Date()
    private fun today(): Date =
        Calendar.getInstance(Locale.getDefault())
            .apply {
                set(Calendar.HOUR, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            .time
}
