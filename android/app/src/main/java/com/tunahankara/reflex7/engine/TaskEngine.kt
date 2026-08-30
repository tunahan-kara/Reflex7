package com.tunahankara.reflex7.engine

import kotlin.math.max
import kotlin.math.roundToLong

class TaskEngine(private val random: RandomSource) {
    private val taskHistory = ArrayDeque<String>()
    private val categoryHistory = ArrayDeque<TaskCategory>()

    fun difficultyBand(level: Int): DifficultyBand = DifficultyProgression.band(level)

    fun candidateWeights(
        level: Int,
        durationSeconds: Double,
        memory: GameMemory,
        activeRule: GlobalRule? = null,
        completedTasks: Int = 0
    ): List<Pair<TaskDefinition, Double>> {
        val recallDue = memory.pendingRecall?.let { completedTasks >= it.dueAt } == true
        return TaskRegistry.tasks.asSequence()
        .filter { level >= it.minLevel && durationSeconds >= it.minDuration }
        .filter { it.id != taskHistory.lastOrNull() }
        .filter { eligible(it, level, memory, completedTasks) }
        .filter {
            recallDue || activeRule == null || (activeRule in it.rules &&
                (activeRule != GlobalRule.FINAL_LINE || it.instructionPhases == InstructionPhases.MULTI_PHASE))
        }
        .filter { !recallDue || it.id == "delayedRecall" }
        .map { definition ->
            var weight = definition.weight
            val progression = ((level - 15) / 35.0).coerceIn(0.0, 1.0)
            val highLevelFactor = when (definition.difficulty) {
                1 -> 0.55
                2 -> 0.75
                3 -> 1.0
                4 -> 1.30
                else -> 1.60
            }
            weight *= 1.0 + (highLevelFactor - 1.0) * progression
            weight *= when (definition.id) {
                "previousMemory" -> 2.2
                "previousRuleRecall" -> 1.7
                "nBack" -> 1.35
                else -> 1.0
            }
            val reversed = taskHistory.reversed()
            val recentIndex = reversed.indexOf(definition.id)
            if (recentIndex >= 0) weight *= 0.18 + recentIndex * 0.12
            if (categoryHistory.lastOrNull() == definition.category) weight *= 0.3
            if (categoryHistory.takeLast(3).count { it == definition.category } >= 2) weight *= 0.45
            definition to max(0.05, weight)
        }.toList()
    }

    fun selectTask(level: Int, durationSeconds: Double, memory: GameMemory, activeRule: GlobalRule?, completedTasks: Int = 0): TaskDefinition {
        val candidates = candidateWeights(level, durationSeconds, memory, activeRule, completedTasks)
        if (candidates.isEmpty()) {
            return TaskRegistry.tasks.firstOrNull {
                level >= it.minLevel && durationSeconds >= it.minDuration && eligible(it, level, memory, completedTasks) &&
                    (activeRule == null || activeRule in it.rules)
            } ?: TaskRegistry.byId("standard")
        }
        val total = candidates.sumOf { it.second }
        var cursor = random.nextDouble() * total
        for ((definition, weight) in candidates) {
            cursor -= weight
            if (cursor <= 0.0) return definition
        }
        return candidates.last().first
    }

    fun remember(definition: TaskDefinition) {
        taskHistory.addLast(definition.id)
        categoryHistory.addLast(definition.category)
        while (taskHistory.size > 6) taskHistory.removeFirst()
        while (categoryHistory.size > 5) categoryHistory.removeFirst()
    }

    fun clearHistory() {
        taskHistory.clear()
        categoryHistory.clear()
    }

    fun nBackDepth(level: Int, memory: GameMemory): Int {
        val maximum = when { level >= 40 -> 3; level >= 32 -> 2; else -> 1 }
        return (maximum downTo 1).firstOrNull { memory.completed.getOrNull(memory.completed.size - it)?.item != null } ?: 0
    }

    fun isRecallDue(memory: GameMemory, completedTasks: Int): Boolean =
        memory.pendingRecall?.let { completedTasks >= it.dueAt } == true

    private fun eligible(definition: TaskDefinition, level: Int, memory: GameMemory, completedTasks: Int): Boolean = when (definition.prerequisite) {
        TaskPrerequisite.NONE -> true
        TaskPrerequisite.PREVIOUS_NUMERIC_ANSWER -> memory.lastCompleted?.numericAnswer != null
        TaskPrerequisite.N_BACK_HISTORY -> nBackDepth(level, memory) > 0
        TaskPrerequisite.PREVIOUS_METADATA -> memory.lastCompleted?.let {
            it.colorAnswer != null || it.side != null || it.parity != null
        } == true
        TaskPrerequisite.DELAYED_RECALL -> memory.pendingRecall == null || completedTasks >= memory.pendingRecall.dueAt
    }

    fun selectModifiers(definition: TaskDefinition, level: Int): List<Modifier> {
        val band = difficultyBand(level)
        if (band.modifierCount == 0 || random.nextDouble() > if (band.modifierCount == 1) 0.55 else 0.78) return emptyList()
        val available = random.shuffle(definition.modifiers.filter { level >= it.minLevel })
        val selected = mutableListOf<Modifier>()
        for (modifier in available) {
            if (selected.size >= band.modifierCount) break
            if ((modifier == Modifier.MOVING && Modifier.SWAP in selected) ||
                (modifier == Modifier.SWAP && Modifier.MOVING in selected)) continue
            selected += modifier
        }
        return selected
    }

    fun maybeStartGlobalRule(level: Int, durationSeconds: Double, completed: Int): ActiveRule? {
        val band = difficultyBand(level)
        if (completed < 10 || random.nextDouble() > band.globalChance) return null
        val compatible = GlobalRule.entries.filter { rule ->
            level >= rule.minLevel && TaskRegistry.tasks.any {
                rule in it.rules && level >= it.minLevel && durationSeconds >= it.minDuration
            }
        }
        if (compatible.isEmpty()) return null
        val rule = compatible[random.nextInt(0, compatible.size)]
        return ActiveRule(rule, random.nextInt(2, if (level >= 40) 5 else 4))
    }

    companion object {
        fun taskDuration(mode: GameMode, level: Int): Double {
            val speedFactor = max(0.4, 1.0 - (level / 5) * 0.05)
            return mode.seconds * speedFactor
        }

        fun score(
            level: Int,
            difficulty: Int,
            modifierCount: Int,
            hasGlobalRule: Boolean,
            remainingMs: Long,
            durationMs: Long,
            combo: Int
        ): Long {
            val normalized = if (durationMs > 0) (remainingMs.toDouble() / durationMs).coerceIn(0.0, 1.0) else 0.0
            val base = 100 + level * 5 + difficulty * 25 + modifierCount * 20 +
                (if (hasGlobalRule) 15 else 0) + (normalized * 100).toInt()
            val multiplier = 1.0 + (combo - 1).coerceIn(0, 10) * 0.05
            return (base * multiplier).roundToLong()
        }
    }
}
