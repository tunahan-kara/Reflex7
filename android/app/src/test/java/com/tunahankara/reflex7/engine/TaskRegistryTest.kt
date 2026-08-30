package com.tunahankara.reflex7.engine

import org.junit.Assert.*
import org.junit.Test

class TaskRegistryTest {
    @Test fun registryContainsExactlyThirtyFourUniqueTasksAndNoRhythm() {
        assertEquals(34, TaskRegistry.tasks.size)
        assertEquals(34, TaskRegistry.tasks.map { it.id }.toSet().size)
        assertFalse(TaskRegistry.tasks.any { it.id.equals("rhythm", true) })
    }

    @Test fun newTasksUseTheSpecifiedUnlockThresholds() {
        val thresholds = mapOf(
            "directionConflict" to 11, "mentalMath" to 12, "missingItem" to 13,
            "reverseSequence" to 16, "countByRule" to 17, "oppositePosition" to 18,
            "changingAnswer" to 19, "doubleCondition" to 21, "ruleSwitch" to 23,
            "nBack" to 25, "previousRuleRecall" to 26, "delayedRecall" to 26
        )
        assertEquals(thresholds, TaskRegistry.tasks.filter { it.id in thresholds }.associate { it.id to it.minLevel })
    }

    @Test fun newTaskCompatibilityIsExplicit() {
        val modifiers = mapOf(
            "directionConflict" to setOf(Modifier.DELAYED, Modifier.SHRINKING, Modifier.DECOY, Modifier.SWAP),
            "mentalMath" to Modifier.entries.toSet(),
            "missingItem" to setOf(Modifier.MIRRORED, Modifier.SHRINKING, Modifier.DECOY),
            "reverseSequence" to setOf(Modifier.MIRRORED, Modifier.SHRINKING, Modifier.DECOY),
            "countByRule" to Modifier.entries.toSet(),
            "oppositePosition" to setOf(Modifier.SHRINKING, Modifier.DECOY),
            "changingAnswer" to setOf(Modifier.MIRRORED, Modifier.SHRINKING, Modifier.DECOY),
            "doubleCondition" to Modifier.entries.toSet(),
            "ruleSwitch" to setOf(Modifier.MIRRORED, Modifier.SHRINKING, Modifier.DECOY),
            "nBack" to setOf(Modifier.MIRRORED, Modifier.SHRINKING, Modifier.DECOY, Modifier.SWAP),
            "previousRuleRecall" to setOf(Modifier.MIRRORED, Modifier.SHRINKING, Modifier.DECOY, Modifier.SWAP),
            "delayedRecall" to setOf(Modifier.MIRRORED, Modifier.SHRINKING, Modifier.DECOY)
        )
        val rules = mapOf(
            "directionConflict" to setOf(GlobalRule.INVERT, GlobalRule.ODD_WAIT),
            "mentalMath" to setOf(GlobalRule.INVERT, GlobalRule.IGNORE_RED, GlobalRule.ODD_WAIT),
            "missingItem" to setOf(GlobalRule.FINAL_LINE),
            "reverseSequence" to setOf(GlobalRule.FINAL_LINE),
            "countByRule" to setOf(GlobalRule.IGNORE_RED, GlobalRule.ODD_WAIT),
            "oppositePosition" to setOf(GlobalRule.FINAL_LINE),
            "changingAnswer" to setOf(GlobalRule.FINAL_LINE),
            "doubleCondition" to setOf(GlobalRule.ODD_WAIT),
            "ruleSwitch" to setOf(GlobalRule.FINAL_LINE),
            "nBack" to setOf(GlobalRule.INVERT, GlobalRule.IGNORE_RED, GlobalRule.ODD_WAIT),
            "previousRuleRecall" to setOf(GlobalRule.ODD_WAIT),
            "delayedRecall" to setOf(GlobalRule.EMOJI_LITERAL)
        )
        modifiers.forEach { (id, expected) -> assertEquals("$id modifiers", expected, TaskRegistry.byId(id).modifiers) }
        rules.forEach { (id, expected) -> assertEquals("$id rules", expected, TaskRegistry.byId(id).rules) }
    }

