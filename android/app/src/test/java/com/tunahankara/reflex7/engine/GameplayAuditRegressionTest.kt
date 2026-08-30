package com.tunahankara.reflex7.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GameplayAuditRegressionTest {
    private val memory = GameMemory(completed = listOf(
        TaskHistoryEntry("a", "◆", 8, 2, "blue", AnswerSide.LEFT, AnswerParity.EVEN),
        TaskHistoryEntry("b", "▲", 7, 2, "red", AnswerSide.RIGHT, AnswerParity.ODD),
        TaskHistoryEntry("c", "●", 2, 2, "green", AnswerSide.LEFT, AnswerParity.EVEN)
    ))

    @Test fun everyStroopPromptHasExactlyOneSemanticallyCorrectChoice() {
        repeat(2_000) { seed ->
            val round = create("stroop", seed.toLong())
            assertEquals("stroop seed $seed", 1, round.choices.count { it.correct })
        }
    }

    @Test fun ruleSwitchParityPromptHasOnlyOneMatchingChoice() {
        repeat(2_000) { seed ->
            val round = create("ruleSwitch", seed.toLong())
            when (round.metadata["finalRule"]) {
                "even" -> assertEquals("even seed $seed", 1, round.choices.count { it.id.toInt() % 2 == 0 })
                "odd" -> assertEquals("odd seed $seed", 1, round.choices.count { kotlin.math.abs(it.id.toInt() % 2) == 1 })
            }
        }
    }

    @Test fun ignoreRedCannotAlterColorSemanticTasks() {
        assertFalse(GlobalRule.IGNORE_RED in TaskRegistry.byId("stroop").rules)
        assertFalse(GlobalRule.IGNORE_RED in TaskRegistry.byId("previousRuleRecall").rules)
    }

    @Test fun dueRecallCannotBeBlockedByAnUnrelatedActiveRule() {
        val due = memory.copy(pendingRecall = PendingRecall("◆", listOf("◆", "▲", "●", "■"), 10))
        val candidates = TaskEngine(SeededRandom(4)).candidateWeights(
            level = 50, durationSeconds = 2.0, memory = due,
            activeRule = GlobalRule.INVERT, completedTasks = 10
        )
        assertEquals(listOf("delayedRecall"), candidates.map { it.first.id })
    }

    @Test fun allDeclaredRuleAndModifierPairsRemainStructurallyValid() {
        TaskRegistry.tasks.forEachIndexed { index, definition ->
            definition.rules.forEach { rule ->
                val round = TaskFactory(SeededRandom(index * 101L + rule.ordinal)).create(
                    index.toLong(), definition, 50, 2_800, memory, "en", emptyList(), ActiveRule(rule, 2)
                )
                assertEquals(rule, round.globalRule?.rule)
                assertEquals(definition.id, round.definition.id)
            }
            definition.modifiers.forEach { modifier ->
                val round = TaskFactory(SeededRandom(index * 211L + modifier.ordinal)).create(
                    index.toLong(), definition, 50, 2_800, memory, "en", listOf(modifier), null
                )
                assertTrue(modifier in round.modifiers)
            }
        }
    }

    private fun create(id: String, seed: Long): TaskRound = TaskFactory(SeededRandom(seed)).create(
        seed, TaskRegistry.byId(id), 50, 2_800, memory, "en", emptyList(), null
    )
}
