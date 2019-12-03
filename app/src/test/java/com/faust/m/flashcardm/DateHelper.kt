package com.faust.m.flashcardm

import com.faust.m.flashcardm.core.domain.ONE_DAY_IN_MS
import java.util.*

val tomorrow = Date().apply { time += ONE_DAY_IN_MS }
val yesterday = Date().apply { time -= ONE_DAY_IN_MS }