package com.tunahankara.reflex7.engine

import com.tunahankara.reflex7.R
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

class TaskFactory(private val random: RandomSource) {
    private val colors = listOf(
        ColorEntry("red", R.string.color_red, 0xFFC62828),
        ColorEntry("blue", R.string.color_blue, 0xFF1565C0),
        ColorEntry("green", R.string.color_green, 0xFF2E7D32),
        ColorEntry("yellow", R.string.color_yellow, 0xFF8A7800),
        ColorEntry("purple", R.string.color_purple, 0xFF6A1B9A),
        ColorEntry("orange", R.string.color_orange, 0xFFB84D00)
    )

    fun create(
        generation: Long,
        definition: TaskDefinition,
        level: Int,
        durationMs: Long,
        memory: GameMemory,
        language: String,
        modifiers: List<Modifier>,
        activeRule: ActiveRule?,
        legacyRule: LegacyRule? = null
    ): TaskRound {
        val duration = durationMs / 1000.0
        val base = when (definition.id) {
            "standard" -> standard(generation, definition, legacyRule)
            "package" -> createPackageRound(generation, definition, newPackage().first(), memory.lastCompleted?.clickCount, legacyRule)
            "hold" -> hold(generation, definition, duration)
            "colorShift" -> colorShift(generation, definition, duration)
            "wait" -> waitTask(generation, definition, level, duration)
            "lastSecondInstruction" -> lastSecond(generation, definition, level, duration)
            "patienceCountdown" -> patience(generation, definition, duration)
            "sequence" -> sequence(generation, definition, duration)
            "evade" -> evade(generation, definition, duration)
            "oddOneOut" -> oddOneOut(generation, definition, level)
            "numberExtremum" -> numberExtremum(generation, definition, level)
            "parity" -> parity(generation, definition, level)
            "stroop" -> stroop(generation, definition, level)
            "previousMemory" -> previousMemory(generation, definition, requireNotNull(memory.lastCompleted?.numericAnswer))
            "flashMemory" -> flashMemory(generation, definition, level, duration, activeRule)
            "alphabetical" -> alphabetical(generation, definition, level, language)
            "clickPattern" -> clickPattern(generation, definition, duration)
            "fakeButton" -> fakeButton(generation, definition)
            "delayedInstruction" -> delayedInstruction(generation, definition, duration)
            "positionMemory" -> positionMemory(generation, definition, duration)
            "countSymbols" -> countSymbols(generation, definition, level, activeRule)
            "yesNo" -> yesNo(generation, definition, level, activeRule)
            "directionConflict" -> directionConflict(generation, definition, level, activeRule)
            "mentalMath" -> mentalMath(generation, definition, level, activeRule)
            "missingItem" -> missingItem(generation, definition, level, duration)
            "reverseSequence" -> reverseSequence(generation, definition, level, duration)
            "countByRule" -> countByRule(generation, definition, level)
            "oppositePosition" -> oppositePosition(generation, definition, duration)
            "changingAnswer" -> changingAnswer(generation, definition, level, duration)
            "doubleCondition" -> doubleCondition(generation, definition, level)
            "ruleSwitch" -> ruleSwitch(generation, definition, level, duration)
            "nBack" -> nBack(generation, definition, level, memory, activeRule)
            "previousRuleRecall" -> previousRuleRecall(generation, definition, memory)
            "delayedRecall" -> delayedRecall(generation, definition, duration, memory, activeRule)
            else -> error("Unknown task: ${definition.id}")
        }
        return applyCompatibility(base, level, durationMs, modifiers, activeRule)
    }

    private fun standard(g: Long, d: TaskDefinition, legacyRule: LegacyRule?): TaskRound {
        if (legacyRule == LegacyRule.MOUSE && random.nextDouble() < 0.2) {
            return round(g, d, UiText.Resource(R.string.task_mouse_override), targetClicks = 3)
        }
        val target = random.nextInt(1, 5)
        return round(g, d, UiText.Resource(R.string.task_quick_press, listOf(target)), targetClicks = target)
    }

    fun newPackage(): List<PackageStep> = when (random.nextInt(0, 4)) {
        0 -> listOf(
            PackageStep(UiText.Resource(R.string.task_future_warning), target = 1),
            PackageStep(UiText.Resource(R.string.task_do_not_press_trap), target = 0, buttonColor = 0xFFF44336)
        )
        1 -> {
            val start = random.nextInt(1, 4)
            listOf(
                PackageStep(UiText.Resource(R.string.task_math_subtract, listOf(start + 2)), target = start),
                PackageStep(UiText.Resource(R.string.task_chain_more), chainFromPrevious = true),
                PackageStep(UiText.Resource(R.string.task_chain_more), chainFromPrevious = true)
            )
        }
        2 -> listOf(
            PackageStep(UiText.Resource(R.string.task_blue_rule), target = 1, buttonColor = 0xFF2196F3,
                setLegacyRule = LegacyRule.BLUE_TARGET, designatedBlueTarget = true),
            PackageStep(UiText.Resource(R.string.task_five_minus_two), target = 3),
            PackageStep(UiText.Resource(R.string.task_blue_word), target = 0),
            PackageStep(UiText.Resource(R.string.task_do_not_press), target = 0)
        )
        else -> listOf(
            PackageStep(UiText.Resource(R.string.task_mouse_rule), target = 1, setLegacyRule = LegacyRule.MOUSE),
            PackageStep(UiText.Resource(R.string.task_three_plus_one), target = 4),
            PackageStep(UiText.Resource(R.string.task_press_three), target = 3),
            PackageStep(UiText.Resource(R.string.task_mouse_rule_action), target = 3)
        )
    }

