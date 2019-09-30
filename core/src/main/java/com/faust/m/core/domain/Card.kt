package com.faust.m.core.domain

data class CardContent (
    val value: String,
    val type: String,
    val cardId: Long = 0,
    val id: Long = 0
)

data class Card (
    val content: HashMap<String, MutableList<CardContent>> = HashMap(),
    val bookletId: Long = 0,
    val id: Long = 0
) {

    fun add(cardContent: CardContent): Card = apply {
        content
            .getOrPut(cardContent.type) { mutableListOf() }
            .add(cardContent)
    }
}