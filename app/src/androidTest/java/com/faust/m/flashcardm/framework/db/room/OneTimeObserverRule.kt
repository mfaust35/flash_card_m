package com.faust.m.flashcardm.framework.db.room

import androidx.lifecycle.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * Helper rule to ensure that observer has been triggered before the end of the test
 * Usage:
 *
 * class MyTestClass {
 *
 *     @get:Rule
 *     val oneTimeRule = OneTimeObserverRule()
 *
 *     @Test
 *     fun myTest() {
 *         setupTest()
 *
 *         getALiveData().observeOnce(oneTimeRule) { result ->
 *
 *             assertThat(result).isXxxx
 *         }
 *     }
 * }
 *
 * If the live data does not call onChanged(), the test will automatically fail
 */
class OneTimeObserverRule : TestWatcher() {

    var observeCount = 0

    override fun starting(description: Description?) {
        observeCount = 0
        super.starting(description)
    }

    override fun finished(description: Description?) {
        assertThat(observeCount).`as`("Observer not triggered").isEqualTo(0)
        super.finished(description)
    }
}

/**
 * Helper code adapted from https://alediaferia.com/2018/12/17/testing-livedata-room-android/
 */
fun <T> LiveData<T>.observeOnce(rule: OneTimeObserverRule, onChangeHandler: (T) -> Unit) {
    rule.observeCount ++
    OneTimeObserver(rule, onChangeHandler).let { observe(it, it) }
}

class OneTimeObserver<T>(private val rule: OneTimeObserverRule,
                         private val handler: (T) -> Unit) : Observer<T>, LifecycleOwner {

    private val lifecycle = LifecycleRegistry(this)

    init {
        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    override fun getLifecycle(): Lifecycle = lifecycle

    override fun onChanged(t: T) {
        handler(t)
        rule.observeCount --
        if (rule.observeCount == 0)
            lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    }
}
