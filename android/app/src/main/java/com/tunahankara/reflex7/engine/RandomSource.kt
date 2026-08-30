package com.tunahankara.reflex7.engine

import kotlin.random.Random

interface RandomSource {
    fun nextDouble(): Double
    fun nextInt(from: Int, until: Int): Int
    fun <T> shuffle(values: List<T>): List<T>
}

class SeededRandom(seed: Long = System.nanoTime()) : RandomSource {
    private val random = Random(seed)
    override fun nextDouble(): Double = random.nextDouble()
    override fun nextInt(from: Int, until: Int): Int = random.nextInt(from, until)
    override fun <T> shuffle(values: List<T>): List<T> = values.shuffled(random)
}
