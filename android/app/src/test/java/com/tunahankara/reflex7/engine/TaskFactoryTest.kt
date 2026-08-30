package com.tunahankara.reflex7.engine

import org.junit.Assert.*
import org.junit.Test

class TaskFactoryTest {
    private fun richMemory(pending: PendingRecall? = null) = GameMemory(
        completed = listOf(
            TaskHistoryEntry("a", "◆", 8, 2, "blue", AnswerSide.LEFT, AnswerParity.EVEN),
            TaskHistoryEntry("b", "▲", 7, 2, "red", AnswerSide.RIGHT, AnswerParity.ODD),
            TaskHistoryEntry("c", "●", 2, 2, "green", AnswerSide.LEFT, AnswerParity.EVEN)
        ),
        pendingRecall = pending
    )

    @Test fun everyTaskCanBeCreatedAtMinimumDuration() {
        TaskRegistry.tasks.forEachIndexed { index, definition ->
            val factory = TaskFactory(SeededRandom(index.toLong()))
            val round = factory.create(index.toLong(), definition, 50, (definition.minDuration * 1000).toLong(), richMemory(), "en", emptyList(), null)
            assertEquals(definition.id, round.definition.id)
            assertNotNull(round.instruction)
            assertTrue(round.events.all { it.delayMs < definition.minDuration * 1000 })
        }
    }

    @Test fun delayedSignalsLeaveAResponseWindow() {
        val timed = listOf(
            "colorShift", "wait", "lastSecondInstruction", "patienceCountdown", "ruleSwitch",
            "oppositePosition", "missingItem", "reverseSequence", "changingAnswer", "delayedRecall"
        )
        timed.forEachIndexed { index, id ->
            val duration = 1_600L
            repeat(100) { seed ->
                val round = TaskFactory(SeededRandom((index * 1000 + seed).toLong())).create(seed.toLong(), TaskRegistry.byId(id), 50, duration, richMemory(), "en", emptyList(), null)
                assertTrue("$id signal was too late", round.events.maxOf { it.delayMs } <= duration - 350)
            }
        }
    }

    @Test fun modeRecordsAreIndependentValues() {
        val preferences = PlayerPreferences(slowRecord = ModeRecord(9, 900), fastRecord = ModeRecord(3, 250))
        assertNotEquals(preferences.slowRecord, preferences.fastRecord)
        assertEquals(900, preferences.slowRecord.bestScore)
        assertEquals(250, preferences.fastRecord.bestScore)
    }

    @Test fun ruleTrapBuildsTheWebPackageShapes() {
        repeat(100) { seed ->
            val steps = TaskFactory(SeededRandom(seed.toLong())).newPackage()
            assertTrue(steps.size in 2..4)
            assertTrue(steps.all { it.target != null || it.chainFromPrevious })
        }
    }

    @Test fun blueLegacyRuleOnlyUsesDesignatedTargets() {
        val factory = TaskFactory(SeededRandom(1))
        val bluePackage = (0 until 200).asSequence()
            .map { TaskFactory(SeededRandom(it.toLong())).newPackage() }
            .first { steps -> steps.any { it.setLegacyRule == LegacyRule.BLUE_TARGET } }
        val blueSteps = bluePackage.filter { it.buttonColor == 0xFF2196F3 }
        assertTrue(blueSteps.isNotEmpty())
        assertTrue(blueSteps.all { it.designatedBlueTarget && it.target == 1 })
        assertTrue(bluePackage.filterNot { it.designatedBlueTarget }.none { it.buttonColor == 0xFF2196F3 })
        val decorativeBlueText = bluePackage.first { it.instruction == UiText.Resource(com.tunahankara.reflex7.R.string.task_blue_word) }
        val decorativeRound = factory.createPackageRound(1, TaskRegistry.byId("package"), decorativeBlueText, 1, LegacyRule.BLUE_TARGET)
        assertEquals(0, decorativeRound.targetClicks)
        assertNotEquals(0xFF2196F3, decorativeRound.mainButtonColor)
        val explicitDoNotPress = bluePackage.first { it.instruction == UiText.Resource(com.tunahankara.reflex7.R.string.task_do_not_press) }
        assertEquals(0, explicitDoNotPress.target)
    }

    @Test fun finalInstructionTasksLockInputAndReserveReactionTime() {
        val compatible = TaskRegistry.tasks.filter { GlobalRule.FINAL_LINE in it.rules }
        assertTrue(compatible.isNotEmpty())
        compatible.forEachIndexed { index, definition ->
            repeat(100) { seed ->
                val round = TaskFactory(SeededRandom(index * 1_000L + seed)).create(
                    seed.toLong(), definition, 50, 1_800, richMemory(), "en", emptyList(), ActiveRule(GlobalRule.FINAL_LINE, 2)
                )
                assertEquals(InstructionPhases.MULTI_PHASE, definition.instructionPhases)
                assertTrue(definition.id, round.inputLocked)
                assertFalse(definition.id, round.goActive)
                assertTrue(definition.id, round.events.maxOf { it.delayMs } <= 1_450)
            }
        }
        val last = TaskFactory(SeededRandom(9)).create(
            9, TaskRegistry.byId("lastSecondInstruction"), 50, 1_800, richMemory(), "en", emptyList(), ActiveRule(GlobalRule.FINAL_LINE, 2)
        )
        assertNotEquals(last.secondaryAnswer, last.answer)
        assertEquals(last.answer, last.choices.single { it.correct }.id)
    }

