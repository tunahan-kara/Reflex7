package com.tunahankara.reflex7.engine

import org.junit.Assert.*
import org.junit.Test

class TaskEngineTest {
    private fun numericMemory(value: Int = 2) = GameMemory(
        completed = listOf(TaskHistoryEntry(taskId = "numberExtremum", numericAnswer = value, parity = if (value % 2 == 0) AnswerParity.EVEN else AnswerParity.ODD))
    )

    @Test fun selectionNeverImmediatelyRepeats() {
        val engine = TaskEngine(SeededRandom(7))
        var previous: TaskDefinition? = null
        repeat(200) {
            val selected = engine.selectTask(50, 4.0, numericMemory(), null)
            assertNotEquals(previous?.id, selected.id)
            engine.remember(selected)
            previous = selected
        }
    }

    @Test fun recentTaskAndCategoryReceivePenalties() {
        val engine = TaskEngine(SeededRandom(1))
        val target = TaskRegistry.byId("numberExtremum")
        val before = engine.candidateWeights(30, 4.0, numericMemory(1)).toMap()
        engine.remember(target)
        engine.remember(TaskRegistry.byId("parity"))
        val after = engine.candidateWeights(30, 4.0, numericMemory(1)).toMap()
        assertTrue(after.getValue(target) < before.getValue(target))
        assertTrue(after.getValue(TaskRegistry.byId("numberExtremum")) < before.getValue(TaskRegistry.byId("numberExtremum")))
    }

    @Test fun scoringMatchesWebFormulaAndComboCap() {
        assertEquals(285, TaskEngine.score(10, 2, 1, true, 2_000, 4_000, 1))
        assertEquals(428, TaskEngine.score(10, 2, 1, true, 2_000, 4_000, 11))
        assertEquals(428, TaskEngine.score(10, 2, 1, true, 2_000, 4_000, 99))
    }

    @Test fun durationScalesAndClamps() {
        assertEquals(7.0, TaskEngine.taskDuration(GameMode.SLOW, 1), 0.0)
        assertEquals(6.65, TaskEngine.taskDuration(GameMode.SLOW, 5), 0.0001)
        assertEquals(2.8, TaskEngine.taskDuration(GameMode.SLOW, 999), 0.0001)
        assertEquals(1.6, TaskEngine.taskDuration(GameMode.FAST, 999), 0.0001)
    }

    @Test fun seededSelectionIsDeterministic() {
        fun run(seed: Long): List<String> {
            val engine = TaskEngine(SeededRandom(seed))
            return buildList { repeat(30) { engine.selectTask(35, 4.0, numericMemory(), null).also { add(it.id); engine.remember(it) } } }
        }
        assertEquals(run(42), run(42))
    }

    @Test fun modifiersRespectDefinitionAndMutualExclusion() {
        val engine = TaskEngine(SeededRandom(91))
        repeat(200) {
            val definition = TaskRegistry.byId("lastSecondInstruction")
            val modifiers = engine.selectModifiers(definition, 50)
            assertTrue(modifiers.all { it in definition.modifiers })
            assertFalse(Modifier.MOVING in modifiers && Modifier.SWAP in modifiers)
        }
    }

    @Test fun activeRuleOnlySelectsCompatibleTasks() {
        val engine = TaskEngine(SeededRandom(12))
        repeat(100) {
            val selected = engine.selectTask(40, 4.0, numericMemory(1), GlobalRule.INVERT)
            assertTrue(GlobalRule.INVERT in selected.rules)
        }
    }

    @Test fun nBackDepthScalesOnlyWhenHistoryExists() {
        val engine = TaskEngine(SeededRandom(5))
        val one = GameMemory(completed = listOf(TaskHistoryEntry(item = "◆")))
        val three = GameMemory(completed = listOf(TaskHistoryEntry(item = "◆"), TaskHistoryEntry(item = "▲"), TaskHistoryEntry(item = "●")))
        assertEquals(0, engine.nBackDepth(50, GameMemory()))
        assertEquals(1, engine.nBackDepth(50, one))
        assertEquals(1, engine.nBackDepth(25, three))
        assertEquals(2, engine.nBackDepth(32, three))
        assertEquals(3, engine.nBackDepth(40, three))
    }

    @Test fun thousandsOfSelectionsRespectProgressionAndAvoidStarvation() {
        val memory = GameMemory(
            completed = listOf(
                TaskHistoryEntry("a", "7", 7, colorAnswer = "red", side = AnswerSide.LEFT, parity = AnswerParity.ODD),
                TaskHistoryEntry("b", "3", 3, colorAnswer = "blue", side = AnswerSide.RIGHT, parity = AnswerParity.ODD),
                TaskHistoryEntry("c", "◆", 4, colorAnswer = "green", side = AnswerSide.LEFT, parity = AnswerParity.EVEN)
            )
        )
        for (level in listOf(5, 20, 50)) {
            val engine = TaskEngine(SeededRandom(90_000L + level))
            val seen = mutableSetOf<String>()
            var previous: String? = null
            repeat(4_000) {
                val selected = engine.selectTask(level, 2.0, memory, null, 100)
                assertTrue(selected.minLevel <= level)
                assertNotEquals(previous, selected.id)
                seen += selected.id
                engine.remember(selected)
                previous = selected.id
            }
            if (level == 50) assertEquals(TaskRegistry.tasks.map { it.id }.toSet(), seen)
        }
    }
}