    fun createPackageRound(
        generation: Long,
        definition: TaskDefinition,
        step: PackageStep,
        previousClickCount: Int?,
        legacyRule: LegacyRule?
    ): TaskRound {
        if (legacyRule == LegacyRule.MOUSE && step.setLegacyRule == null && random.nextDouble() < 0.2) {
            return round(generation, definition, UiText.Resource(R.string.task_mouse_override), targetClicks = 3)
        }
        val target = if (step.chainFromPrevious) requireNotNull(previousClickCount) + 1 else requireNotNull(step.target)
        check(step.buttonColor != 0xFF2196F3 || step.designatedBlueTarget) {
            "Only a designated blue target may use the persistent-rule blue"
        }
        return round(generation, definition, step.instruction, targetClicks = target, color = step.buttonColor)
    }

    private fun hold(g: Long, d: TaskDefinition, duration: Double) = round(
        g, d, UiText.Resource(R.string.task_hold_start), color = 0xFFFF9800,
        goActive = false,
        requiredHoldMs = (duration * 0.45).coerceIn(0.45, 1.0).times(1000).toLong()
    )

    private fun colorShift(g: Long, d: TaskDefinition, duration: Double): TaskRound {
        val responseWindow = (duration * 0.32).coerceIn(0.35, 0.8)
        val maxDelay = min(2.0, max(0.2, duration - responseWindow))
        val preferredMin = max(0.35, min(1.0, duration * 0.3))
        val minDelay = min(maxDelay, preferredMin)
        val delay = minDelay + random.nextDouble() * (maxDelay - minDelay)
        return round(g, d, UiText.Resource(R.string.task_color_wait), color = 0xFFF44336,
            goActive = false, events = listOf(event(delay, RoundEventType.COLOR_GO)))
    }

    private fun waitTask(g: Long, d: TaskDefinition, level: Int, duration: Double): TaskRound {
        val progress = ((level - 4) / 46.0).coerceIn(0.0, 1.0)
        val center = 0.58 - progress * 0.2
        val spread = 0.08 + progress * 0.12
        val ratio = center + (random.nextDouble() * 2 - 1) * spread
        val reserve = (duration * 0.38).coerceIn(0.5, 1.1)
        val delay = (duration * ratio).coerceIn(0.32, duration - reserve)
        return round(g, d, UiText.Resource(R.string.task_wait_before), color = 0xFF6D4C41,
            goActive = false, events = listOf(event(delay, RoundEventType.WAIT_GO)))
    }

    private fun lastSecond(g: Long, d: TaskDefinition, level: Int, duration: Double): TaskRound {
        val count = (3 + level / 15).coerceIn(3, colors.size)
        val selected = random.shuffle(colors).take(count)
        val initial = selected[0]
        val final = selected[1]
        val progress = ((level - 7) / 43.0).coerceIn(0.0, 1.0)
        val reserve = (duration * 0.44).coerceIn(0.58, 1.15)
        val center = 0.42 - progress * 0.14
        val spread = 0.06 + progress * 0.08
        val ratio = center + (random.nextDouble() * 2 - 1) * spread
        val delay = (duration * ratio).coerceIn(0.3, duration - reserve)
        val choices = random.shuffle(selected).map { color ->
            TaskChoice(color.id, UiText.Resource(color.nameRes), color == final, color.argb)
        }
        return round(g, d, UiText.Resource(R.string.task_last_initial, listOf(UiText.Resource(initial.nameRes))),
            choices = choices, main = false, inputLocked = true, goActive = false,
            answer = final.id, secondary = initial.id, events = listOf(event(delay, RoundEventType.LAST_FINAL)))
    }

    private fun patience(g: Long, d: TaskDefinition, duration: Double): TaskRound {
        val reserve = (duration * 0.32).coerceIn(0.48, 0.9)
        val budget = duration - reserve
        val maxHesitation = min(0.42, budget * 0.25)
        val hesitation = if (random.nextDouble() >= 0.5) 0.08 + random.nextDouble() * max(0.0, maxHesitation - 0.08) else 0.0
        val countdown = budget - maxHesitation
        val step = countdown / 3
        val events = buildList {
            add(event(step, RoundEventType.PATIENCE_TWO))
            add(event(step * 2, RoundEventType.PATIENCE_ONE))
            if (hesitation > 0) add(event(countdown, RoundEventType.PATIENCE_HESITATE))
            add(event(countdown + hesitation, RoundEventType.PATIENCE_GO))
        }
        return round(g, d, UiText.Resource(R.string.task_patience), color = 0xFF5D4037, goActive = false,
            status = UiText.Literal("3"), events = events)
    }

