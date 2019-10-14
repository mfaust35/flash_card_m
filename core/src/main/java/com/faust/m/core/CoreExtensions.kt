package com.faust.m.core

import androidx.collection.LongSparseArray

class LongSparseArrayList<T: Any>: LongSparseArray<MutableList<T>> {

    constructor(): super()

    constructor(initialCapacity: Int): super(initialCapacity)

    fun addOrPutInEmptyList(key: Long, value: T) {
        var list: MutableList<T> = mutableListOf()
        this[key]?.let {
            list = it
        } ?: this.append(key, list)
        list.add(value)
    }
}
