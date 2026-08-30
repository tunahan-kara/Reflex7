package com.tunahankara.reflex7.engine

import kotlin.math.ceil
import kotlin.math.hypot
import kotlin.math.sqrt

data class TargetPosition(val x: Float, val y: Float)

object TargetPlacement {
    const val TARGET_DIAMETER_DP = 54f
    const val MIN_CENTER_DISTANCE_RATIO = 0.72f
    private const val MAX_ATTEMPTS_PER_TARGET = 64

    fun generate(
        count: Int,
        widthDp: Float,
        heightDp: Float,
        random: RandomSource,
        diameterDp: Float = TARGET_DIAMETER_DP
    ): List<TargetPosition> {
        require(count >= 0)
        if (count == 0) return emptyList()
        val width = widthDp.coerceAtLeast(diameterDp)
        val height = heightDp.coerceAtLeast(diameterDp)
        val radius = diameterDp / 2f
        val minimumDistance = diameterDp * MIN_CENTER_DISTANCE_RATIO
        val accepted = mutableListOf<Pair<Float, Float>>()

        repeat(count) {
            var candidate: Pair<Float, Float>? = null
            repeat(MAX_ATTEMPTS_PER_TARGET) {
                val x = radius + random.nextDouble().toFloat() * (width - diameterDp)
                val y = radius + random.nextDouble().toFloat() * (height - diameterDp)
                if (accepted.all { (px, py) -> hypot(x - px, y - py) >= minimumDistance }) {
                    candidate = x to y
                    return@repeat
                }
            }
            if (candidate == null) return fallback(count, width, height, diameterDp)
            accepted += candidate!!
        }
        return accepted.map { (x, y) -> normalize(x, y, width, height, diameterDp) }
    }

    fun isFair(
        positions: List<TargetPosition>,
        widthDp: Float,
        heightDp: Float,
        diameterDp: Float = TARGET_DIAMETER_DP
    ): Boolean {
        if (widthDp < diameterDp || heightDp < diameterDp) return false
        val centers = positions.map { position ->
            diameterDp / 2f + position.x * (widthDp - diameterDp) to
                diameterDp / 2f + position.y * (heightDp - diameterDp)
        }
        return centers.indices.all { index ->
            val (x, y) = centers[index]
            x >= diameterDp / 2f && x <= widthDp - diameterDp / 2f &&
                y >= diameterDp / 2f && y <= heightDp - diameterDp / 2f &&
                centers.indices.filter { it != index }.all { other ->
                    hypot(x - centers[other].first, y - centers[other].second) >=
                        diameterDp * MIN_CENTER_DISTANCE_RATIO
                }
        }
    }

    private fun fallback(count: Int, width: Float, height: Float, diameter: Float): List<TargetPosition> {
        val aspect = width / height
        val columns = ceil(sqrt(count * aspect)).toInt().coerceIn(1, count)
        val rows = ceil(count.toDouble() / columns).toInt()
        return (0 until count).map { index ->
            val column = index % columns
            val row = index / columns
            val x = if (columns == 1) width / 2f else diameter / 2f + column * (width - diameter) / (columns - 1)
            val y = if (rows == 1) height / 2f else diameter / 2f + row * (height - diameter) / (rows - 1)
            normalize(x, y, width, height, diameter)
        }
    }

    private fun normalize(x: Float, y: Float, width: Float, height: Float, diameter: Float) = TargetPosition(
        if (width == diameter) .5f else ((x - diameter / 2f) / (width - diameter)).coerceIn(0f, 1f),
        if (height == diameter) .5f else ((y - diameter / 2f) / (height - diameter)).coerceIn(0f, 1f)
    )
}
