package com.tunahankara.reflex7.engine

fun interface MonotonicClock { fun nowMs(): Long }

class MonotonicTimer(private val clock: MonotonicClock) {
    private var deadlineMs = 0L
    private var pausedRemainingMs = 0L
    var isRunning: Boolean = false
        private set

    fun start(durationMs: Long) {
        pausedRemainingMs = durationMs.coerceAtLeast(0)
        deadlineMs = clock.nowMs() + pausedRemainingMs
        isRunning = true
    }

    fun remainingMs(): Long = if (isRunning) (deadlineMs - clock.nowMs()).coerceAtLeast(0) else pausedRemainingMs

    fun pause(): Long {
        if (isRunning) pausedRemainingMs = remainingMs()
        isRunning = false
        return pausedRemainingMs
    }

    fun resume() {
        if (isRunning) return
        deadlineMs = clock.nowMs() + pausedRemainingMs
        isRunning = true
    }

    fun stop() {
        pausedRemainingMs = 0
        isRunning = false
    }
}
