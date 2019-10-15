package com.faust.m.core.domain

import java.util.*

data class CardContent (
    val value: String,
    val type: String,
    val cardId: Long = 0,
    val id: Long = 0
)

data class Card (
    val rating: Int = 0,
    val lastSeen: Date = Date(),
    val createdAt: Date = lastSeen,
    val content: HashMap<String, MutableList<CardContent>> = HashMap(),
    val bookletId: Long = 0,
    val id: Long = 0
) {

    fun add(cardContent: CardContent): Card = apply {
        content
            .getOrPut(cardContent.type) { mutableListOf() }
            .add(cardContent)
    }

    fun frontAsTextOrNull() = content["front"]?.firstOrNull()?.value

    fun backAsTextOrNull() = content["back"]?.firstOrNull()?.value
}