    private fun sequence(g: Long, d: TaskDefinition, duration: Double): TaskRound {
        val count = floor(duration / 0.48).toInt().coerceIn(2, 4)
        val choices = (1..count).map { value ->
            TaskChoice(value.toString(), UiText.Literal(value.toString()), value == 1)
        }
        return round(g, d, UiText.Resource(R.string.task_sequence_name), choices = choices, main = false,
            sequence = (1..count).map(Int::toString), metadata = mapOf("layoutSeed" to random.nextInt(0, Int.MAX_VALUE).toString()))
    }

    private fun evade(g: Long, d: TaskDefinition, duration: Double): TaskRound {
        val maximum = floor(duration / 0.42).toInt().coerceIn(2, 4)
        val minimum = min(3, maximum)
        val target = random.nextInt(minimum, maximum + 1)
        val choice = TaskChoice("evade", UiText.Resource(R.string.task_catch), true, 0xFFFF5722,
            x = 0.15f + random.nextDouble().toFloat() * 0.7f, y = 0.15f + random.nextDouble().toFloat() * 0.7f)
        return round(g, d, UiText.Resource(R.string.task_evade_name), choices = listOf(choice), main = false,
            targetClicks = target, moving = true)
    }

    private fun oddOneOut(g: Long, d: TaskDefinition, level: Int): TaskRound {
        val count = band(level).options
        val pairs = if (level >= 25) listOf("▲" to "△", "●" to "◉", "■" to "□") else listOf("●" to "◆", "▲" to "■", "★" to "●")
        val pair = pairs[random.nextInt(0, pairs.size)]
        val odd = random.nextInt(0, count)
        return grid(g, d, R.string.task_odd_instruction, (0 until count).map { index ->
            TaskChoice(index.toString(), UiText.Literal(if (index == odd) pair.second else pair.first), index == odd)
        })
    }

    private fun numberExtremum(g: Long, d: TaskDefinition, level: Int): TaskRound {
        val count = band(level).options.coerceIn(4, 7)
        val start = random.nextInt(if (level >= 13) -35 else 1, 29)
        val step = random.nextInt(1, if (level >= 13) 5 else 8)
        val values = (0 until count).map { start + it * step }
        val largest = random.nextDouble() < 0.5
        val answer = if (largest) values.max() else values.min()
        return grid(g, d, if (largest) R.string.task_largest else R.string.task_smallest,
            random.shuffle(values).map { value -> TaskChoice(value.toString(), UiText.Literal(value.toString()), value == answer) }, answer = answer.toString())
    }

    private fun parity(g: Long, d: TaskDefinition, level: Int): TaskRound {
        val count = band(level).options.coerceIn(4, 7)
        val even = random.nextDouble() < 0.5
        val answer = random.nextInt(if (level >= 13) -15 else 1, 25) * 2 + if (even) 0 else 1
        val opposite = if (even) 1 else 0
        val base = random.nextInt(if (level >= 13) -15 else 1, 21) * 2 + opposite
        val values = listOf(answer) + (0 until count - 1).map { base + it * 2 }
        return grid(g, d, if (even) R.string.task_even else R.string.task_odd,
            random.shuffle(values).map { value -> TaskChoice(value.toString(), UiText.Literal(value.toString()), value == answer) }, answer = answer.toString())
    }

    private fun stroop(g: Long, d: TaskDefinition, level: Int): TaskRound {
        val selected = random.shuffle(colors.take(4)).take(3)
        val target = selected[0]
        val mode = if (level >= 20) random.nextInt(0, 3) else random.nextInt(0, 2)
        val choices = selected.mapIndexed { index, word ->
            val ink = if (mode == 2) {
                if (index < 2) target else selected[1]
            } else selected[(index + 1) % selected.size]
            val correct = when (mode) { 0 -> word == target; 1 -> ink == target; else -> ink != target }
            TaskChoice(word.id, UiText.Resource(word.nameRes), correct, ink.argb)
        }
        val instruction = when (mode) {
            0 -> R.string.task_stroop_word
            1 -> R.string.task_stroop_ink
            else -> R.string.task_stroop_not
        }
        return round(g, d, UiText.Resource(instruction, listOf(UiText.Resource(target.nameRes))),
            choices = random.shuffle(choices), main = false, answer = choices.single { it.correct }.id)
    }

    private fun previousMemory(g: Long, d: TaskDefinition, answer: Int): TaskRound {
        val values = listOf(answer, answer - 2, answer + 1, answer + 3)
        return grid(g, d, R.string.task_previous, random.shuffle(values).map {
            TaskChoice(it.toString(), UiText.Literal(it.toString()), it == answer)
        }, answer = answer.toString())
    }