    @Test fun everyNewChoiceTaskHasExactlyOneCorrectAnswer() {
        val ids = listOf(
            "directionConflict", "mentalMath", "countByRule", "oppositePosition", "changingAnswer",
            "doubleCondition", "ruleSwitch", "nBack", "previousRuleRecall"
        )
        ids.forEachIndexed { index, id ->
            repeat(100) { seed ->
                val round = create(id, index * 1000L + seed)
                assertEquals("$id seed $seed", 1, round.choices.count { it.correct })
                if (id !in setOf("mentalMath", "nBack")) assertEquals(round.answer, round.choices.single { it.correct }.id)
            }
        }
    }

    @Test fun stagedMemoryTasksExposeFairLockedRevealPaths() {
        mapOf(
            "missingItem" to RoundEventType.MISSING_REVEAL,
            "reverseSequence" to RoundEventType.REVERSE_REVEAL,
            "oppositePosition" to RoundEventType.OPPOSITE_HIDE,
            "changingAnswer" to RoundEventType.CHANGE_POSITIONS,
            "ruleSwitch" to RoundEventType.RULE_SWITCH
        ).forEach { (id, event) ->
            val round = create(id, id.hashCode().toLong())
            assertTrue(id, round.inputLocked)
            assertFalse(id, round.goActive)
            assertTrue(id, round.events.any { it.type == event })
            if (id == "reverseSequence") assertTrue(round.sequence.isNotEmpty()) else assertNotNull(id, round.answer)
        }
    }

    @Test fun reverseSequenceStoresTheExactReverseOrder() {
        repeat(100) { seed ->
            val round = create("reverseSequence", seed.toLong())
            val source = round.metadata.getValue("source").split("|")
            assertEquals(source.reversed(), round.sequence)
            assertEquals(source.size, source.toSet().size)
        }
    }

    @Test fun mentalMathTruthAndInvertAreConsistent() {
        repeat(100) { seed ->
            val normal = create("mentalMath", seed.toLong())
            val inverted = create("mentalMath", seed.toLong(), ActiveRule(GlobalRule.INVERT, 2))
            val statementTrue = normal.metadata.getValue("actual") == normal.metadata.getValue("claimed")
            assertEquals(statementTrue, normal.choices.single { it.id == "yes" }.correct)
            assertEquals(!statementTrue, inverted.choices.single { it.id == "yes" }.correct)
        }
    }

    @Test fun nBackUsesARealDepthAndReference() {
        repeat(100) { seed ->
            val round = create("nBack", seed.toLong())
            val depth = round.metadata.getValue("depth").toInt()
            val reference = richMemory().completed[richMemory().completed.size - depth].item
            assertEquals(round.answer == reference, round.metadata.getValue("match").toBoolean())
        }
    }

    @Test fun countByRuleMatchesRenderedAttributes() {
        repeat(100) { seed ->
            val round = create("countByRule", seed.toLong())
            val colorId = round.metadata.getValue("targetColor")
            val argb = mapOf("red" to 0xFFC62828, "blue" to 0xFF1565C0, "green" to 0xFF2E7D32, "yellow" to 0xFF8A7800).getValue(colorId)
            val shape = round.metadata.getValue("answerShape")
            assertEquals(round.answer!!.toInt(), round.visualItems.count { it.text == shape && it.color == argb })
        }
    }

    @Test fun delayedRecallHasCueAndRetrievalPhases() {
        val cue = create("delayedRecall", 4)
        assertEquals("true", cue.metadata["cue"])
        assertTrue(cue.inputLocked)
        assertEquals(listOf(RoundEventType.DELAYED_RECALL_CUE), cue.events.map { it.type })
        val pending = PendingRecall(cue.answer!!, cue.metadata.getValue("options").split("|"), 9)
        val recall = create("delayedRecall", 5, memory = richMemory(pending))
        assertEquals("false", recall.metadata["cue"])
        assertEquals(1, recall.choices.count { it.correct })
        assertEquals(pending.item, recall.choices.single { it.correct }.id)
    }

    @Test fun previousColorRecallIncludesAllStoredColors() {
        val memory = GameMemory(completed = listOf(TaskHistoryEntry(item = "◆", colorAnswer = "purple")))
        val round = create("previousRuleRecall", 19, memory = memory)
        assertEquals(1, round.choices.count { it.correct })
        assertEquals("purple", round.choices.single { it.correct }.id)
    }

    @Test fun historyExtractionOnlyPublishesPropertiesTheTaskActuallyOwns() {
        val odd = create("oddOneOut", 41)
        val numeric = create("numberExtremum", 42)
        val clickCount = create("clickPattern", 43)
        assertNull(TaskHistory.from(odd).numericAnswer)
        assertNull(TaskHistory.from(odd).clickCount)
        assertEquals(numeric.answer!!.toInt(), TaskHistory.from(numeric).numericAnswer)
        assertEquals(clickCount.targetClicks, TaskHistory.from(clickCount).clickCount)
    }

    @Test fun generationIsDeterministicForEveryNewTask() {
        val ids = TaskRegistry.tasks.drop(22).map { it.id }
        ids.forEach { id -> assertEquals(create(id, 77), create(id, 77)) }
    }

    private fun create(
        id: String,
        seed: Long,
        rule: ActiveRule? = null,
        memory: GameMemory = richMemory()
    ): TaskRound = TaskFactory(SeededRandom(seed)).create(
        seed, TaskRegistry.byId(id), 50, 1_800, memory, "en", emptyList(), rule
    )
}
