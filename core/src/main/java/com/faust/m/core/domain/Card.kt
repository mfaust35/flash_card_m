package com.faust.m.core.domain

data class CardContent (
    val value: String,
    val type: String,
    val cardId: Long,
    val id: Long = 0
)

data class Card (
    val content: Map<String, List<CardContent>>,
    val bookletId: Long,
    val id: Long = 0
)