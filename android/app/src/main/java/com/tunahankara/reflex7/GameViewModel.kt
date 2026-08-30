package com.tunahankara.reflex7

import android.app.Application
import android.os.SystemClock
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tunahankara.reflex7.audio.RetroAudio
import com.tunahankara.reflex7.audio.SoundCue
import com.tunahankara.reflex7.data.PreferencesRepository
import com.tunahankara.reflex7.engine.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.max

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: PreferencesRepository = (application as Reflex7Application).preferencesRepository
    private val random: RandomSource = SeededRandom()
    private val engine = TaskEngine(random)
    private val factory = TaskFactory(random)
    private val audio = RetroAudio()
    private val timer = MonotonicTimer { SystemClock.elapsedRealtime() }
    private val resolutionGate = ResolutionGate()
    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var transitionJob: Job? = null
    private var announcementJob: Job? = null
    private val eventRecords = mutableListOf<EventRecord>()
    private val inputLocks = mutableSetOf<String>()
    private var memory = GameMemory()
    private var activeRule: ActiveRule? = null
    private var completedTasks = 0
    private var generation = 0L
    private var sessionStartedAt = 0L
    private var totalPausedMs = 0L
    private var pausedAt = 0L
    private var holdStartedAt = 0L
    private var holdPausedAt = 0L
    private var holding = false
    private var urgencyStage = 0
    private var personalBest = false
    private var personalBestAnnounced = false
    private var modifierDiscoveryShown = false
    private var roundStarted = false
    private var transitionRemainingMs = 0L
    private var transitionStartedAt = 0L
    private val packageQueue = ArrayDeque<PackageStep>()
    private var legacyRule: LegacyRule? = null

    init {
        viewModelScope.launch {
            repository.preferences.collect { preferences ->
                audio.setEnabled(preferences.soundEnabled)
                _uiState.update { current ->
                    current.copy(
                        preferences = preferences,
                        nickname = if (current.nickname.isBlank()) preferences.nickname else current.nickname,
                        showOnboarding = !preferences.onboardingSeen && current.screen == GameScreen.MENU
                    )
                }
            }
        }
    }

    fun updateNickname(value: String) {
        val sanitized = value.take(12)
        _uiState.update { it.copy(nickname = sanitized) }
        viewModelScope.launch { repository.setNickname(sanitized) }
    }

    fun toggleSound() {
        val enabled = !_uiState.value.preferences.soundEnabled
        audio.setEnabled(enabled)
        viewModelScope.launch { repository.setSound(enabled) }
        _uiState.update { it.copy(preferences = it.preferences.copy(soundEnabled = enabled)) }
    }

    fun setLanguage(language: String) {
        if (language !in setOf("tr", "en")) return
        _uiState.update { it.copy(preferences = it.preferences.copy(language = language)) }
        viewModelScope.launch { repository.setLanguage(language) }
    }

    fun showHowTo(value: Boolean) = _uiState.update { it.copy(showHowTo = value) }

    fun dismissOnboarding() {
        _uiState.update { it.copy(showOnboarding = false, preferences = it.preferences.copy(onboardingSeen = true)) }
        viewModelScope.launch { repository.setOnboardingSeen() }
    }

    fun startGame(mode: GameMode) {
        cleanRuntime()
        engine.clearHistory()
        memory = GameMemory()
        activeRule = null
        completedTasks = 0
        generation = 0
        totalPausedMs = 0
        sessionStartedAt = SystemClock.elapsedRealtime()
        personalBest = false
        personalBestAnnounced = false
        modifierDiscoveryShown = false
        packageQueue.clear()
        legacyRule = null
        val nickname = _uiState.value.nickname.trim().take(12)
        _uiState.update {
            it.copy(
                screen = GameScreen.PLAYING,
                mode = mode,
                nickname = nickname,
                level = 1,
                score = 0,
                combo = 0,
                highestCombo = 0,
                paused = false,
                result = null,
                announcement = null
            )
        }
        viewModelScope.launch { repository.setNickname(nickname) }
        audio.play(SoundCue.START)
        beginNextRound()
    }

    fun retry() = startGame(_uiState.value.mode)

    fun returnToMenu() {
        cleanRuntime()
        _uiState.update { it.copy(screen = GameScreen.MENU, paused = false, round = null, result = null, announcement = null) }
    }

    private fun beginNextRound() {
        cleanRound()
        val state = _uiState.value
        if (state.screen != GameScreen.PLAYING) return
        val durationSeconds = TaskEngine.taskDuration(state.mode, state.level)
        val continuingPackage = packageQueue.isNotEmpty()
        if (!continuingPackage && engine.isRecallDue(memory, completedTasks)) activeRule = null
        var startedGlobalRule = false
        if (!continuingPackage && activeRule == null && memory.pendingRecall == null) {
            activeRule = engine.maybeStartGlobalRule(state.level, durationSeconds, completedTasks)
            startedGlobalRule = activeRule != null
        }
        val definition = if (continuingPackage) TaskRegistry.byId("package")
            else engine.selectTask(state.level, durationSeconds, memory, activeRule?.rule, completedTasks)
        val modifiers = if (continuingPackage || definition.id == "package") emptyList() else engine.selectModifiers(definition, state.level)
        if (!continuingPackage) engine.remember(definition)
        generation += 1
        resolutionGate.begin(generation)
        val round = if (definition.id == "package") {
            if (packageQueue.isEmpty()) packageQueue.addAll(factory.newPackage())
            val step = packageQueue.removeFirst()
            step.setLegacyRule?.let { legacyRule = it }
            factory.createPackageRound(generation, definition, step, memory.lastCompleted?.clickCount, legacyRule)
        } else factory.create(
            generation, definition, state.level, (durationSeconds * 1000).toLong(), memory,
            state.preferences.language, modifiers, activeRule, legacyRule
        )
        initializeLocks(round)
        roundStarted = false
        _uiState.update {
            it.copy(
                durationMs = (durationSeconds * 1000).toLong(),
                remainingMs = (durationSeconds * 1000).toLong(),
                round = round.copy(inputLocked = inputLocks.isNotEmpty()),
                inputLocked = true
            )
        }
        armTransition(round, announceDiscoveries(round, startedGlobalRule).coerceAtLeast(180))
    }

    private fun armTransition(round: TaskRound, delayMs: Long) {
        transitionJob?.cancel()
        transitionRemainingMs = delayMs
        transitionStartedAt = SystemClock.elapsedRealtime()
        transitionJob = viewModelScope.launch {
            delay(delayMs)
            if (_uiState.value.round?.generation != round.generation || _uiState.value.paused) return@launch
            transitionRemainingMs = 0
            _uiState.update { it.copy(inputLocked = false) }
            timer.start(_uiState.value.durationMs)
            roundStarted = true
            scheduleRoundEvents(round)
            startTimerLoop()
            audio.play(SoundCue.LEVEL)
        }
    }

    private fun initializeLocks(round: TaskRound) {
        inputLocks.clear()
        if (round.inputLocked) {
            if (round.definition.id in setOf(
                    "lastSecondInstruction", "flashMemory", "delayedInstruction", "positionMemory",
                    "ruleSwitch", "oppositePosition", "missingItem", "reverseSequence",
                    "changingAnswer", "delayedRecall"
                )) inputLocks += "task"
            if (Modifier.DELAYED in round.modifiers) inputLocks += "modifier"
            if (Modifier.SWAP in round.modifiers) inputLocks += "swap"
            if (round.globalRule?.rule == GlobalRule.ODD_WAIT && _uiState.value.level % 2 == 1) inputLocks += "rule"
        }
    }

    private fun scheduleRoundEvents(round: TaskRound) {
        eventRecords.clear()
        round.events.filter { it.type != RoundEventType.CLICK_SETTLE }.forEach { event ->
            val record = EventRecord(event.type, event.delayMs)
            eventRecords += record
            armEvent(record, round.generation)
        }
    }

    private fun armEvent(record: EventRecord, expectedGeneration: Long) {
        record.startedAt = SystemClock.elapsedRealtime()
        record.job = viewModelScope.launch {
            delay(record.remainingMs)
            if (_uiState.value.round?.generation == expectedGeneration && !_uiState.value.paused) {
                eventRecords.remove(record)
                handleEvent(record.type)
            }
        }
    }

    private fun handleEvent(type: RoundEventType) {
        val round = _uiState.value.round ?: return
        when (type) {
            RoundEventType.COLOR_GO -> updateRound(round.copy(goActive = true, mainButtonColor = 0xFF4CAF50))
            RoundEventType.WAIT_GO -> {
                updateRound(round.copy(goActive = true, instruction = UiText.Resource(R.string.task_wait_go), mainButtonColor = 0xFF4CAF50))
                audio.play(SoundCue.SIGNAL)
            }
            RoundEventType.LAST_FINAL -> {
                inputLocks -= "task"
                updateRound(round.copy(
                    goActive = true,
                    inputLocked = inputLocks.isNotEmpty(),
                    instruction = UiText.Resource(R.string.task_last_final, listOf(colorText(round.answer)))
                ))
                audio.play(SoundCue.SIGNAL)
            }
            RoundEventType.PATIENCE_TWO -> updateRound(round.copy(statusText = UiText.Literal("2")))
            RoundEventType.PATIENCE_ONE -> updateRound(round.copy(statusText = UiText.Literal("1")))
            RoundEventType.PATIENCE_HESITATE -> updateRound(round.copy(statusText = UiText.Literal("…")))
            RoundEventType.PATIENCE_GO -> {
                updateRound(round.copy(goActive = true, instruction = UiText.Resource(R.string.task_go), statusText = UiText.Resource(R.string.task_go), mainButtonColor = 0xFF4CAF50))
                audio.play(SoundCue.SIGNAL)
            }
            RoundEventType.FLASH_REVEAL -> {
                inputLocks -= "task"
                val symbols = round.sequence.distinct().let { if (it.size < 4) (it + listOf("●", "▲", "■", "◆")).distinct().take(4) else it }
                updateRound(round.copy(
                    instruction = UiText.Resource(R.string.task_flash_repeat),
                    choices = symbols.map { TaskChoice(it, UiText.Literal(it), false) },
                    goActive = true,
                    inputLocked = inputLocks.isNotEmpty()
                ))
            }
            RoundEventType.DELAYED_REVEAL -> {
                inputLocks -= "task"
                val answer = round.answer?.toIntOrNull() ?: 1
                updateRound(round.copy(
                    instruction = UiText.Resource(R.string.task_delayed_final, listOf(answer)),
                    choices = (1..4).map { TaskChoice(it.toString(), UiText.Literal(it.toString()), it == answer) },
                    goActive = true,
                    inputLocked = inputLocks.isNotEmpty()
                ))
            }
            RoundEventType.POSITION_HIDE -> {
                inputLocks -= "task"
                updateRound(round.copy(
                    instruction = UiText.Resource(R.string.task_position_choose),
                    choices = round.choices.map { it.copy(style = ChoiceStyle.NORMAL) },
                    goActive = true,
                    inputLocked = inputLocks.isNotEmpty()
                ))
            }
            RoundEventType.RULE_SWITCH -> {
                inputLocks -= "task"
                val finalRule = round.metadata["finalRule"] ?: "largest"
                updateRound(round.copy(
                    instruction = UiText.Resource(R.string.task_rule_switch_final, listOf(ruleText(finalRule))),
                    goActive = true,
                    inputLocked = inputLocks.isNotEmpty()
                ))
                audio.play(SoundCue.SIGNAL)
            }
            RoundEventType.OPPOSITE_HIDE -> {
                inputLocks -= "task"
                updateRound(round.copy(
                    instruction = UiText.Resource(R.string.task_opposite_choose),
                    choices = round.choices.map { it.copy(style = ChoiceStyle.NORMAL) },
                    goActive = true,
                    inputLocked = inputLocks.isNotEmpty()
                ))
            }
            RoundEventType.MISSING_REVEAL -> {
                inputLocks -= "task"
                val answer = round.answer.orEmpty()
                val options = round.metadata["options"].orEmpty().split("|").filter(String::isNotEmpty)
                val remaining = round.metadata["remaining"].orEmpty().split("|").filter(String::isNotEmpty)
                updateRound(round.copy(
                    instruction = UiText.Resource(R.string.task_missing_choose),
                    choices = options.map { TaskChoice(it, UiText.Literal(it), it == answer) },
                    visualItems = remaining.map { TaskVisualItem(it) },
                    goActive = true,
                    inputLocked = inputLocks.isNotEmpty()
                ))
            }
            RoundEventType.REVERSE_REVEAL -> {
                inputLocks -= "task"
                val source = round.metadata["source"].orEmpty().split("|").filter(String::isNotEmpty)
                updateRound(round.copy(
                    instruction = UiText.Resource(R.string.task_reverse_choose),
                    choices = random.shuffle(source.distinct()).map { TaskChoice(it, UiText.Literal(it), false) },
                    visualItems = emptyList(),
                    goActive = true,
                    inputLocked = inputLocks.isNotEmpty()
                ))
            }
            RoundEventType.CHANGE_POSITIONS -> {
                inputLocks -= "task"
                var changed = random.shuffle(round.choices)
                if (changed.map { it.id } == round.choices.map { it.id } && changed.size > 1) changed = changed.drop(1) + changed.first()
                updateRound(round.copy(
                    instruction = UiText.Resource(R.string.task_changing_choose, listOf(round.answer?.toIntOrNull() ?: 0)),
                    choices = changed,
                    goActive = true,
                    inputLocked = inputLocks.isNotEmpty()
                ))
                audio.play(SoundCue.SIGNAL)
            }
            RoundEventType.DELAYED_RECALL_CUE -> success()
            RoundEventType.CLICK_SETTLE -> {
                if (round.targetClicks == 1 && round.currentClicks == 1) success()
                else if (round.targetClicks == 2 && round.currentClicks < 2) fail(UiText.Resource(R.string.failure_double))
            }
            RoundEventType.MODIFIER_UNLOCK -> { inputLocks -= "modifier"; syncRoundLock() }
            RoundEventType.RULE_UNLOCK -> { inputLocks -= "rule"; syncRoundLock() }
            RoundEventType.SWAP_CHOICES -> {
                inputLocks -= "swap"
                updateRound(round.copy(choices = random.shuffle(round.choices), inputLocked = inputLocks.isNotEmpty()))
            }
        }
    }

    fun activateMain() {
        val state = _uiState.value
        val round = state.round ?: return
        if (!acceptsInput(round)) return
        when (round.definition.id) {
            "standard", "package" -> countMainTap(round)
            "colorShift" -> if (round.goActive) success() else fail(UiText.Resource(R.string.failure_wrong_color))
            "wait" -> if (round.goActive) success() else fail(UiText.Resource(R.string.failure_wait_early))
            "patienceCountdown" -> if (round.goActive) success() else fail(UiText.Resource(R.string.failure_patience_early))
            "clickPattern" -> clickPattern(round)
        }
    }

    private fun countMainTap(round: TaskRound) {
        val clicks = round.currentClicks + 1
        when {
            round.targetClicks == 0 -> fail(UiText.Resource(R.string.failure_should_not_press))
            clicks > round.targetClicks -> fail(UiText.Resource(R.string.failure_too_many))
            clicks == round.targetClicks -> success()
            else -> updateRound(round.copy(currentClicks = clicks))
        }
    }

    private fun clickPattern(round: TaskRound) {
        val clicks = round.currentClicks + 1
        if (clicks > round.targetClicks) return fail(UiText.Resource(R.string.failure_single))
        if (round.targetClicks == 2 && clicks == 2) return success()
        updateRound(round.copy(currentClicks = clicks))
        if (clicks == 1) {
            val settle = round.events.firstOrNull { it.type == RoundEventType.CLICK_SETTLE } ?: RoundEvent(380, RoundEventType.CLICK_SETTLE)
            val record = EventRecord(settle.type, settle.delayMs)
            eventRecords += record
            armEvent(record, round.generation)
        }
    }

    fun pressStart() {
        val round = _uiState.value.round ?: return
        if (!acceptsInput(round) || round.definition.id != "hold" || holding) return
        holding = true
        holdStartedAt = SystemClock.elapsedRealtime()
        updateRound(round.copy(instruction = UiText.Resource(R.string.task_hold_wait)))
    }

    fun pressEnd(cancelled: Boolean = false) {
        val round = _uiState.value.round ?: return
        if (round.definition.id != "hold" || !holding) return
        val heldMs = SystemClock.elapsedRealtime() - holdStartedAt
        holding = false
        if (cancelled) fail(UiText.Resource(R.string.failure_cancelled))
        else if (heldMs >= round.requiredHoldMs) success()
        else fail(UiText.Resource(R.string.failure_early_release))
    }

    fun choose(id: String) {
        val round = _uiState.value.round ?: return
        if (!acceptsInput(round)) return
        val choice = round.choices.find { it.id == id && it.enabled } ?: return
        if (choice.style == ChoiceStyle.RED_DECOY && round.globalRule?.rule == GlobalRule.IGNORE_RED) {
            return fail(UiText.Resource(R.string.failure_wrong_choice))
        }
        when (round.definition.id) {
            "sequence", "reverseSequence" -> orderedChoice(round, id)
            "alphabetical" -> orderedChoice(round, id)
            "flashMemory" -> flashChoice(round, id)
            "evade" -> evadeChoice(round)
            else -> if (choice.correct) success() else fail(UiText.Resource(R.string.failure_wrong_choice))
        }
    }

    private fun orderedChoice(round: TaskRound, id: String) {
        val expected = round.sequence.getOrNull(round.progress)
        if (id != expected) return fail(UiText.Resource(R.string.failure_wrong_sequence))
        val progress = round.progress + 1
        val choices = round.choices.map { if (it.id == id) it.copy(enabled = false, selected = true) else it }
        if (progress >= round.sequence.size) success() else updateRound(round.copy(progress = progress, choices = choices))
    }

    private fun flashChoice(round: TaskRound, id: String) {
        val expected = round.sequence.getOrNull(round.progress)
        if (id != expected) return fail(UiText.Resource(R.string.failure_wrong_choice))
        val progress = round.progress + 1
        if (progress >= round.sequence.size) success() else updateRound(round.copy(progress = progress))
    }

    private fun evadeChoice(round: TaskRound) {
        val clicks = round.currentClicks + 1
        if (clicks >= round.targetClicks) return success()
        val moved = round.choices.first().copy(
            label = UiText.Literal((round.targetClicks - clicks).toString()),
            x = 0.15f + random.nextDouble().toFloat() * 0.7f,
            y = 0.15f + random.nextDouble().toFloat() * 0.7f
        )
        updateRound(round.copy(currentClicks = clicks, choices = listOf(moved)))
    }

    private fun acceptsInput(round: TaskRound): Boolean {
        val state = _uiState.value
        return state.screen == GameScreen.PLAYING && !state.paused && !state.inputLocked && !round.inputLocked && state.round?.generation == round.generation
    }

    private fun success() {
        val state = _uiState.value
        val round = state.round ?: return
        if (state.screen != GameScreen.PLAYING) return
        if (!resolutionGate.claim(round.generation)) return
        updateMemory(round)
        if (round.definition.id == "package" && packageQueue.isEmpty()) legacyRule = null
        completedTasks += 1
        val combo = state.combo + 1
        val score = state.score + TaskEngine.score(
            state.level, round.definition.difficulty, round.modifiers.size, round.globalRule != null,
            timer.remainingMs(), state.durationMs, combo
        )
        activeRule = activeRule?.let { if (it.remaining <= 1) null else it.copy(remaining = it.remaining - 1) }
        _uiState.update { it.copy(score = score, combo = combo, highestCombo = max(it.highestCombo, combo), level = it.level + 1) }
        updateRecord(state.level + 1, score)
        audio.play(SoundCue.SUCCESS)
        beginNextRound()
    }

    private fun updateMemory(round: TaskRound) {
        var updated = memory
        if (round.definition.id == "delayedRecall") {
            updated = if (round.metadata["cue"] == "true") {
                val options = round.metadata["options"].orEmpty().split("|").filter(String::isNotEmpty)
                val gap = round.metadata["gap"]?.toIntOrNull() ?: 2
                updated.copy(pendingRecall = PendingRecall(round.answer.orEmpty(), options, completedTasks + 1 + gap))
            } else updated.copy(pendingRecall = null)
        }
        val entry = TaskHistory.from(round)
        memory = updated.copy(completed = (memory.completed + entry).takeLast(16))
    }

    private fun updateRecord(reachedLevel: Int, score: Long) {
        val state = _uiState.value
        val current = if (state.mode == GameMode.SLOW) state.preferences.slowRecord else state.preferences.fastRecord
        val updated = ModeRecord(max(current.bestLevel, reachedLevel), max(current.bestScore, score))
        if (updated != current) {
            personalBest = true
            val preferences = if (state.mode == GameMode.SLOW) state.preferences.copy(slowRecord = updated) else state.preferences.copy(fastRecord = updated)
            _uiState.update { it.copy(preferences = preferences) }
            viewModelScope.launch { repository.saveRecord(state.mode, updated) }
            if (!personalBestAnnounced) {
                personalBestAnnounced = true
                showAnnouncement(UiText.Resource(R.string.new_best), 800)
                audio.play(SoundCue.BEST)
            }
        }
    }

    private fun fail(reason: UiText) {
        val state = _uiState.value
        val round = state.round ?: return
        if (!resolutionGate.claim(round.generation)) return
        timer.pause()
        pauseEvents()
        audio.play(SoundCue.FAILURE)
        updateRecord(state.level, state.score)
        val updatedState = _uiState.value
        val record = if (state.mode == GameMode.SLOW) updatedState.preferences.slowRecord else updatedState.preferences.fastRecord
        val result = ResultState(
            nickname = state.nickname.ifBlank { getApplication<Application>().getString(R.string.default_player) },
            level = state.level,
            score = state.score,
            bestLevel = max(record.bestLevel, state.level),
            bestScore = max(record.bestScore, state.score),
            durationSeconds = ((SystemClock.elapsedRealtime() - sessionStartedAt - totalPausedMs) / 1000).coerceAtLeast(0),
            highestCombo = state.highestCombo,
            failure = reason,
            taskNameRes = round.definition.nameRes,
            categoryRes = round.definition.category.labelRes,
            context = round.modifiers.map { it.labelRes } + listOfNotNull(round.globalRule?.rule?.labelRes),
            newBest = personalBest,
            sarcasmRes = listOf(R.string.sarcasm_1, R.string.sarcasm_2, R.string.sarcasm_3, R.string.sarcasm_4, R.string.sarcasm_5)[random.nextInt(0, 5)]
        )
        cleanRound()
        _uiState.update { it.copy(screen = GameScreen.RESULTS, combo = 0, paused = false, result = result, round = null, inputLocked = false) }
    }

    private fun onTimeout(expectedGeneration: Long) {
        val round = _uiState.value.round ?: return
        if (round.generation != expectedGeneration) return
        when (round.definition.id) {
            "package" -> if (round.targetClicks == 0) success() else fail(UiText.Resource(R.string.failure_timeout))
            "hold" -> fail(UiText.Resource(R.string.failure_hold_timeout))
            "wait" -> fail(UiText.Resource(R.string.failure_wait_timeout))
            "lastSecondInstruction" -> fail(UiText.Resource(R.string.failure_last_timeout))
            "patienceCountdown" -> fail(UiText.Resource(R.string.failure_patience_timeout))
            "sequence" -> fail(UiText.Resource(R.string.failure_sequence_timeout))
            "evade" -> fail(UiText.Resource(R.string.failure_evade_timeout))
            "flashMemory", "oddOneOut", "numberExtremum", "parity", "stroop", "previousMemory", "alphabetical", "fakeButton", "positionMemory", "countSymbols", "yesNo",
            "directionConflict", "mentalMath", "missingItem", "reverseSequence", "countByRule", "oppositePosition", "changingAnswer", "doubleCondition", "nBack", "previousRuleRecall" -> fail(UiText.Resource(R.string.failure_grid_timeout))
            "ruleSwitch" -> fail(UiText.Resource(R.string.failure_rule_switch))
            "delayedRecall" -> fail(UiText.Resource(R.string.failure_memory_timeout))
            "clickPattern" -> fail(UiText.Resource(if (round.targetClicks == 2) R.string.failure_double else R.string.failure_timeout))
            else -> fail(UiText.Resource(R.string.failure_timeout))
        }
    }

    private fun startTimerLoop() {
        timerJob?.cancel()
        urgencyStage = 0
        val expectedGeneration = _uiState.value.round?.generation ?: return
        timerJob = viewModelScope.launch {
            while (isActive && timer.isRunning) {
                if (_uiState.value.round?.generation != expectedGeneration) break
                val remaining = timer.remainingMs()
                _uiState.update { it.copy(remainingMs = remaining) }
                val ratio = if (_uiState.value.durationMs > 0) remaining.toDouble() / _uiState.value.durationMs else 0.0
                val stage = if (ratio <= 0.15) 2 else if (ratio <= 0.3) 1 else 0
                if (stage > urgencyStage) { urgencyStage = stage; audio.play(SoundCue.URGENCY) }
                val round = _uiState.value.round
                if (round?.definition?.id == "hold" && holding && !round.goActive && SystemClock.elapsedRealtime() - holdStartedAt >= round.requiredHoldMs) {
                    updateRound(round.copy(goActive = true, instruction = UiText.Resource(R.string.task_hold_release), mainButtonColor = 0xFF4CAF50))
                }
                if (remaining <= 0) { onTimeout(expectedGeneration); break }
                delay(33)
            }
        }
    }

    fun pauseGame() {
        val state = _uiState.value
        if (state.screen != GameScreen.PLAYING || state.paused) return
        pausedAt = SystemClock.elapsedRealtime()
        timer.pause()
        timerJob?.cancel()
        transitionJob?.cancel()
        if (!roundStarted && transitionRemainingMs > 0) {
            transitionRemainingMs = (transitionRemainingMs - (pausedAt - transitionStartedAt)).coerceAtLeast(0)
        }
        pauseEvents()
        if (holding) holdPausedAt = pausedAt
        audio.stop()
        val pausedRemaining = if (roundStarted) timer.remainingMs() else state.remainingMs
        _uiState.update { it.copy(paused = true, inputLocked = true, remainingMs = pausedRemaining) }
    }

    fun resumeGame() {
        val state = _uiState.value
        if (state.screen != GameScreen.PLAYING || !state.paused) return
        val pausedDuration = SystemClock.elapsedRealtime() - pausedAt
        totalPausedMs += pausedDuration
        if (holding && holdPausedAt > 0) holdStartedAt += pausedDuration
        _uiState.update { it.copy(paused = false, inputLocked = true) }
        if (!roundStarted) {
            val round = state.round ?: return
            armTransition(round, transitionRemainingMs.coerceAtLeast(120))
            return
        }
        viewModelScope.launch {
            delay(120)
            if (_uiState.value.screen != GameScreen.PLAYING || _uiState.value.paused) return@launch
            _uiState.update { it.copy(inputLocked = false) }
            timer.resume()
            resumeEvents()
            startTimerLoop()
        }
    }

    fun onBackground() = pauseGame()

    private fun pauseEvents() {
        val now = SystemClock.elapsedRealtime()
        eventRecords.forEach { record ->
            record.job?.cancel()
            record.remainingMs = (record.remainingMs - (now - record.startedAt)).coerceAtLeast(0)
        }
    }

    private fun resumeEvents() {
        val expected = _uiState.value.round?.generation ?: return
        eventRecords.toList().forEach { armEvent(it, expected) }
    }

    private fun announceDiscoveries(round: TaskRound, startedGlobalRule: Boolean): Long {
        val discoveries = _uiState.value.preferences.discoveries.toMutableSet()
        var transitionDelay = 180L
        val taskKey = "task:${round.definition.id}"
        if (discoveries.add(taskKey)) {
            showAnnouncement(UiText.Resource(R.string.new_mechanic, listOf(UiText.Resource(round.definition.nameRes))), 950)
            transitionDelay = max(transitionDelay, 950)
        }
        if (round.modifiers.isNotEmpty()) {
            audio.play(SoundCue.MODIFIER)
            if (!modifierDiscoveryShown && discoveries.add("system:modifiers")) {
                showAnnouncement(UiText.Resource(R.string.modifier_unlocked), 1050)
                transitionDelay = max(transitionDelay, 1050)
            }
            modifierDiscoveryShown = true
        }
        if (startedGlobalRule && round.globalRule != null) {
            audio.play(SoundCue.RULE)
            if (discoveries.add("system:rules")) {
                showAnnouncement(UiText.Resource(R.string.rule_unlocked), 1150)
                transitionDelay = max(transitionDelay, 1150)
            } else {
                showAnnouncement(UiText.Resource(R.string.rule_announce, listOf(UiText.Resource(round.globalRule.rule.labelRes))), 950)
                transitionDelay = max(transitionDelay, 950)
            }
        }
        if (discoveries != _uiState.value.preferences.discoveries) {
            _uiState.update { it.copy(preferences = it.preferences.copy(discoveries = discoveries)) }
            viewModelScope.launch { repository.setDiscoveries(discoveries) }
        }
        return transitionDelay
    }

    private fun showAnnouncement(text: UiText, durationMs: Long) {
        announcementJob?.cancel()
        _uiState.update { it.copy(announcement = text) }
        announcementJob = viewModelScope.launch { delay(durationMs); _uiState.update { it.copy(announcement = null) } }
    }

    private fun syncRoundLock() {
        val round = _uiState.value.round ?: return
        updateRound(round.copy(inputLocked = inputLocks.isNotEmpty()))
    }

    private fun updateRound(round: TaskRound) = _uiState.update { state ->
        if (state.round?.generation == round.generation) state.copy(round = round) else state
    }

    private fun cleanRound() {
        timerJob?.cancel()
        transitionJob?.cancel()
        eventRecords.forEach { it.job?.cancel() }
        eventRecords.clear()
        inputLocks.clear()
        timer.stop()
        holding = false
        roundStarted = false
        holdStartedAt = 0
        holdPausedAt = 0
        transitionRemainingMs = 0
        transitionStartedAt = 0
        resolutionGate.clear()
    }

    private fun cleanRuntime() {
        cleanRound()
        announcementJob?.cancel()
        audio.stop()
        activeRule = null
        packageQueue.clear()
        legacyRule = null
        memory = GameMemory()
    }

    private fun colorText(id: String?): UiText = UiText.Resource(when (id) {
        "red" -> R.string.color_red
        "blue" -> R.string.color_blue
        "green" -> R.string.color_green
        "yellow" -> R.string.color_yellow
        "purple" -> R.string.color_purple
        else -> R.string.color_orange
    })

    private fun ruleText(id: String): UiText = UiText.Resource(when (id) {
        "smallest" -> R.string.rule_select_smallest
        "even" -> R.string.rule_select_even
        "odd" -> R.string.rule_select_odd
        else -> R.string.rule_select_largest
    })

    override fun onCleared() {
        cleanRuntime()
        audio.release()
        super.onCleared()
    }

    private data class EventRecord(
        val type: RoundEventType,
        var remainingMs: Long,
        var startedAt: Long = 0,
        var job: Job? = null
    )
}
