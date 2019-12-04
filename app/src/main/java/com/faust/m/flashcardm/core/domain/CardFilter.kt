package com.faust.m.flashcardm.core.domain

import java.util.*
import kotlin.reflect.KProperty1

/**
 * Classes Filter & FilterState are a test on how to write code that would be used like
 * ```
 *      declareFilterState {
 *          with { Filter.Numeric1(bla bla) }
 *          with { Filter.Timestamp2(bla bla) }
 *      }
 * ```
 * instead of the usual function call like
 * ```
 *      declareFilterState(filter1, filter2)
 * ```
 *
 * This does not add or remove anything to the legibility of the code nor to the performance, it's
 * really just a test.
 *
 *
 * Filters are meant to be created in viewModel and passed down to the room library to create
 * a raw query. It is very simplistic and has limited use which is sufficient for this project now.
 *
 * CardRoomFilters is a separate class that makes a filter into a room query. If using another
 * type of dataSource, then another class will transform the filter into a query for this other
 * dataSource
 */

typealias FilterStateDeclaration = FilterState.() -> Unit
typealias FilterDeclaration = () -> Filter

fun declareFilterState(filterStateDeclaration: FilterStateDeclaration): FilterState {
    val filter = FilterState()
    filter.filterStateDeclaration()
    return filter
}


enum class FilterType { SUPERIOR, INFERIOR, EQUAL }

sealed class Filter {

    data class Numeric(
        val property: KProperty1<Card, Number>,
        val value: Number,
        val filterType: FilterType
    ) : Filter()

    data class Timestamp(
        val property: KProperty1<Card, Date>,
        val _value: Date,
        val filterType: FilterType
    ) : Filter() {

        val value: Long
            get() = _value.time
    }
}


class FilterState private constructor(private val _filters: MutableList<Filter>) {

    constructor(): this(mutableListOf())

    fun with(filterDeclaration: FilterDeclaration) {
        _filters.add(filterDeclaration())
    }


    val filters: List<Filter>
        get() = _filters.toList()

    fun contains(filter: Filter) =
        _filters.contains(filter)


    operator fun plus(filterDeclaration: FilterDeclaration): FilterState =
        FilterState(_filters.toMutableList().apply {
            add(filterDeclaration())
        })

    operator fun minus(filterDeclaration: FilterDeclaration): FilterState {
        val filter = filterDeclaration()
        return when {
            _filters.contains(filter) ->
                FilterState(_filters.toMutableList().apply {
                    remove(filter)
                })
            else -> this
        }
    }
}


