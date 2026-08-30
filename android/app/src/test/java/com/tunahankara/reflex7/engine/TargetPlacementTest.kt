package com.tunahankara.reflex7.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TargetPlacementTest {
    @Test fun layoutsRemainFairAcrossSupportedShapesAndCounts() {
        val scenarios = listOf(
            Triple(4, 320f, 320f),
            Triple(7, 320f, 320f),
            Triple(4, 180f, 300f),
            Triple(6, 300f, 180f),
            Triple(4, 220f, 110f)
        )
        scenarios.forEachIndexed { scenario, (count, width, height) ->
            repeat(2_000) { seed ->
                val positions = TargetPlacement.generate(count, width, height, SeededRandom(scenario * 10_000L + seed))
                assertEquals(count, positions.size)
                assertTrue("$count targets at ${width}x$height, seed $seed", TargetPlacement.isFair(positions, width, height))
            }
        }
    }

    @Test fun generationIsDeterministicAndBounded() {
        val first = TargetPlacement.generate(8, 240f, 140f, SeededRandom(77))
        val second = TargetPlacement.generate(8, 240f, 140f, SeededRandom(77))
        assertEquals(first, second)
        assertTrue(TargetPlacement.isFair(first, 240f, 140f))
    }

    @Test fun commonAndroidViewportArenasKeepEverySequenceTargetReachable() {
        val screens = listOf(
            320f to 568f,
            360f to 640f,
            360f to 800f,
            393f to 873f,
            412f to 915f,
            640f to 320f
        )
        screens.forEachIndexed { scenario, (screenWidth, screenHeight) ->
            val arena = minOf(screenWidth * .86f, screenHeight * .62f, 320f)
            val contentHeight = (arena - 46f).coerceAtLeast(54f)
            repeat(1_000) { seed ->
                val positions = TargetPlacement.generate(4, arena, contentHeight, SeededRandom(80_000L + scenario * 1_000L + seed))
                assertTrue("$screenWidth x $screenHeight, seed $seed", TargetPlacement.isFair(positions, arena, contentHeight))
            }
        }
    }
}
