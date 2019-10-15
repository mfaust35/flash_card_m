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

    internal fun needReview(): Boolean {
        return rating < 5 && needReviewToday()
    }

    private fun needReviewToday(): Boolean {
        if (this.lastSeen == this.createdAt)
            return true
        Calendar.getInstance().let { today: Calendar ->
            today.set(Calendar.HOUR, 0)
            today.set(Calendar.MINUTE, 0)
            today.set(Calendar.SECOND, 0)
            today.set(Calendar.MILLISECOND, 0)
            return today.after(Calendar.getInstance().apply { time = lastSeen })
        }
    }
}