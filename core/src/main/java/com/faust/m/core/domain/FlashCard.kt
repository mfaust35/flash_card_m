package com.faust.m.core.domain

data class FlashCardSideDescription(val text: String)

data class FlashCard(val test: String,
                     val recto: FlashCardSideDescription,
                     val verso: FlashCardSideDescription,
                     val id: Long = 0)