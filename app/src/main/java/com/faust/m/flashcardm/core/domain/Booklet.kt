package com.faust.m.flashcardm.core.domain

data class Booklet(val name: String, val id: Long = 0)

/**
 * Redefine MutableList<Booklet> as a Library
 */
class Library constructor(booklets: MutableList<Booklet>): MutableList<Booklet> by booklets

fun List<Booklet>.toLibrary(): Library = Library(this.toMutableList())
