package com.faust.m.flashcardm.framework.db.room

import androidx.sqlite.db.SupportSQLiteProgram
import androidx.sqlite.db.SupportSQLiteQuery
import com.faust.m.flashcardm.core.domain.Card
import com.faust.m.flashcardm.core.domain.Filter
import com.faust.m.flashcardm.core.domain.FilterState
import com.faust.m.flashcardm.core.domain.FilterType.EQUAL
import com.faust.m.flashcardm.core.domain.FilterType.INFERIOR
import com.faust.m.flashcardm.framework.db.room.model.CardTableName
import com.faust.m.flashcardm.yesterday
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class CardRoomFilterTest {

    private val program: SupportSQLiteProgram = mockk(relaxed = true)
    private val filterState: FilterState = mockk()

    @Test
    fun testQueryFromFilterNumericEqualShouldReturnSupportSQLiteQueryWithCorrectSql() {
        givenFilterStateWithOneNumericFilterEqual()
            .toRoomQuery()
            .let { result ->

                assertThat(result.rawQuery)
                    .`as`("Sql created incorrectly")
                    .isEqualTo("SELECT * FROM $CardTableName WHERE booklet_id = ?")
            }
    }

    @Test
    fun testQueryFromFilterNumericEqualShouldReturnSupportSQLiteQueryWithCorrectQueryArgs() {
        givenFilterStateWithOneNumericFilterEqual()
            .toRoomQuery()
            .let { result ->

                // Then result has correct query args
                result.verifyBinding {
                    bindLong(1, 1)
                }
            }
    }

    @Test
    fun testQueryFromFilterTimestampEqualShouldReturnSupportSQLiteQueryWithCorrectSql() {
        givenFilterStateWithOneTimestampFilterInferior()
            .toRoomQuery()
            .let { result ->

                assertThat(result.rawQuery)
                    .`as`("Sql created incorrectly")
                    .isEqualTo("SELECT * FROM $CardTableName WHERE next_review < ?")
            }
    }

    @Test
    fun testQueryFromFilterTimestampEqualShouldReturnSupportSQLiteQueryWithCorrectQueryArgs() {
        givenFilterStateWithOneTimestampFilterInferior()
            .toRoomQuery()
            .let { result ->

                result.verifyBinding {
                    bindLong(1, yesterday.time)
                }
            }
    }

    @Test
    fun testQueryFromDoubleFilterShouldReturnSupportSQLiteQueryWithCorrectSql() {
        givenDoubleFilter()
            .toRoomQuery()
            .let { result ->

                assertThat(result.rawQuery)
                    .`as`("Sql created incorrectly")
                    .isEqualTo("SELECT * FROM $CardTableName WHERE booklet_id = ? AND next_review < ?")
            }
    }

    @Test
    fun testQueryFromDoubleFilterShouldReturnSupportSQLiteQueryWithCorrectQueryArgs() {
        givenDoubleFilter()
            .toRoomQuery()
            .let { result ->

                result.verifyBinding {
                    bindLong(1, 1)
                    bindLong(2, yesterday.time)
                }
            }
    }


    private fun givenFilterStateWithOneNumericFilterEqual() = filterState.apply {
        every { filters } returns
                listOf(
                    Filter.Numeric(Card::bookletId, 1, EQUAL)
                )
    }

    private fun givenFilterStateWithOneTimestampFilterInferior() = filterState.apply {
        every { filters } returns
                listOf(
                    Filter.Timestamp(Card::nextReview, yesterday, INFERIOR)
                )
    }

    private fun givenDoubleFilter() = filterState.apply {
        every { filters } returns
                listOf(
                    Filter.Numeric(Card::bookletId, 1, EQUAL),
                    Filter.Timestamp(Card::nextReview, yesterday, INFERIOR)
                )
    }

    /**
     * Rename property sql and trim it
     */
    private val SupportSQLiteQuery.rawQuery: String
        get() = sql.trim()

    // Since I cannot get a list of query args, I instead verify that they will be bound correctly
    // to a mockk sql program
    private fun SupportSQLiteQuery.verifyBinding(binding: SupportSQLiteProgram.() -> Unit) {
        bindTo(program)
        verify { program.binding() }
    }
}