    private fun flashMemory(g: Long, d: TaskDefinition, level: Int, duration: Double, rule: ActiveRule?): TaskRound {
        val symbols = if (rule?.rule == GlobalRule.EMOJI_LITERAL) listOf("🍎", "⚡", "🌙", "⭐") else listOf("●", "▲", "■", "◆")
        val sequence = (0 until band(level).memory).map { symbols[random.nextInt(0, symbols.size)] }
        val delay = (duration * 0.28).coerceIn(0.45, 1.2)
        return round(g, d, UiText.Resource(R.string.task_flash_watch, listOf(sequence.joinToString(" "))), main = false,
            inputLocked = true, goActive = false, sequence = sequence,
            events = listOf(event(delay, RoundEventType.FLASH_REVEAL)))
    }

    private fun alphabetical(g: Long, d: TaskDefinition, level: Int, language: String): TaskRound {
        val alphabet = if (language == "tr") listOf("A", "Ç", "E", "Ğ", "İ", "Ö", "Ş", "Ü") else listOf("A", "C", "E", "G", "I", "O", "S", "U")
        val selected = random.shuffle(alphabet).take(band(level).memory.coerceIn(3, 5))
        val order = if (language == "tr") selected.sortedWith(compareBy { turkishOrder(it) }) else selected.sorted()
        return grid(g, d, R.string.task_alphabet, random.shuffle(selected).map {
            TaskChoice(it, UiText.Literal(it), it == order.first())
        }, sequence = order)
    }

    private fun clickPattern(g: Long, d: TaskDefinition, duration: Double): TaskRound {
        val required = if (random.nextDouble() < 0.5) 1 else 2
        val window = (duration * 300).toLong().coerceIn(320, 440)
        return round(g, d, UiText.Resource(if (required == 1) R.string.task_single else R.string.task_double),
            targetClicks = required, events = listOf(RoundEvent(window, RoundEventType.CLICK_SETTLE)))
    }

    private fun fakeButton(g: Long, d: TaskDefinition): TaskRound {
        val real = random.nextInt(0, 4)
        val choices = (0 until 4).map { index ->
            TaskChoice(index.toString(), UiText.Resource(if (index == real) R.string.real else R.string.fake), index == real,
                style = if (index == real) ChoiceStyle.REAL else ChoiceStyle.FAKE)
        }
        return grid(g, d, R.string.task_fake, choices)
    }

    private fun delayedInstruction(g: Long, d: TaskDefinition, duration: Double): TaskRound {
        val answer = random.nextInt(1, 5)
        val delay = (duration * 0.22).coerceIn(0.35, 0.8)
        return round(g, d, UiText.Resource(R.string.task_delayed_initial), main = false, inputLocked = true,
            goActive = false, answer = answer.toString(), events = listOf(event(delay, RoundEventType.DELAYED_REVEAL)))
    }

    private fun positionMemory(g: Long, d: TaskDefinition, duration: Double): TaskRound {
        val answer = random.nextInt(0, 9)
        val delay = (duration * 0.22).coerceIn(0.4, 0.9)
        val choices = (0 until 9).map { index ->
            TaskChoice(index.toString(), UiText.Literal(" "), index == answer,
                style = if (index == answer) ChoiceStyle.MEMORY else ChoiceStyle.NORMAL)
        }
        return round(g, d, UiText.Resource(R.string.task_position_watch), choices = choices, main = false,
            inputLocked = true, goActive = false, answer = answer.toString(), events = listOf(event(delay, RoundEventType.POSITION_HIDE)))
    }

    private fun countSymbols(g: Long, d: TaskDefinition, level: Int, rule: ActiveRule?): TaskRound {
        val symbols = if (rule?.rule == GlobalRule.EMOJI_LITERAL) listOf("🍎", "⚡", "🌙", "⭐") else listOf("★", "●", "▲", "◆")
        val symbol = symbols[random.nextInt(0, symbols.size)]
        val answer = random.nextInt(2, band(level).memory + 2)
        val total = (band(level).options + 2).coerceIn(6, 11)
        val display = MutableList(answer) { symbol }
        while (display.size < total) display += symbols.filter { it != symbol }[random.nextInt(0, 3)]
        val answers = listOf(answer, max(0, answer - 1), answer + 1, answer + 2).distinct()
        val instruction = UiText.Resource(R.string.task_count, listOf(symbol, random.shuffle(display).joinToString(" ")))
        return round(g, d, instruction, choices = random.shuffle(answers).map {
            TaskChoice(it.toString(), UiText.Literal(it.toString()), it == answer)
        }, main = false, answer = answer.toString())
    }

    private fun yesNo(g: Long, d: TaskDefinition, level: Int, rule: ActiveRule?): TaskRound {
        val number = random.nextInt(if (level >= 13) -12 else 1, 31)
        val claimedEven = random.nextDouble() < 0.5
        val trueStatement = kotlin.math.abs(number % 2) == if (claimedEven) 0 else 1
        val yes = if (rule?.rule == GlobalRule.INVERT) !trueStatement else trueStatement
        val choices = listOf(
            TaskChoice("yes", UiText.Resource(R.string.yes), yes),
            TaskChoice("no", UiText.Resource(R.string.no), !yes)
        )
        return round(g, d, UiText.Resource(R.string.task_statement, listOf(number, UiText.Resource(if (claimedEven) R.string.even_claim else R.string.odd_claim))),
            choices = choices, main = false, answer = number.toString())
    }

