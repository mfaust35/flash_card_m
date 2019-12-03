package com.faust.m.flashcardm.presentation.booklet

import com.faust.m.flashcardm.core.domain.Card
import com.faust.m.flashcardm.core.domain.Filter
import com.faust.m.flashcardm.core.domain.FilterType.EQUAL
import com.faust.m.flashcardm.core.domain.FilterType.INFERIOR
import com.faust.m.flashcardm.core.domain.declareFilterState
import com.faust.m.flashcardm.yesterday
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class CardFilterTest {

    private val timestampFilter = Filter.Timestamp(Card::nextReview, yesterday, INFERIOR)
    private val numericFilter = Filter.Numeric(Card::bookletId, 5, EQUAL)

    @Test
    fun testDeclareFilterStateWithFiltersShouldReturnFilterStateWhichContainFilters() {
        val filterState = declareFilterState {
            with { timestampFilter }
            with { numericFilter }
        }

        assertThat(filterState.contains(timestampFilter)).isTrue()
        assertThat(filterState.contains(numericFilter)).isTrue()
    }

    @Test
    fun testFilterStatePlusFilterShouldReturnFilterStateCopyWithAdditionalFilter() {
        // Given an existing filterState with a filter inside
        val oldFilterState = declareFilterState { with { timestampFilter } }

        (oldFilterState + { numericFilter }).let { result ->

            assertThat(result.contains(numericFilter)).isTrue()
        }
    }

    @Test
    fun testRemoveFilterFromFilterStateShouldReturnFilterStateWithCorrectFilterUnits() {
        val oldFilterState = declareFilterState { with { timestampFilter } }

        (oldFilterState - { timestampFilter }).let { result ->

            assertThat(result.contains(timestampFilter)).isFalse()
        }
    }
}