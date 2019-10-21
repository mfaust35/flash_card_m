package com.faust.m.core.domain

import com.faust.m.core.domain.CardContentType.BACK
import com.faust.m.core.domain.CardContentType.FRONT
import java.util.*

enum class CardContentType { FRONT, BACK }

data class CardContent (
    val value: String,
    val type: CardContentType,
    val cardId: Long = 0,
    val id: Long = 0
)

data class Card (
    val rating: Int = 0,
    val lastSeen: Date = Date(),
    val createdAt: Date = lastSeen,
    val content: EnumMap<CardContentType, MutableList<CardContent>> =
        EnumMap(CardContentType::class.java),
    val bookletId: Long = 0,
    val id: Long = 0
) {

    fun add(cardContent: CardContent): Card = apply {
        content
            .getOrPut(cardContent.type) { mutableListOf() }
            .add(cardContent)
    }

    fun frontAsTextOrNull() = content[FRONT]?.firstOrNull()?.value

    fun backAsTextOrNull() = content[BACK]?.firstOrNull()?.value

    fun addFrontAsText(text: String) = add(CardContent(text, FRONT))

    fun addBackAsText(text: String) = add(CardContent(text, BACK))

    fun editFrontAsText(newValue: String) = editAsText(newValue, FRONT)

    private fun editAsText(newValue: String, type: CardContentType) = content[type]?.let {
        it.firstOrNull()?.run {
            it.remove(this)
            it.add(this.copy(value = newValue))
        }
    }

    fun editBackAsText(newValue: String) = editAsText(newValue, BACK)

    internal fun needReview(): Boolean {
        return rating < 5 && needReviewToday()
    }

    private fun needReviewToday(): Boolean {
        if (this.lastSeen == this.createdAt)
            return true
        if (this.lastSeen.before(this.createdAt))
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