    private fun directionConflict(g: Long, d: TaskDefinition, level: Int, rule: ActiveRule?): TaskRound {
        val directionIds = if (level >= 24) listOf("left", "right", "up", "down") else listOf("left", "right")
        val arrows = mapOf("left" to "←", "right" to "→", "up" to "↑", "down" to "↓")
        val opposite = mapOf("left" to "right", "right" to "left", "up" to "down", "down" to "up")
        val arrow = directionIds[random.nextInt(0, directionIds.size)]
        var position = directionIds[random.nextInt(0, directionIds.size)]
        if (position == arrow) position = opposite.getValue(arrow)
        val useArrow = random.nextDouble() < 0.5
        val semantic = if (useArrow) arrow else position
        val answer = if (rule?.rule == GlobalRule.INVERT) opposite.getValue(semantic) else semantic
        val labels = mapOf(
            "left" to R.string.direction_left, "right" to R.string.direction_right,
            "up" to R.string.direction_up, "down" to R.string.direction_down
        )
        return round(
            g, d, UiText.Resource(if (useArrow) R.string.task_direction_arrow else R.string.task_direction_position),
            choices = random.shuffle(directionIds).map { id ->
                TaskChoice(id, UiText.Resource(labels.getValue(id)), id == answer)
            }, main = false, answer = answer, status = UiText.Literal(arrows.getValue(arrow)),
            metadata = mapOf("arrow" to arrow, "position" to position, "historyItem" to arrow)
        )
    }

    private fun mentalMath(g: Long, d: TaskDefinition, level: Int, rule: ActiveRule?): TaskRound {
        val (expression, actual) = if (level >= 30) {
            val a = random.nextInt(3, 9)
            val b = random.nextInt(2, 7)
            val c = random.nextInt(2, 10)
            "($a × $b) − $c" to (a * b - c)
        } else if (level >= 18) {
            val a = random.nextInt(3, 10)
            val b = random.nextInt(2, 8)
            "$a × $b" to (a * b)
        } else {
            val a = random.nextInt(3, 19)
            val b = random.nextInt(2, 13)
            val operator = if (random.nextDouble() < 0.5) "+" else "-"
            "$a $operator $b" to when (operator) { "+" -> a + b; "-" -> a - b; else -> a * b }
        }
        val isTrue = random.nextDouble() < 0.5
        val claimed = if (isTrue) actual else actual + listOf(-3, -2, -1, 1, 2, 3)[random.nextInt(0, 6)]
        val yesCorrect = if (rule?.rule == GlobalRule.INVERT) !isTrue else isTrue
        return round(
            g, d, UiText.Resource(R.string.task_mental_math, listOf(expression, claimed)),
            choices = listOf(
                TaskChoice("yes", UiText.Resource(R.string.yes), yesCorrect),
                TaskChoice("no", UiText.Resource(R.string.no), !yesCorrect)
            ), main = false, answer = claimed.toString(),
            metadata = mapOf("actual" to actual.toString(), "claimed" to claimed.toString(), "historyItem" to claimed.toString())
        )
    }

    private fun missingItem(g: Long, d: TaskDefinition, level: Int, duration: Double): TaskRound {
        val pool = if (level >= 30) listOf("●", "○", "■", "□", "▲", "△", "◆", "◇", "★") else listOf("●", "■", "▲", "◆", "★", "✚", "⬟", "☀")
        val count = if (level >= 30) 7 else if (level >= 20) 6 else 5
        val shown = random.shuffle(pool).take(count)
        val missing = shown[random.nextInt(0, shown.size)]
        val remaining = shown.filter { it != missing }
        val options = random.shuffle(shown)
        val delay = (duration * 0.28).coerceIn(0.45, 1.05)
        return round(
            g, d, UiText.Resource(R.string.task_missing_watch, listOf(shown.joinToString(" "))),
            main = false, inputLocked = true, goActive = false, answer = missing,
            events = listOf(event(delay, RoundEventType.MISSING_REVEAL)),
            visualItems = shown.map { TaskVisualItem(it) },
            metadata = mapOf("remaining" to remaining.joinToString("|"), "options" to options.joinToString("|"), "historyItem" to missing)
        )
    }

    private fun reverseSequence(g: Long, d: TaskDefinition, level: Int, duration: Double): TaskRound {
        val count = if (level >= 32) 5 else if (level >= 22) 4 else 3
        val source = random.shuffle(memoryItems(level)).take(count)
        val delay = (duration * 0.28).coerceIn(0.45, 1.05)
        return round(
            g, d, UiText.Resource(R.string.task_reverse_watch, listOf(source.joinToString(" "))),
            main = false, inputLocked = true, goActive = false, sequence = source.reversed(),
            events = listOf(event(delay, RoundEventType.REVERSE_REVEAL)),
            visualItems = source.map { TaskVisualItem(it) }, metadata = mapOf("source" to source.joinToString("|"))
        )
    }

