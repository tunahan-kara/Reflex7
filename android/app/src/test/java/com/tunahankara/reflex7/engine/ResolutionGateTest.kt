package com.tunahankara.reflex7.engine

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ResolutionGateTest {
    @Test fun eachGenerationCanResolveExactlyOnce() {
        val gate = ResolutionGate()
        gate.begin(7)
        assertTrue(gate.claim(7))
        assertFalse(gate.claim(7))
        assertFalse(gate.claim(6))
        gate.begin(8)
        assertFalse(gate.claim(7))
        assertTrue(gate.claim(8))
        assertFalse(gate.claim(8))
    }
}
