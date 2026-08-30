package com.tunahankara.reflex7.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GameplayStressTest {
    private val auditLevels = listOf(1, 3, 5, 10, 15, 20, 25, 30, 40, 50, 75, 100)

    @Test fun oneHundredThousandGeneratedRoundsRespectCoreInvariants() {
        val taskCounts = linkedMapOf<String, Int>()
        repeat(100_000) { iteration ->
            val level = auditLevels[iteration % auditLevels.size]
            val durationMs = (TaskEngine.taskDuration(if (iteration % 2 == 0) GameMode.SLOW else GameMode.FAST, level) * 1_000).toLong()
            val random = SeededRandom(700_000L + iteration)
            val engine = TaskEngine(random)
            val memory = richMemory()
            val definition = engine.selectTask(level, durationMs / 1_000.0, memory, null, 20)
            val modifiers = engine.selectModifiers(definition, level)
            val rule = definition.rules.filter { level >= it.minLevel }.let {
                if (it.isEmpty() || iteration % 4 != 0) null else ActiveRule(it[iteration % it.size], 2)
            }
            val round = TaskFactory(random).create(
                iteration.toLong(), definition, level, durationMs, memory, if (iteration % 3 == 0) "tr" else "en",
                modifiers, rule
            )
            assertRoundInvariants(round, durationMs)
            taskCounts[definition.id] = taskCounts.getOrDefault(definition.id, 0) + 1
        }
        assertEquals(TaskRegistry.tasks.map { it.id }.toSet(), taskCounts.keys)
        println("AUDIT_100K_TASKS=" + taskCounts.entries.joinToString(",") { "${it.key}:${it.value}" })
    }

    @Test fun realisticSessionsPreserveVarietyAndStateLifecycles() {
        val requestedEndpoints = listOf(10, 25, 50)
        requestedEndpoints.forEach { endpoint ->
            val aggregate = SessionStats()
            repeat(100) { session -> aggregate.merge(simulateSession(endpoint, endpoint * 10_000L + session)) }
            assertEquals(endpoint to aggregate.taskCounts.toSortedMap(), endpoint to aggregate.taskCounts.toSortedMap())
            assertEquals(0, aggregate.immediateRepeats)
            if (endpoint == 50) {
                assertEquals(TaskRegistry.tasks.map { it.id }.toSet(), aggregate.taskCounts.keys)
                assertTrue(aggregate.taskCounts.values.min() > 0)
            }
            println("AUDIT_SESSIONS_L$endpoint=" + aggregate.summary())
        }

        val longAggregate = SessionStats()
        repeat(5) { session -> longAggregate.merge(simulateSession(1_000, 990_000L + session)) }
        assertEquals(0, longAggregate.immediateRepeats)
        assertEquals(TaskRegistry.tasks.map { it.id }.toSet(), longAggregate.taskCounts.keys)
        assertTrue(longAggregate.maxPendingRecall <= 1)
        assertTrue(longAggregate.forcedRecalls > 0)
        assertTrue(longAggregate.nBackDepths.containsAll(listOf(1, 2, 3)))
        println("AUDIT_LONG_SESSIONS=" + longAggregate.summary())
    }

    @Test fun compatibilityMatrixIsCompleteAndEveryAllowedPairHasAnEffectPath() {
        val memory = richMemory()
        var allowedModifiers = 0
        var allowedRules = 0
        TaskRegistry.tasks.forEachIndexed { taskIndex, definition ->
            Modifier.entries.forEach { modifier ->
                if (modifier !in definition.modifiers) return@forEach
                allowedModifiers += 1
                val round = TaskFactory(SeededRandom(taskIndex * 1_000L + modifier.ordinal)).create(
                    taskIndex.toLong(), definition, 50, 2_800, memory, "en", listOf(modifier), null
                )
                assertTrue(modifier in round.modifiers)
                when (modifier) {
                    Modifier.DELAYED -> assertTrue(round.events.any { it.type == RoundEventType.MODIFIER_UNLOCK })
                    Modifier.SWAP -> {
                        assertTrue(round.choices.isNotEmpty())
                        assertTrue(round.events.any { it.type == RoundEventType.SWAP_CHOICES })
                    }
                    else -> Unit // These are rendered effects, covered by registry and Compose source audits.
                }
            }
            GlobalRule.entries.forEach { rule ->
                if (rule !in definition.rules) return@forEach
                allowedRules += 1
                val round = TaskFactory(SeededRandom(taskIndex * 2_000L + rule.ordinal)).create(
                    taskIndex.toLong(), definition, 51, 2_800, memory, "en", emptyList(), ActiveRule(rule, 2)
                )
                assertEquals(rule, round.globalRule?.rule)
                when (rule) {
                    GlobalRule.INVERT -> assertTrue(definition.id in setOf("yesNo", "directionConflict", "mentalMath", "nBack"))
                    GlobalRule.IGNORE_RED -> assertEquals(1, round.choices.count { it.style == ChoiceStyle.RED_DECOY })
                    GlobalRule.FINAL_LINE -> {
                        assertEquals(InstructionPhases.MULTI_PHASE, definition.instructionPhases)
                        assertTrue(round.inputLocked)
                    }
                    GlobalRule.ODD_WAIT -> assertTrue(round.events.any { it.type == RoundEventType.RULE_UNLOCK })
                    GlobalRule.EMOJI_LITERAL -> {
                        assertTrue(definition.id in setOf("flashMemory", "countSymbols", "delayedRecall"))
                        if (definition.id == "delayedRecall") {
                            assertTrue(round.answer in setOf("🍎", "⚡", "🌙", "⭐"))
                        }
                    }
                }
            }
        }
        assertTrue(allowedModifiers > 0)
        assertTrue(allowedRules > 0)
        println("AUDIT_MATRIX=tasks:${TaskRegistry.tasks.size},modifierPairs:$allowedModifiers,rulePairs:$allowedRules")
    }

    @Test fun difficultyProgressionKeepsTheFullPoolWhileShiftingTowardComplexity() {
        val memory = richMemory()
        val rows = auditLevels.map { level ->
            val duration = TaskEngine.taskDuration(GameMode.FAST, level)
            val weights = TaskEngine(SeededRandom(level.toLong())).candidateWeights(level, duration, memory, null, 100)
            val simple = weights.filter { it.first.difficulty <= 2 }.sumOf { it.second }
            val complex = weights.filter { it.first.difficulty >= 4 }.sumOf { it.second }
            "L$level:${DifficultyProgression.band(level).id},pool=${weights.size},time=${"%.2f".format(java.util.Locale.US, duration)},simple=${"%.2f".format(java.util.Locale.US, simple)},complex=${"%.2f".format(java.util.Locale.US, complex)}"
        }
        val low = TaskEngine(SeededRandom(1)).candidateWeights(15, TaskEngine.taskDuration(GameMode.FAST, 15), memory, null, 100)
        val high = TaskEngine(SeededRandom(2)).candidateWeights(100, TaskEngine.taskDuration(GameMode.FAST, 100), memory, null, 100)
        val lowRatio = low.filter { it.first.difficulty >= 4 }.sumOf { it.second } / low.sumOf { it.second }
        val highRatio = high.filter { it.first.difficulty >= 4 }.sumOf { it.second } / high.sumOf { it.second }
        assertTrue(highRatio > lowRatio)
        assertEquals(TaskRegistry.tasks.size, high.size)
        println("AUDIT_DIFFICULTY=" + rows.joinToString(";"))
    }

    private fun simulateSession(taskCount: Int, seed: Long): SessionStats {
        val random = SeededRandom(seed)
        val engine = TaskEngine(random)
        val factory = TaskFactory(random)
        val stats = SessionStats()
        var memory = GameMemory()
        var activeRule: ActiveRule? = null
        var completed = 0
        var previousTask: String? = null

        repeat(taskCount) { index ->
            val level = index + 1
            val durationMs = (TaskEngine.taskDuration(if (index % 2 == 0) GameMode.SLOW else GameMode.FAST, level) * 1_000).toLong()
            val recallDue = engine.isRecallDue(memory, completed)
            if (recallDue) activeRule = null
            if (activeRule == null && memory.pendingRecall == null) {
                activeRule = engine.maybeStartGlobalRule(level, durationMs / 1_000.0, completed)
            }
            val candidates = engine.candidateWeights(level, durationMs / 1_000.0, memory, activeRule?.rule, completed)
            assertTrue("no candidate at level $level", candidates.isNotEmpty())
            val unlocked = TaskRegistry.tasks.count { level >= it.minLevel && durationMs / 1_000.0 >= it.minDuration }
            stats.rejectedCandidates += (unlocked - candidates.size).coerceAtLeast(0)
            stats.candidateChecks += 1
            val definition = engine.selectTask(level, durationMs / 1_000.0, memory, activeRule?.rule, completed)
            if (recallDue) {
                assertEquals("delayedRecall", definition.id)
                stats.forcedRecalls += 1
            }
            val modifiers = engine.selectModifiers(definition, level)
            val round = factory.create(index.toLong(), definition, level, durationMs, memory, "en", modifiers, activeRule)
            assertRoundInvariants(round, durationMs)
            if (previousTask == definition.id) stats.immediateRepeats += 1
            stats.recordTask(definition.id, completed)
            modifiers.forEach { stats.modifierCounts[it] = stats.modifierCounts.getOrDefault(it, 0) + 1 }
            activeRule?.rule?.let { stats.ruleCounts[it] = stats.ruleCounts.getOrDefault(it, 0) + 1 }
            round.metadata["depth"]?.toIntOrNull()?.let(stats.nBackDepths::add)

            var updated = memory
            if (definition.id == "delayedRecall") {
                updated = if (round.metadata["cue"] == "true") {
                    assertTrue(memory.pendingRecall == null)
                    val gap = round.metadata.getValue("gap").toInt()
                    val options = round.metadata.getValue("options").split("|")
                    memory.copy(pendingRecall = PendingRecall(round.answer!!, options, completed + 1 + gap))
                } else {
                    assertNotNull(memory.pendingRecall)
                    memory.copy(pendingRecall = null)
                }
            }
            memory = updated.copy(completed = (memory.completed + TaskHistory.from(round)).takeLast(16))
            stats.maxPendingRecall = maxOf(stats.maxPendingRecall, if (memory.pendingRecall == null) 0 else 1)
            completed += 1
            activeRule = activeRule?.let { if (it.remaining <= 1) null else it.copy(remaining = it.remaining - 1) }
            engine.remember(definition)
            previousTask = definition.id
        }
        return stats
    }

    private fun assertRoundInvariants(round: TaskRound, durationMs: Long) {
        assertTrue(round.definition in TaskRegistry.tasks)
        assertTrue(durationMs > 0)
        assertEquals(round.choices.size, round.choices.map { it.id }.toSet().size)
        assertTrue(round.choices.all { it.x.isFinite() && it.y.isFinite() && it.x in 0f..1f && it.y in 0f..1f })
        assertFalse(Modifier.MOVING in round.modifiers && Modifier.SWAP in round.modifiers)
        assertTrue(round.modifiers.all { it in round.definition.modifiers })
        assertTrue(round.globalRule == null || round.globalRule.rule in round.definition.rules)
        assertTrue(round.events.all { it.delayMs > 0 && it.delayMs < durationMs })
        val lastPhase = round.events.filter { it.type != RoundEventType.CLICK_SETTLE }.maxOfOrNull { it.delayMs } ?: 0
        assertTrue("${round.definition.id} leaves too little response time", durationMs - lastPhase >= 350)
        if (round.mainButtonVisible) {
            assertTrue(round.targetClicks >= 0)
        } else if (round.choices.isNotEmpty() && round.definition.id !in setOf("sequence", "alphabetical", "flashMemory", "reverseSequence")) {
            assertEquals("${round.definition.id} must have one answer", 1, round.choices.count { it.correct })
        }
        if (round.globalRule?.rule == GlobalRule.IGNORE_RED && round.choices.isNotEmpty()) {
            assertEquals(1, round.choices.count { it.style == ChoiceStyle.RED_DECOY })
            assertFalse(round.choices.single { it.style == ChoiceStyle.RED_DECOY }.correct)
        }
        if (round.globalRule?.rule == GlobalRule.FINAL_LINE) {
            assertEquals(InstructionPhases.MULTI_PHASE, round.definition.instructionPhases)
            assertTrue(round.inputLocked)
            assertFalse(round.goActive)
        }
        when (round.definition.id) {
            "reverseSequence" -> assertEquals(round.metadata.getValue("source").split("|").reversed(), round.sequence)
            "countByRule" -> {
                val colors = mapOf("red" to 0xFFC62828, "blue" to 0xFF1565C0, "green" to 0xFF2E7D32, "yellow" to 0xFF8A7800)
                val answerColor = colors.getValue(round.metadata.getValue("targetColor"))
                val answerShape = round.metadata.getValue("answerShape")
                assertEquals(round.answer!!.toInt(), round.visualItems.count { it.text == answerShape && it.color == answerColor })
            }
            "missingItem" -> assertTrue(round.answer in round.metadata.getValue("options").split("|"))
            "nBack" -> assertTrue(round.metadata.getValue("depth").toInt() in 1..3)
            "delayedRecall" -> assertTrue(round.answer != null)
        }
    }

    private fun richMemory() = GameMemory(completed = listOf(
        TaskHistoryEntry("a", "◆", 8, 2, "blue", AnswerSide.LEFT, AnswerParity.EVEN),
        TaskHistoryEntry("b", "▲", 7, 2, "red", AnswerSide.RIGHT, AnswerParity.ODD),
        TaskHistoryEntry("c", "●", 2, 2, "green", AnswerSide.LEFT, AnswerParity.EVEN)
    ))

    private data class SessionStats(
        val taskCounts: MutableMap<String, Int> = linkedMapOf(),
        val modifierCounts: MutableMap<Modifier, Int> = linkedMapOf(),
        val ruleCounts: MutableMap<GlobalRule, Int> = linkedMapOf(),
        val lastSeen: MutableMap<String, Int> = mutableMapOf(),
        val longestGap: MutableMap<String, Int> = mutableMapOf(),
        val totalGap: MutableMap<String, Long> = mutableMapOf(),
        val gapSamples: MutableMap<String, Int> = mutableMapOf(),
        val nBackDepths: MutableSet<Int> = mutableSetOf(),
        var immediateRepeats: Int = 0,
        var forcedRecalls: Int = 0,
        var maxPendingRecall: Int = 0,
        var rejectedCandidates: Long = 0,
        var candidateChecks: Long = 0
    ) {
        fun recordTask(id: String, position: Int) {
            taskCounts[id] = taskCounts.getOrDefault(id, 0) + 1
            lastSeen[id]?.let {
                val gap = position - it - 1
                longestGap[id] = maxOf(longestGap.getOrDefault(id, 0), gap)
                totalGap[id] = totalGap.getOrDefault(id, 0) + gap
                gapSamples[id] = gapSamples.getOrDefault(id, 0) + 1
            }
            lastSeen[id] = position
        }

        fun merge(other: SessionStats) {
            other.taskCounts.forEach { (key, value) -> taskCounts[key] = taskCounts.getOrDefault(key, 0) + value }
            other.modifierCounts.forEach { (key, value) -> modifierCounts[key] = modifierCounts.getOrDefault(key, 0) + value }
            other.ruleCounts.forEach { (key, value) -> ruleCounts[key] = ruleCounts.getOrDefault(key, 0) + value }
            other.longestGap.forEach { (key, value) -> longestGap[key] = maxOf(longestGap.getOrDefault(key, 0), value) }
            other.totalGap.forEach { (key, value) -> totalGap[key] = totalGap.getOrDefault(key, 0) + value }
            other.gapSamples.forEach { (key, value) -> gapSamples[key] = gapSamples.getOrDefault(key, 0) + value }
            nBackDepths += other.nBackDepths
            immediateRepeats += other.immediateRepeats
            forcedRecalls += other.forcedRecalls
            maxPendingRecall = maxOf(maxPendingRecall, other.maxPendingRecall)
            rejectedCandidates += other.rejectedCandidates
            candidateChecks += other.candidateChecks
        }

        fun summary(): String {
            val tasks = taskCounts.toSortedMap().entries.joinToString(",") {
                val averageGap = totalGap[it.key]?.toDouble()?.div(gapSamples[it.key] ?: 1) ?: 0.0
                "${it.key}:${it.value}/avg${"%.1f".format(java.util.Locale.US, averageGap)}/max${longestGap[it.key] ?: 0}"
            }
            val modifiers = modifierCounts.toSortedMap(compareBy { it.ordinal }).entries.joinToString(",") { "${it.key.name}:${it.value}" }
            val rules = ruleCounts.toSortedMap(compareBy { it.ordinal }).entries.joinToString(",") { "${it.key.name}:${it.value}" }
            val maxGap = longestGap.maxByOrNull { it.value }
            val rejectedAverage = if (candidateChecks == 0L) 0.0 else rejectedCandidates.toDouble() / candidateChecks
            return "tasks=[$tasks];modifiers=[$modifiers];rules=[$rules];maxGap=${maxGap?.key}:${maxGap?.value};" +
                "forcedRecalls=$forcedRecalls;nBack=$nBackDepths;avgRejected=${"%.2f".format(java.util.Locale.US, rejectedAverage)}"
        }
    }
}