    private fun countByRule(g: Long, d: TaskDefinition, level: Int): TaskRound {
        val shapeDefs = listOf("triangle" to "▲", "circle" to "●", "square" to "■")
        val shapeNames = mapOf("triangle" to R.string.shape_triangle, "circle" to R.string.shape_circle, "square" to R.string.shape_square)
        val palette = colors.take(if (level >= 30) 4 else 3)
        val selectedColor = palette[random.nextInt(0, palette.size)]
        val selectedShape = shapeDefs[random.nextInt(0, shapeDefs.size)]
        val total = if (level >= 30) 11 else 8
        val answer = random.nextInt(1, if (level >= 30) 5 else 4)
        val items = MutableList(answer) { TaskVisualItem(selectedShape.second, selectedColor.argb) }
        while (items.size < total) {
            val shape = shapeDefs[random.nextInt(0, shapeDefs.size)]
            var color = palette[random.nextInt(0, palette.size)]
            if (shape == selectedShape && color == selectedColor) color = palette.first { it != selectedColor }
            items += TaskVisualItem(shape.second, color.argb)
        }
        val answers = listOf(answer, max(0, answer - 1), answer + 1, answer + 2).distinct()
        return round(
            g, d, UiText.Resource(R.string.task_count_rule, listOf(UiText.Resource(selectedColor.nameRes), UiText.Resource(shapeNames.getValue(selectedShape.first)))),
            choices = random.shuffle(answers).map { TaskChoice(it.toString(), UiText.Literal(it.toString()), it == answer) },
            main = false, answer = answer.toString(), visualItems = random.shuffle(items),
            metadata = mapOf("targetColor" to selectedColor.id, "answerShape" to selectedShape.second, "historyItem" to answer.toString())
        )
    }

    private fun oppositePosition(g: Long, d: TaskDefinition, duration: Double): TaskRound {
        val positions = listOf(0, 1, 2, 3, 5, 6, 7, 8)
        val target = positions[random.nextInt(0, positions.size)]
        val answer = 8 - target
        val delay = (duration * 0.25).coerceIn(0.42, 0.95)
        val choices = (0 until 9).map { index ->
            TaskChoice(index.toString(), UiText.Literal(" "), index == answer,
                style = if (index == target) ChoiceStyle.MEMORY else ChoiceStyle.NORMAL)
        }
        return round(
            g, d, UiText.Resource(R.string.task_opposite_watch), choices = choices, main = false,
            inputLocked = true, goActive = false, answer = answer.toString(),
            events = listOf(event(delay, RoundEventType.OPPOSITE_HIDE)), metadata = mapOf("target" to target.toString())
        )
    }

    private fun changingAnswer(g: Long, d: TaskDefinition, level: Int, duration: Double): TaskRound {
        val count = if (level >= 30) 6 else 4
        val values = random.shuffle((1..6).toList()).take(count)
        val answer = values[random.nextInt(0, values.size)]
        val delay = (duration * 0.28).coerceIn(0.42, 0.95)
        return round(
            g, d, UiText.Resource(R.string.task_changing_watch),
            choices = random.shuffle(values).map { TaskChoice(it.toString(), UiText.Literal(it.toString()), it == answer) },
            main = false, inputLocked = true, goActive = false, answer = answer.toString(),
            events = listOf(event(delay, RoundEventType.CHANGE_POSITIONS)), metadata = mapOf("historyItem" to answer.toString())
        )
    }

    private fun doubleCondition(g: Long, d: TaskDefinition, level: Int): TaskRound {
        if (random.nextDouble() < 0.66) {
            val largestEven = random.nextDouble() < 0.5
            val values = uniqueNumbers(band(level).options.coerceIn(5, 7), if (level >= 28) -18 else 1, 42)
            val eligible = values.filter { kotlin.math.abs(it % 2) == if (largestEven) 0 else 1 }
            val answer = if (eligible.isEmpty()) {
                val fallback = if (largestEven) 2 else 3
                fallback
            } else if (largestEven) eligible.max() else eligible.min()
            val finalValues = if (answer in values) values else values.dropLast(1) + answer
            return grid(
                g, d, if (largestEven) R.string.task_double_largest_even else R.string.task_double_smallest_odd,
                random.shuffle(finalValues).map { TaskChoice(it.toString(), UiText.Literal(it.toString()), it == answer) }, answer = answer.toString()
            ).copy(metadata = mapOf("historyItem" to answer.toString()))
        }
        val shapes = listOf("▲", "●", "■")
        val selectedColor = colors.take(4)[random.nextInt(0, 4)]
        val selectedShape = shapes[random.nextInt(0, shapes.size)]
        val correctId = "${selectedColor.id}:$selectedShape"
        val choices = mutableListOf(TaskChoice(correctId, UiText.Literal(selectedShape), true, selectedColor.argb))
        colors.take(4).forEach { color -> shapes.forEach { shape ->
            val id = "${color.id}:$shape"
            if (id != correctId && choices.size < band(level).options.coerceIn(5, 7)) choices += TaskChoice(id, UiText.Literal(shape), false, color.argb)
        } }
        return round(
            g, d, UiText.Resource(R.string.task_double_color_shape, listOf(UiText.Resource(selectedColor.nameRes), selectedShape)),
            choices = random.shuffle(choices), main = false, answer = correctId,
            metadata = mapOf("answerColor" to selectedColor.id, "historyItem" to selectedShape)
        )
    }

