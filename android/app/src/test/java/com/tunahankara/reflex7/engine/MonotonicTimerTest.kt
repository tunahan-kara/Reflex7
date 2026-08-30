package com.tunahankara.reflex7.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class MonotonicTimerTest {
    @Test fun pauseAndResumeExcludePausedTime() {
        var now = 1_000L
        val timer = MonotonicTimer { now }
        timer.start(4_000)
        now += 1_250
        assertEquals(2_750, timer.pause())
        now += 50_000
        assertEquals(2_750, timer.remainingMs())
        timer.resume()
        now += 750
        assertEquals(2_000, timer.remainingMs())
    }

    @Test fun remainingTimeNeverBecomesNegative() {
        var now = 0L
        val timer = MonotonicTimer { now }
        timer.start(10)
        now = 100
        assertEquals(0, timer.remainingMs())
        timer.stop()
        assertFalse(timer.isRunning)
    }
}