    @Test fun allDefinitionsHaveValidEligibilityAndCompatibility() {
        TaskRegistry.tasks.forEach { task ->
            assertTrue(task.minLevel >= 1)
            assertTrue(task.minDuration > 0)
            assertTrue(task.weight > 0)
            assertTrue(task.modifiers.all { it in Modifier.entries })
            assertTrue(task.rules.all { it in GlobalRule.entries })
        }
    }

    @Test fun previousMemoryRequiresImmediateNumericAnswer() {
        val engine = TaskEngine(SeededRandom(4))
        val without = engine.candidateWeights(30, 4.0, GameMemory()).map { it.first.id }
        val with = engine.candidateWeights(30, 4.0, GameMemory(completed = listOf(TaskHistoryEntry(taskId = "parity", numericAnswer = 2)))).map { it.first.id }
        assertFalse("previousMemory" in without)
        assertTrue("previousMemory" in with)
    }

    @Test fun minimumDurationIsEnforced() {
        val ids = TaskEngine(SeededRandom(3)).candidateWeights(50, 1.25, GameMemory(completed = listOf(TaskHistoryEntry(numericAnswer = 1)))).map { it.first.id }
        assertFalse("flashMemory" in ids)
        assertFalse("lastSecondInstruction" in ids)
        assertTrue("standard" in ids)
    }

    @Test fun memoryPrerequisitesPreventImpossibleRecallTasks() {
        val engine = TaskEngine(SeededRandom(13))
        val empty = engine.candidateWeights(50, 4.0, GameMemory(), completedTasks = 20).map { it.first.id }
        assertFalse("nBack" in empty)
        assertFalse("previousRuleRecall" in empty)
        val ready = GameMemory(completed = listOf(TaskHistoryEntry("task", "◆", colorAnswer = "red", side = AnswerSide.LEFT, parity = AnswerParity.EVEN)))
        val candidates = engine.candidateWeights(50, 4.0, ready, completedTasks = 20).map { it.first.id }
        assertTrue("nBack" in candidates)
        assertTrue("previousRuleRecall" in candidates)
    }

    @Test fun dueDelayedRecallIsForcedAndCannotStarve() {
        val memory = GameMemory(pendingRecall = PendingRecall("◆", listOf("◆", "▲", "●", "■"), 12))
        val candidates = TaskEngine(SeededRandom(8)).candidateWeights(50, 4.0, memory, completedTasks = 12)
        assertEquals(listOf("delayedRecall"), candidates.map { it.first.id })
    }

    @Test fun staleNumericMetadataIsNeverUsedAsPrevious() {
        val engine = TaskEngine(SeededRandom(101))
        val oddOneOutLast = GameMemory(completed = listOf(
            TaskHistoryEntry(taskId = "numberExtremum", numericAnswer = 9),
            TaskHistoryEntry(taskId = "oddOneOut")
        ))
        val noCountLast = GameMemory(completed = listOf(
            TaskHistoryEntry(taskId = "standard", numericAnswer = 3, clickCount = 3),
            TaskHistoryEntry(taskId = "stroop", colorAnswer = "blue")
        ))
        assertFalse(engine.candidateWeights(50, 4.0, oddOneOutLast).any { it.first.id == "previousMemory" })
        assertFalse(engine.candidateWeights(50, 4.0, noCountLast).any { it.first.id == "previousMemory" })
        assertTrue(engine.candidateWeights(50, 4.0, GameMemory(completed = listOf(
            TaskHistoryEntry(taskId = "numberExtremum", numericAnswer = 11)
        ))).any { it.first.id == "previousMemory" })
    }

    @Test fun finalLineOnlySelectsRealMultiPhaseTasks() {
        val engine = TaskEngine(SeededRandom(202))
        val memory = GameMemory(completed = listOf(TaskHistoryEntry(taskId = "numberExtremum", numericAnswer = 4)))
        repeat(10_000) {
            val selected = engine.selectTask(50, 4.0, memory, GlobalRule.FINAL_LINE)
            assertEquals(InstructionPhases.MULTI_PHASE, selected.instructionPhases)
            assertTrue(GlobalRule.FINAL_LINE in selected.rules)
            engine.remember(selected)
        }
        assertFalse(GlobalRule.FINAL_LINE in TaskRegistry.byId("oddOneOut").rules)
        assertTrue(GlobalRule.FINAL_LINE in TaskRegistry.byId("lastSecondInstruction").rules)
    }
}