    private fun ruleSwitch(g: Long, d: TaskDefinition, level: Int, duration: Double): TaskRound {
        val ruleResources = mapOf(
            "largest" to R.string.rule_select_largest, "smallest" to R.string.rule_select_smallest,
            "even" to R.string.rule_select_even, "odd" to R.string.rule_select_odd
        )
        val rules = ruleResources.keys.toList()
        val initial = rules[random.nextInt(0, rules.size)]
        val final = rules.filter { it != initial }[random.nextInt(0, rules.size - 1)]
        val count = if (level >= 35) 6 else 4
        val values = if (final == "even" || final == "odd") {
            val wantedParity = if (final == "even") 0 else 1
            val answer = random.nextInt(2, 25) * 2 + wantedParity
            val distractorStart = random.nextInt(1, 12)
            val distractors = (0 until count - 1).map { (distractorStart + it) * 2 + (1 - wantedParity) }
            (listOf(answer) + distractors).toMutableList()
        } else uniqueNumbers(count, 2, 49).toMutableList()
        val eligible = when (final) {
            "even" -> values.filter { it % 2 == 0 }
            "odd" -> values.filter { kotlin.math.abs(it % 2) == 1 }
            else -> values
        }
        val answer = when (final) { "largest" -> values.max(); "smallest" -> values.min(); "even" -> eligible.max(); else -> eligible.min() }
        val delay = (duration * (0.36 - ((level - 23).coerceAtLeast(0) * 0.006))).coerceIn(0.42, duration - 0.65)
        return round(
            g, d, UiText.Resource(R.string.task_rule_switch_initial, listOf(UiText.Resource(ruleResources.getValue(initial)))),
            choices = random.shuffle(values.distinct()).map { TaskChoice(it.toString(), UiText.Literal(it.toString()), it == answer) },
            main = false, inputLocked = true, goActive = false, answer = answer.toString(),
            events = listOf(event(delay, RoundEventType.RULE_SWITCH)),
            metadata = mapOf("initialRule" to initial, "finalRule" to final, "historyItem" to answer.toString())
        )
    }

    private fun nBack(g: Long, d: TaskDefinition, level: Int, memory: GameMemory, rule: ActiveRule?): TaskRound {
        val maximum = when { level >= 40 -> 3; level >= 32 -> 2; else -> 1 }
        val depth = (maximum downTo 1).first { memory.completed.getOrNull(memory.completed.size - it)?.item != null }
        val reference = requireNotNull(memory.completed.getOrNull(memory.completed.size - depth)?.item)
        val match = random.nextDouble() < 0.5
        val alternatives = memoryItems(level).filter { it != reference }
        val item = if (match) reference else alternatives[random.nextInt(0, alternatives.size)]
        val yesCorrect = if (rule?.rule == GlobalRule.INVERT) !match else match
        return round(
            g, d, UiText.Resource(R.string.task_n_back, listOf(depth)),
            choices = listOf(
                TaskChoice("yes", UiText.Resource(R.string.yes), yesCorrect),
                TaskChoice("no", UiText.Resource(R.string.no), !yesCorrect)
            ), main = false, answer = item, status = UiText.Literal(item),
            metadata = mapOf("depth" to depth.toString(), "match" to match.toString(), "historyItem" to item)
        )
    }

    private fun previousRuleRecall(g: Long, d: TaskDefinition, memory: GameMemory): TaskRound {
        val previous = memory.completed.last()
        val available = buildList {
            previous.colorAnswer?.let { add("color" to it) }
            previous.side?.let { add("side" to it.name.lowercase()) }
            previous.parity?.let { add("parity" to it.name.lowercase()) }
        }
        val (kind, answer) = available[random.nextInt(0, available.size)]
        val choices = when (kind) {
            "color" -> random.shuffle(colors).map { TaskChoice(it.id, UiText.Resource(it.nameRes), it.id == answer, it.argb) }
            "side" -> listOf(
                TaskChoice("left", UiText.Resource(R.string.direction_left), answer == "left"),
                TaskChoice("right", UiText.Resource(R.string.direction_right), answer == "right")
            )
            else -> listOf(
                TaskChoice("odd", UiText.Resource(R.string.parity_odd), answer == "odd"),
                TaskChoice("even", UiText.Resource(R.string.parity_even), answer == "even")
            )
        }
        val instruction = when (kind) { "color" -> R.string.task_previous_color; "side" -> R.string.task_previous_side; else -> R.string.task_previous_parity }
        return grid(g, d, instruction, choices, answer = answer).copy(metadata = mapOf("recallKind" to kind, "historyItem" to answer))
    }

