package com.tunahankara.reflex7.engine

import androidx.annotation.StringRes

enum class GameMode(val seconds: Double, val storageId: String) {
    SLOW(7.0, "7"), FAST(4.0, "4")
}

enum class GameScreen { MENU, PLAYING, RESULTS }

enum class TaskCategory(@param:StringRes val labelRes: Int) {
    REACTION(com.tunahankara.reflex7.R.string.category_reaction),
    INHIBITION(com.tunahankara.reflex7.R.string.category_inhibition),
    MEMORY(com.tunahankara.reflex7.R.string.category_memory),
    VISUAL(com.tunahankara.reflex7.R.string.category_visual),
    ARITHMETIC(com.tunahankara.reflex7.R.string.category_arithmetic),
    SEQUENCE(com.tunahankara.reflex7.R.string.category_sequence),
    TIMING(com.tunahankara.reflex7.R.string.category_timing),
    LANGUAGE(com.tunahankara.reflex7.R.string.category_language),
    DECEPTION(com.tunahankara.reflex7.R.string.category_deception),
    PRECISION(com.tunahankara.reflex7.R.string.category_precision)
}

enum class Modifier(@param:StringRes val labelRes: Int, val minLevel: Int) {
    MIRRORED(com.tunahankara.reflex7.R.string.modifier_mirrored, 25),
    DELAYED(com.tunahankara.reflex7.R.string.modifier_delayed, 27),
    MOVING(com.tunahankara.reflex7.R.string.modifier_moving, 29),
    SHRINKING(com.tunahankara.reflex7.R.string.modifier_shrinking, 31),
    DECOY(com.tunahankara.reflex7.R.string.modifier_decoy, 34),
    SWAP(com.tunahankara.reflex7.R.string.modifier_swap, 37)
}

enum class GlobalRule(@param:StringRes val labelRes: Int, val minLevel: Int) {
    INVERT(com.tunahankara.reflex7.R.string.rule_invert, 13),
    IGNORE_RED(com.tunahankara.reflex7.R.string.rule_ignore_red, 15),
    FINAL_LINE(com.tunahankara.reflex7.R.string.rule_final_line, 17),
    ODD_WAIT(com.tunahankara.reflex7.R.string.rule_odd_wait, 19),
    EMOJI_LITERAL(com.tunahankara.reflex7.R.string.rule_emoji_literal, 21)
}

data class TaskDefinition(
    val id: String,
    val category: TaskCategory,
    val minLevel: Int,
    val weight: Double,
    val difficulty: Int,
    val minDuration: Double,
    @param:StringRes val nameRes: Int,
    val rules: Set<GlobalRule> = emptySet(),
    val modifiers: Set<Modifier> = emptySet(),
    val prerequisite: TaskPrerequisite = TaskPrerequisite.NONE,
    val instructionPhases: InstructionPhases = InstructionPhases.SINGLE
)

enum class TaskPrerequisite { NONE, PREVIOUS_NUMERIC_ANSWER, N_BACK_HISTORY, PREVIOUS_METADATA, DELAYED_RECALL }
enum class InstructionPhases { SINGLE, MULTI_PHASE }
enum class AnswerSide { LEFT, RIGHT }
enum class AnswerParity { ODD, EVEN }
enum class LegacyRule { BLUE_TARGET, MOUSE }

data class DifficultyBand(
    val id: String,
    val options: Int,
    val memory: Int,
    val modifierCount: Int,
    val globalChance: Double
)

object DifficultyProgression {
    fun band(level: Int): DifficultyBand = when {
        level <= 5 -> DifficultyBand("onboarding", 4, 3, 0, 0.0)
        level <= 10 -> DifficultyBand("basic", 5, 3, 0, 0.0)
        level <= 20 -> DifficultyBand("mixed", 6, 4, 0, 0.08)
        level <= 34 -> DifficultyBand("modified", 7, 4, 1, 0.12)
        level <= 59 -> DifficultyBand("advanced", 9, 5, 2, 0.17)
        else -> DifficultyBand("expert", 9, 6, 2, 0.22)
    }
}

class ResolutionGate {
    private var activeGeneration: Long? = null
    private var resolved = false

    fun begin(generation: Long) {
        activeGeneration = generation
        resolved = false
    }

    fun claim(generation: Long): Boolean {
        if (generation != activeGeneration || resolved) return false
        resolved = true
        return true
    }

    fun clear() {
        activeGeneration = null
        resolved = false
    }
}

sealed interface UiText {
    data class Resource(@param:StringRes val id: Int, val args: List<Any> = emptyList()) : UiText
    data class Literal(val value: String) : UiText
}

enum class ChoiceStyle { NORMAL, REAL, FAKE, MEMORY, RED_DECOY }

data class TaskChoice(
    val id: String,
    val label: UiText,
    val correct: Boolean,
    val color: Long? = null,
    val style: ChoiceStyle = ChoiceStyle.NORMAL,
    val enabled: Boolean = true,
    val selected: Boolean = false,
    val x: Float = 0.5f,
    val y: Float = 0.5f
)

enum class RoundEventType {
    COLOR_GO, WAIT_GO, LAST_FINAL, PATIENCE_TWO, PATIENCE_ONE, PATIENCE_HESITATE,
    PATIENCE_GO, FLASH_REVEAL, DELAYED_REVEAL, POSITION_HIDE, CLICK_SETTLE,
    MODIFIER_UNLOCK, RULE_UNLOCK, SWAP_CHOICES, RULE_SWITCH, OPPOSITE_HIDE,
    MISSING_REVEAL, REVERSE_REVEAL, CHANGE_POSITIONS, DELAYED_RECALL_CUE
}

