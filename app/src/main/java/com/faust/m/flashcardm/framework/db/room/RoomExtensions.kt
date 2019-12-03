package com.faust.m.flashcardm.framework.db.room

import com.faust.m.flashcardm.core.domain.Card
import kotlin.reflect.KProperty1


class NameForProperty(private val _innerMap: Map<KProperty1<Card, Any>, String>)
    : Map<KProperty1<Card, Any>, String> by _innerMap {

    override fun get(key: KProperty1<Card, Any>): String = _innerMap[key] ?:
        throw Exception("No name for this property, check the entity corresponding")

}

fun mapPropertyToName(vararg pairs: Pair<KProperty1<Card, Any>, String>): NameForProperty =
    NameForProperty(mapOf(*pairs))