    private fun delayedRecall(g: Long, d: TaskDefinition, duration: Double, memory: GameMemory, rule: ActiveRule?): TaskRound {
        val pending = memory.pendingRecall
        if (pending != null) {
            return round(
                g, d, UiText.Resource(R.string.task_delayed_recall_question),
                choices = random.shuffle(pending.options).map { TaskChoice(it, UiText.Literal(it), it == pending.item) },
                main = false, answer = pending.item,
                metadata = mapOf("cue" to "false", "historyItem" to pending.item)
            )
        }
        val pool = if (rule?.rule == GlobalRule.EMOJI_LITERAL) listOf("🍎", "⚡", "🌙", "⭐") else memoryItems(40)
        val item = pool[random.nextInt(0, pool.size)]
        val options = random.shuffle((listOf(item) + pool.filter { it != item }).distinct()).take(4).toMutableList()
        if (item !in options) options[0] = item
        val gap = random.nextInt(2, 5)
        val delay = (duration * 0.18).coerceIn(0.3, 0.65)
        return round(
            g, d, UiText.Resource(R.string.task_delayed_recall_remember, listOf(item)), main = false,
            inputLocked = true, goActive = false, answer = item,
            events = listOf(event(delay, RoundEventType.DELAYED_RECALL_CUE)), status = UiText.Literal(item),
            metadata = mapOf("cue" to "true", "options" to options.joinToString("|"), "gap" to gap.toString(), "historyItem" to item)
        )
    }

    private fun applyCompatibility(base: TaskRound, level: Int, durationMs: Long, modifiers: List<Modifier>, rule: ActiveRule?): TaskRound {
        var choices = base.choices
        var locked = base.inputLocked
        val events = base.events.toMutableList()
        if (Modifier.SWAP in modifiers && choices.isNotEmpty()) {
            locked = true
            events += RoundEvent((durationMs * 0.16).toLong().coerceIn(260, 520), RoundEventType.SWAP_CHOICES)
        }
        if (Modifier.DELAYED in modifiers) {
            locked = true
            events += RoundEvent((durationMs * 0.18).toLong().coerceIn(280, 600), RoundEventType.MODIFIER_UNLOCK)
        }
        if (rule?.rule == GlobalRule.ODD_WAIT && level % 2 == 1) {
            locked = true
            events += RoundEvent((durationMs * 0.18).toLong().coerceIn(280, 600), RoundEventType.RULE_UNLOCK)
        }
        if (rule?.rule == GlobalRule.IGNORE_RED && choices.any { !it.correct }) {
            val decoys = choices.filter { !it.correct }
            val red = decoys[random.nextInt(0, decoys.size)].id
            choices = choices.map { if (it.id == red) it.copy(style = ChoiceStyle.RED_DECOY, color = 0xFF7A1111) else it }
        }
        return base.copy(choices = choices, inputLocked = locked, modifiers = modifiers, globalRule = rule, events = events)
    }

    private fun grid(g: Long, d: TaskDefinition, instructionRes: Int, choices: List<TaskChoice>, answer: String? = null, sequence: List<String> = emptyList()) =
        round(g, d, UiText.Resource(instructionRes), choices = choices, main = false, answer = answer, sequence = sequence)

    private fun round(
        g: Long, d: TaskDefinition, instruction: UiText, choices: List<TaskChoice> = emptyList(),
        events: List<RoundEvent> = emptyList(), main: Boolean = true, color: Long = 0xFF4CAF50,
        inputLocked: Boolean = false, goActive: Boolean = true, targetClicks: Int = 1,
        requiredHoldMs: Long = 0, sequence: List<String> = emptyList(), answer: String? = null,
        secondary: String? = null, status: UiText? = null, moving: Boolean = false,
        visualItems: List<TaskVisualItem> = emptyList(), metadata: Map<String, String> = emptyMap()
    ) = TaskRound(g, d, instruction, instruction, choices, events, main, color, inputLocked, goActive,
        targetClicks, requiredHoldMs = requiredHoldMs, sequence = sequence, answer = answer,
        secondaryAnswer = secondary, statusText = status, movingTarget = moving,
        visualItems = visualItems, metadata = metadata)

    private fun event(seconds: Double, type: RoundEventType) = RoundEvent((seconds * 1000).toLong(), type)
    private fun band(level: Int) = DifficultyProgression.band(level)
    private fun turkishOrder(letter: String): Int = "ABCÇDEFGĞHIİJKLMNOÖPRSŞTUÜVYZ".indexOf(letter)
    private fun memoryItems(level: Int) = listOf("●", "▲", "■", "◆", "A", "K", "7", "3")
    private fun uniqueNumbers(count: Int, from: Int, until: Int): List<Int> {
        val values = linkedSetOf<Int>()
        var attempts = 0
        while (values.size < count && attempts < count * 8) { values += random.nextInt(from, until); attempts += 1 }
        for (candidate in from until until) { if (values.size >= count) break; values += candidate }
        return values.toList()
    }
    private data class ColorEntry(val id: String, val nameRes: Int, val argb: Long)
}