data class RoundEvent(val delayMs: Long, val type: RoundEventType)

data class TaskVisualItem(val text: String, val color: Long? = null)

data class PackageStep(
    val instruction: UiText,
    val target: Int? = null,
    val chainFromPrevious: Boolean = false,
    val buttonColor: Long = 0xFF4CAF50,
    val setLegacyRule: LegacyRule? = null,
    val designatedBlueTarget: Boolean = false
)

data class TaskRound(
    val generation: Long,
    val definition: TaskDefinition,
    val instruction: UiText,
    val initialInstruction: UiText = instruction,
    val choices: List<TaskChoice> = emptyList(),
    val events: List<RoundEvent> = emptyList(),
    val mainButtonVisible: Boolean = true,
    val mainButtonColor: Long = 0xFF4CAF50,
    val inputLocked: Boolean = false,
    val goActive: Boolean = true,
    val targetClicks: Int = 1,
    val currentClicks: Int = 0,
    val requiredHoldMs: Long = 0,
    val progress: Int = 0,
    val sequence: List<String> = emptyList(),
    val answer: String? = null,
    val secondaryAnswer: String? = null,
    val statusText: UiText? = null,
    val modifiers: List<Modifier> = emptyList(),
    val globalRule: ActiveRule? = null,
    val failureOnLockedInput: Boolean = false,
    val movingTarget: Boolean = false,
    val visualItems: List<TaskVisualItem> = emptyList(),
    val metadata: Map<String, String> = emptyMap()
)

data class ActiveRule(val rule: GlobalRule, val remaining: Int)

data class TaskHistoryEntry(
    val taskId: String = "",
    val item: String? = null,
    val numericAnswer: Int? = null,
    val clickCount: Int? = null,
    val colorAnswer: String? = null,
    val side: AnswerSide? = null,
    val parity: AnswerParity? = null
)

object TaskHistory {
    private val numericAnswerTasks = setOf(
        "numberExtremum", "parity", "previousMemory", "countSymbols", "delayedInstruction",
        "countByRule", "changingAnswer", "ruleSwitch", "doubleCondition"
    )
    private val clickCountTasks = setOf("standard", "package", "clickPattern")

    fun from(round: TaskRound): TaskHistoryEntry {
        val correctIndex = round.choices.indexOfFirst { it.correct }
        val numericAnswer = round.answer?.toIntOrNull()?.takeIf { round.definition.id in numericAnswerTasks }
            ?: round.targetClicks.takeIf { round.definition.id in clickCountTasks }
        return TaskHistoryEntry(
            taskId = round.definition.id,
            item = round.metadata["historyItem"],
            numericAnswer = numericAnswer,
            clickCount = round.targetClicks.takeIf { round.definition.id in clickCountTasks },
            colorAnswer = round.metadata["answerColor"] ?: round.answer?.takeIf {
                round.definition.id == "lastSecondInstruction" ||
                    (round.definition.id == "previousRuleRecall" && round.metadata["recallKind"] == "color")
            },
            side = if (round.choices.size == 2 && correctIndex >= 0) {
                if (correctIndex == 0) AnswerSide.LEFT else AnswerSide.RIGHT
            } else null,
            parity = numericAnswer?.let {
                if (kotlin.math.abs(it % 2) == 0) AnswerParity.EVEN else AnswerParity.ODD
            }
        )
    }
}

data class PendingRecall(val item: String, val options: List<String>, val dueAt: Int)

data class GameMemory(
    val completed: List<TaskHistoryEntry> = emptyList(),
    val pendingRecall: PendingRecall? = null
) {
    val lastCompleted: TaskHistoryEntry? get() = completed.lastOrNull()
}

data class ModeRecord(val bestLevel: Int = 0, val bestScore: Long = 0)

data class PlayerPreferences(
    val nickname: String = "",
    val soundEnabled: Boolean = true,
    val language: String = "tr",
    val onboardingSeen: Boolean = false,
    val discoveries: Set<String> = emptySet(),
    val slowRecord: ModeRecord = ModeRecord(),
    val fastRecord: ModeRecord = ModeRecord()
)

data class ResultState(
    val nickname: String,
    val level: Int,
    val score: Long,
    val bestLevel: Int,
    val bestScore: Long,
    val durationSeconds: Long,
    val highestCombo: Int,
    val failure: UiText,
    val taskNameRes: Int,
    val categoryRes: Int,
    val context: List<Int>,
    val newBest: Boolean,
    val sarcasmRes: Int
)

data class GameUiState(
    val screen: GameScreen = GameScreen.MENU,
    val preferences: PlayerPreferences = PlayerPreferences(),
    val mode: GameMode = GameMode.SLOW,
    val nickname: String = "",
    val level: Int = 1,
    val score: Long = 0,
    val combo: Int = 0,
    val highestCombo: Int = 0,
    val remainingMs: Long = 0,
    val durationMs: Long = 0,
    val round: TaskRound? = null,
    val paused: Boolean = false,
    val inputLocked: Boolean = false,
    val announcement: UiText? = null,
    val showHowTo: Boolean = false,
    val showOnboarding: Boolean = false,
    val result: ResultState? = null
)
