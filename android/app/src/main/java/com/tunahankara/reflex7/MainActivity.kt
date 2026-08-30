package com.tunahankara.reflex7

import android.os.Bundle
import android.provider.Settings
import android.view.KeyEvent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.LocaleListCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tunahankara.reflex7.engine.*
import com.tunahankara.reflex7.ui.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlin.math.sin

class MainActivity : AppCompatActivity() {
    private var immersiveGameplay = false

    override fun onCreate(savedInstanceState: Bundle?) {
        val app = application as Reflex7Application
        val language = runBlocking { app.preferencesRepository.preferences.first().language }
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(language))
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
        }
        setContent {
            Reflex7Theme {
                val vm: GameViewModel = viewModel()
                val state by vm.uiState.collectAsStateWithLifecycle()
                LaunchedEffect(state.screen) { setGameplayImmersive(state.screen == GameScreen.PLAYING) }
                val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
                DisposableEffect(lifecycleOwner) {
                    val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_STOP) vm.onBackground() }
                    lifecycleOwner.lifecycle.addObserver(observer)
                    onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
                }
                Reflex7App(state, vm) { selected ->
                    vm.setLanguage(selected)
                    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(selected))
                }
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus && immersiveGameplay) applySystemBars(true)
    }

    private fun setGameplayImmersive(enabled: Boolean) {
        immersiveGameplay = enabled
        applySystemBars(enabled)
    }

    private fun applySystemBars(hidden: Boolean) {
        WindowCompat.getInsetsController(window, window.decorView).apply {
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            if (hidden) hide(WindowInsetsCompat.Type.systemBars()) else show(WindowInsetsCompat.Type.systemBars())
        }
    }
}

@Composable
private fun Reflex7App(state: GameUiState, vm: GameViewModel, setLanguage: (String) -> Unit) {
    val reducedMotion = rememberReducedMotion()
    BackHandler(enabled = state.screen != GameScreen.MENU || state.showHowTo) {
        when {
            state.showHowTo -> vm.showHowTo(false)
            state.screen == GameScreen.PLAYING && !state.paused -> vm.pauseGame()
            state.screen == GameScreen.PLAYING -> vm.returnToMenu()
            else -> vm.returnToMenu()
        }
    }
    BoxWithConstraints(Modifier.fillMaxSize().background(ReflexColors.Page)) {
        ReflexBackground(compact = maxWidth <= 340.dp)
        Box(Modifier.fillMaxSize().safeDrawingPadding()) {
            when (state.screen) {
                GameScreen.MENU -> MenuScreen(state, vm, setLanguage)
                GameScreen.PLAYING -> GameScreenContent(state, vm, reducedMotion)
                GameScreen.RESULTS -> ResultsScreen(state, vm, setLanguage)
            }
        }
        if (!reducedMotion) Scanlines()
        Vignette()
        state.announcement?.let { Announcement(resolve(it)) }
        if (state.paused) PauseOverlay(vm)
        if (state.showOnboarding) OnboardingOverlay(vm::dismissOnboarding)
    }
}

@Composable
private fun ReflexBackground(compact: Boolean) {
    if (!compact) {
        Image(
            painterResource(R.drawable.reflex7_bg), null,
            Modifier.fillMaxSize().alpha(.26f),
            contentScale = ContentScale.Crop,
            alignment = Alignment.Center
        )
    } else {
        Canvas(Modifier.fillMaxSize()) {
            drawRect(
                Brush.radialGradient(
                    listOf(Color(0x33207835), Color.Transparent),
                    center = Offset(size.width * .75f, size.height * .30f),
                    radius = size.minDimension * .95f
                )
            )
        }
    }
}

@Composable
private fun MenuScreen(state: GameUiState, vm: GameViewModel, setLanguage: (String) -> Unit) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val compactHeight = maxHeight < 640.dp
        val compactWidth = maxWidth <= 380.dp
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = if (compactHeight) 6.dp else 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = if (compactHeight) Arrangement.Top else Arrangement.Center
        ) {
            BrandLockup(Modifier.widthIn(max = 344.dp).fillMaxWidth())
            Spacer(Modifier.height(if (compactHeight) 6.dp else 12.dp))
            ReflexPanel(Modifier.widthIn(max = 344.dp).fillMaxWidth(), PaddingValues(if (compactWidth) 14.dp else 18.dp)) {
                Column {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        LanguageSelector(state.preferences.language, setLanguage)
                        ReflexTextControl(stringResource(if (state.preferences.soundEnabled) R.string.sound_on else R.string.sound_off), vm::toggleSound)
                    }
                    Text(
                        stringResource(
                            R.string.record_summary,
                            state.preferences.slowRecord.bestLevel, state.preferences.slowRecord.bestScore,
                            state.preferences.fastRecord.bestLevel, state.preferences.fastRecord.bestScore
                        ),
                        color = ReflexColors.Ink,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        lineHeight = 22.sp,
                        modifier = Modifier.padding(vertical = 14.dp)
                    )
                    Text(stringResource(R.string.menu_intro), color = ReflexColors.Ink, fontFamily = TerminalFamily, fontSize = 20.sp, lineHeight = 24.sp)
                    Spacer(Modifier.height(16.dp))
                    Text(stringResource(R.string.nickname_label).uppercase() + ":", color = ReflexColors.Ink, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(Modifier.height(7.dp))
                    ReflexTextField(state.nickname, vm::updateNickname, stringResource(R.string.nickname_placeholder), Modifier.fillMaxWidth())
                    Spacer(Modifier.height(10.dp))
                    ReflexActionButton(stringResource(R.string.slow_mode), { vm.startGame(GameMode.SLOW) }, Modifier.fillMaxWidth(), ReflexButtonKind.PRIMARY, minHeight = 64.dp)
                    Spacer(Modifier.height(10.dp))
                    ReflexActionButton(stringResource(R.string.fast_mode), { vm.startGame(GameMode.FAST) }, Modifier.fillMaxWidth(), ReflexButtonKind.FAST, minHeight = 64.dp)
                    Spacer(Modifier.height(12.dp))
                    ReflexTextControl(stringResource(R.string.how_to_play), { vm.showHowTo(!state.showHowTo) }, Modifier.fillMaxWidth())
                    if (state.showHowTo) InlineHowTo { vm.showHowTo(false) }
                }
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun BrandLockup(modifier: Modifier = Modifier) {
    Column(modifier, horizontalAlignment = Alignment.Start) {
        Box {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(stringResource(R.string.brand_word), color = ReflexColors.Ink, fontFamily = TerminalFamily, fontSize = 55.sp, letterSpacing = 3.sp, lineHeight = 48.sp)
                Text("7", color = ReflexColors.Primary, fontFamily = TerminalFamily, fontSize = 67.sp, lineHeight = 54.sp, modifier = Modifier.graphicsLayer(rotationZ = -2f))
            }
            Text("R7", color = ReflexColors.Primary, fontFamily = TerminalFamily, fontSize = 10.sp, letterSpacing = 2.sp, modifier = Modifier.offset(x = 8.dp, y = (-6).dp))
        }
        Text(stringResource(R.string.brand_tagline), color = ReflexColors.Muted, fontFamily = TerminalFamily, fontSize = 16.sp, letterSpacing = 3.sp)
    }
}

@Composable
private fun LanguageSelector(language: String, setLanguage: (String) -> Unit, modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        ReflexToggle(stringResource(R.string.language_tr), language == "tr", { setLanguage("tr") })
        Text("/", color = Color(0xFF777777), fontFamily = TerminalFamily, fontSize = 19.sp)
        ReflexToggle(stringResource(R.string.language_en), language == "en", { setLanguage("en") })
    }
}

@Composable
private fun InlineHowTo(close: () -> Unit) {
    Column(Modifier.fillMaxWidth().padding(top = 12.dp).border(width = 1.dp, color = ReflexColors.BorderMuted, shape = RoundedCornerShape(1.dp)).padding(12.dp)) {
        Text(stringResource(R.string.how_to_play), color = ReflexColors.Primary, fontFamily = TerminalFamily, fontSize = 25.sp, letterSpacing = 2.sp)
        Spacer(Modifier.height(10.dp))
        Text(stringResource(R.string.how_to_intro), color = Color(0xFFCED8CF))
        Spacer(Modifier.height(8.dp))
        Bullet(stringResource(R.string.how_to_instructions))
        Bullet(stringResource(R.string.how_to_controls))
        Bullet(stringResource(R.string.how_to_local))
        Spacer(Modifier.height(10.dp))
        ReflexTextControl(stringResource(R.string.close), close)
    }
}

@Composable private fun Bullet(text: String) {
    Row(Modifier.padding(vertical = 3.dp)) { Text("•", color = ReflexColors.Ink); Spacer(Modifier.width(8.dp)); Text(text, color = Color(0xFFCED8CF), modifier = Modifier.weight(1f)) }
}

@Composable
private fun GameScreenContent(state: GameUiState, vm: GameViewModel, reducedMotion: Boolean) {
    val round = state.round
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val arenaSize = minOf(maxWidth * .86f, maxHeight * .62f, 320.dp)
        Column(
            Modifier.align(Alignment.Center).width(arenaSize),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Indicators(round)
            SessionHud(state, vm)
            Spacer(Modifier.height(4.dp))
            if (round == null) {
                Box(Modifier.size(arenaSize), contentAlignment = Alignment.Center) { Text("…", color = ReflexColors.Primary, fontFamily = TerminalFamily, fontSize = 42.sp) }
            } else {
                TaskArena(state, round, vm, reducedMotion, arenaSize)
            }
        }
    }
}

@Composable
private fun Indicators(round: TaskRound?) {
    val context = LocalContext.current
    Column(Modifier.fillMaxWidth().heightIn(min = 22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        round?.globalRule?.let { ReflexBadge(stringResource(R.string.rule_badge, it.remaining, stringResource(it.rule.labelRes)), ReflexColors.Warning) }
        if (!round?.modifiers.isNullOrEmpty()) {
            ReflexBadge(round!!.modifiers.joinToString(" · ") { context.getString(it.labelRes) }, ReflexColors.Cyan)
        }
    }
}

@Composable
private fun SessionHud(state: GameUiState, vm: GameViewModel) {
    Column(Modifier.fillMaxWidth().padding(bottom = 4.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.level_format, state.level), color = ReflexColors.Ink, fontFamily = TerminalFamily, fontSize = 18.sp, letterSpacing = 1.sp)
            Text(stringResource(R.string.score_format, state.score), color = ReflexColors.Ink, fontFamily = TerminalFamily, fontSize = 18.sp, letterSpacing = 1.sp)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(if (state.combo > 0) stringResource(R.string.combo_format, state.combo) else " ", color = ReflexColors.Warning, fontFamily = TerminalFamily, fontSize = 17.sp)
            ReflexTextControl("Ⅱ", vm::pauseGame, Modifier.width(44.dp))
        }
    }
}

@Composable
private fun TaskArena(state: GameUiState, round: TaskRound, vm: GameViewModel, reducedMotion: Boolean, size: Dp) {
    val progress = if (state.durationMs > 0) (state.remainingMs.toFloat() / state.durationMs).coerceIn(0f, 1f) else 0f
    val urgent = progress <= .30f
    val critical = progress <= .15f
    val pulse = if (critical && !reducedMotion) .68f + .32f * kotlin.math.abs(sin((state.remainingMs / 160.0))).toFloat() else 1f
    Box(Modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(Modifier.matchParentSize().alpha(pulse)) {
            val stroke = if (urgent) 5.dp.toPx() else 3.dp.toPx()
            drawCircle(ReflexColors.TimerTrack, style = Stroke(3.dp.toPx()))
            drawArc(
                if (critical) ReflexColors.Danger else ReflexColors.Primary,
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                style = Stroke(stroke, cap = StrokeCap.Round, pathEffect = if (urgent) PathEffect.dashPathEffect(floatArrayOf(18f, 7f)) else null)
            )
        }
        if (urgent) Text(if (critical) "!!" else "!", color = ReflexColors.Warning, fontFamily = TerminalFamily, fontSize = 24.sp, modifier = Modifier.align(Alignment.TopEnd).padding(22.dp))
        if (round.mainButtonVisible) MainTaskButton(state, round, vm, reducedMotion)
        else GridTaskPresentation(state, round, vm, reducedMotion)
    }
}

@Composable
private fun MainTaskButton(state: GameUiState, round: TaskRound, vm: GameViewModel, reducedMotion: Boolean) {
    val focusRequester = remember { FocusRequester() }
    var focused by remember { mutableStateOf(false) }
    val locked = state.inputLocked || round.inputLocked
    val isHold = round.definition.id == "hold"
    val goSignal = round.goActive && round.definition.id in setOf("wait", "patienceCountdown")
    val phase = if (reducedMotion || state.paused) 0f else sin((state.durationMs - state.remainingMs) / 260.0).toFloat()
    val base = Modifier
        .offset(
            x = if (com.tunahankara.reflex7.engine.Modifier.MOVING in round.modifiers) (phase * 10).dp else 0.dp,
            y = if (com.tunahankara.reflex7.engine.Modifier.MOVING in round.modifiers) (-phase * 8).dp else 0.dp
        )
        .scale(if (com.tunahankara.reflex7.engine.Modifier.SHRINKING in round.modifiers) .86f else 1f)
        .fillMaxSize(.75f)
        .focusRequester(focusRequester)
        .onFocusChanged { focused = it.isFocused }
        .onKeyEvent { event ->
            if (event.nativeKeyEvent.keyCode !in listOf(KeyEvent.KEYCODE_SPACE, KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_DPAD_CENTER)) return@onKeyEvent false
            if (isHold) {
                if (event.nativeKeyEvent.action == KeyEvent.ACTION_DOWN && event.nativeKeyEvent.repeatCount == 0) vm.pressStart()
                if (event.nativeKeyEvent.action == KeyEvent.ACTION_UP) vm.pressEnd()
            } else if (event.nativeKeyEvent.action == KeyEvent.ACTION_UP) vm.activateMain()
            true
        }
        .focusable()
    val color = Color(round.mainButtonColor)
    val visual = base
        .drawBehind {
            drawCircle(ReflexColors.PrimaryDim, radius = size.minDimension / 2, center = center + Offset(0f, 9.dp.toPx()))
            if (goSignal) {
                drawCircle(ReflexColors.Warning.copy(alpha = .78f), radius = size.minDimension / 2 + 8.dp.toPx(), style = Stroke(4.dp.toPx()))
                drawCircle(Color.White, radius = size.minDimension / 2 - 4.dp.toPx(), style = Stroke(5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(13f, 6f))))
            }
            if (focused) drawCircle(ReflexColors.Warning, radius = size.minDimension / 2 - 3.dp.toPx(), style = Stroke(3.dp.toPx()))
        }
        .clip(CircleShape)
        .background(color.copy(alpha = if (locked) .68f else 1f))
    val pointer = if (isHold) visual.pointerInput(round.generation, locked) {
        detectTapGestures(onPress = {
            if (!locked) vm.pressStart()
            val released = tryAwaitRelease()
            if (!locked) vm.pressEnd(cancelled = !released)
        })
    } else visual.clickable(enabled = !locked, onClick = vm::activateMain)
    LaunchedEffect(round.generation) { focusRequester.requestFocus() }
    Box(pointer, contentAlignment = Alignment.Center) {
        Text(
            resolve(round.instruction),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp,
            modifier = Modifier.padding(22.dp).graphicsLayer { if (com.tunahankara.reflex7.engine.Modifier.MIRRORED in round.modifiers) scaleX = -1f }
        )
    }
}

@Composable
private fun GridTaskPresentation(state: GameUiState, round: TaskRound, vm: GameViewModel, reducedMotion: Boolean) {
    val locked = state.inputLocked || round.inputLocked
    val phase = if (reducedMotion || state.paused) 0f else sin((state.durationMs - state.remainingMs) / 260.0).toFloat()
    val controls = Modifier
        .offset(x = if (com.tunahankara.reflex7.engine.Modifier.MOVING in round.modifiers) (phase * 10).dp else 0.dp,
            y = if (com.tunahankara.reflex7.engine.Modifier.MOVING in round.modifiers) (-phase * 8).dp else 0.dp)
        .scale(if (com.tunahankara.reflex7.engine.Modifier.SHRINKING in round.modifiers) .86f else 1f)
    Box(Modifier.fillMaxSize()) {
        InstructionPlate(round, Modifier.align(Alignment.TopCenter).padding(top = 8.dp))
        if (round.definition.id == "directionConflict") DirectionStatus(round, Modifier.align(Alignment.TopCenter))
        else round.statusText?.let {
            Text(resolve(it), color = if (round.goActive) ReflexColors.Warning else Color.White, fontFamily = TerminalFamily, fontSize = 48.sp,
                modifier = Modifier.align(Alignment.TopCenter).padding(top = 46.dp).background(ReflexColors.PageSoft).border(2.dp, Color.White).padding(horizontal = 10.dp, vertical = 1.dp))
        }
        if (round.visualItems.isNotEmpty()) VisualItemBoard(round.visualItems, Modifier.align(Alignment.TopCenter).padding(top = 54.dp))
        when (round.definition.id) {
            "sequence" -> PositionedTargets(round, locked, vm, controls)
            "evade" -> MovingTarget(round, locked, vm)
            else -> ChoiceGrid(round, locked, vm, controls)
        }
        if (com.tunahankara.reflex7.engine.Modifier.DECOY in round.modifiers) {
            Text(stringResource(R.string.decoy_instruction), color = Color(0xFF777777), fontSize = 11.sp, textDecoration = TextDecoration.LineThrough,
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 8.dp, start = 20.dp, end = 20.dp), textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun InstructionPlate(round: TaskRound, modifier: Modifier = Modifier) {
    val final = round.globalRule?.rule == GlobalRule.FINAL_LINE
    Column(
        modifier
            .fillMaxWidth(.86f)
            .background(Color(0xDB000000), RoundedCornerShape(9.dp))
            .border(if (final) 2.dp else 1.dp, if (final) ReflexColors.Warning else ReflexColors.Primary.copy(alpha = .55f), RoundedCornerShape(9.dp))
            .padding(horizontal = 9.dp, vertical = 7.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (final) {
            Text(
                if (round.goActive) stringResource(R.string.rule_first_superseded) else resolve(round.initialInstruction),
                color = Color(0xFF888888), fontSize = 10.sp, textDecoration = TextDecoration.LineThrough,
                textAlign = TextAlign.Center
            )
            if (!round.goActive) {
                Text(stringResource(R.string.rule_final_pending), color = ReflexColors.Warning, fontSize = 11.sp)
                return@Column
            }
        }
        Text(
            resolve(round.instruction), color = Color.White, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, lineHeight = 18.sp,
            modifier = Modifier.graphicsLayer { if (com.tunahankara.reflex7.engine.Modifier.MIRRORED in round.modifiers) scaleX = -1f }
        )
    }
}

@Composable
private fun ChoiceGrid(round: TaskRound, locked: Boolean, vm: GameViewModel, modifier: Modifier) {
    val columns = when {
        round.definition.id in setOf("positionMemory", "oppositePosition") -> 3
        round.choices.size <= 4 -> 2
        else -> 3
    }
    val topPadding = when {
        round.visualItems.isNotEmpty() -> 128.dp
        round.definition.id == "directionConflict" -> 128.dp
        round.statusText != null -> 110.dp
        else -> 62.dp
    }
    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        modifier = modifier.fillMaxSize().padding(top = topPadding, bottom = 20.dp, start = 10.dp, end = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items(round.choices, key = { it.id }) { choice -> ReflexTaskChoice(round, choice, locked) { vm.choose(choice.id) } }
    }
}

@Composable
private fun VisualItemBoard(items: List<TaskVisualItem>, modifier: Modifier = Modifier) {
    Column(
        modifier.background(ReflexColors.PageSoft, RoundedCornerShape(9.dp)).border(1.dp, ReflexColors.Cyan, RoundedCornerShape(9.dp)).padding(7.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items.chunked(6).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                row.forEach { item -> Text(item.text, color = item.color?.let(::Color) ?: Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold) }
            }
        }
    }
}

@Composable
private fun DirectionStatus(round: TaskRound, modifier: Modifier = Modifier) {
    val alignment = when (round.metadata["position"]) {
        "left" -> Alignment.CenterStart
        "right" -> Alignment.CenterEnd
        "up" -> Alignment.TopCenter
        else -> Alignment.BottomCenter
    }
    Box(modifier.padding(top = 48.dp).fillMaxWidth(.72f).height(66.dp)) {
        Text(
            resolve(round.statusText ?: UiText.Literal("?")), color = Color.White, fontFamily = TerminalFamily, fontSize = 42.sp,
            modifier = Modifier.align(alignment).background(ReflexColors.PageSoft).border(2.dp, ReflexColors.Cyan).padding(horizontal = 8.dp)
        )
    }
}

@Composable
private fun ReflexTaskChoice(round: TaskRound, choice: TaskChoice, locked: Boolean, onClick: () -> Unit) {
    var focused by remember(choice.id) { mutableStateOf(false) }
    val stroop = round.definition.id == "stroop"
    val coloredTarget = round.definition.id in setOf("lastSecondInstruction", "doubleCondition", "previousRuleRecall")
    val background = when {
        stroop -> Color(0xFF151515)
        coloredTarget && choice.color != null -> Color(choice.color)
        choice.style == ChoiceStyle.RED_DECOY -> Color(0xFF281010)
        choice.style == ChoiceStyle.FAKE -> Color(0xFF181818)
        choice.style == ChoiceStyle.MEMORY -> ReflexColors.Primary
        else -> ReflexColors.Choice
    }
    val border = when (choice.style) {
        ChoiceStyle.RED_DECOY -> ReflexColors.Danger
        ChoiceStyle.FAKE -> Color(0xFF777777)
        ChoiceStyle.MEMORY -> ReflexColors.Warning
        else -> Color(0xFF8BC34A)
    }
    val dashed = choice.style == ChoiceStyle.RED_DECOY || choice.style == ChoiceStyle.FAKE
    val ink = if (stroop && choice.color != null) Color(choice.color) else Color.White
    val shape = RoundedCornerShape(11.dp)
    Box(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .padding(bottom = 2.dp)
            .onFocusChanged { focused = it.isFocused }
            .drawBehind { drawRoundRect(ReflexColors.ChoiceShadow, topLeft = Offset(0f, 4.dp.toPx()), cornerRadius = CornerRadius(11.dp.toPx())) }
            .clip(shape)
            .background(background)
            .drawBehind {
                if (choice.style == ChoiceStyle.RED_DECOY) {
                    var x = -size.height
                    while (x < size.width) { drawLine(Color(0x55401010), Offset(x, size.height), Offset(x + size.height, 0f), 7.dp.toPx()); x += 14.dp.toPx() }
                }
                drawRoundRect(if (focused) ReflexColors.Warning else border, style = Stroke(if (focused || choice.style == ChoiceStyle.REAL) 3.dp.toPx() else 2.dp.toPx(), pathEffect = if (!focused && dashed) PathEffect.dashPathEffect(floatArrayOf(10f, 7f)) else null), cornerRadius = CornerRadius(11.dp.toPx()))
            }
            .alpha(if (choice.enabled) if (locked) .62f else 1f else .35f)
            .clickable(enabled = choice.enabled && !locked, onClick = onClick)
            .padding(horizontal = 5.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(resolve(choice.label), color = ink, fontWeight = FontWeight.Bold, fontSize = if (round.definition.id == "positionMemory") 18.sp else 20.sp, textAlign = TextAlign.Center)
        if (choice.style == ChoiceStyle.RED_DECOY) Text("×", color = Color.White, fontSize = 11.sp, modifier = Modifier.align(Alignment.TopEnd))
    }
}

@Composable
private fun PositionedTargets(round: TaskRound, locked: Boolean, vm: GameViewModel, modifier: Modifier) {
    BoxWithConstraints(modifier.fillMaxSize().padding(top = 46.dp)) {
        val seed = round.metadata["layoutSeed"]?.toLongOrNull() ?: round.generation
        val positions = remember(round.generation, maxWidth, maxHeight, seed) {
            TargetPlacement.generate(round.choices.size, maxWidth.value, maxHeight.value, SeededRandom(seed))
        }
        round.choices.zip(positions).forEach { (choice, position) ->
            var focused by remember(choice.id) { mutableStateOf(false) }
            val alignment = BiasAlignment(position.x * 2 - 1, position.y * 2 - 1)
            Box(
                Modifier.align(alignment).size(54.dp).onFocusChanged { focused = it.isFocused }.clip(CircleShape).background(ReflexColors.Primary)
                    .border(if (focused) 4.dp else 2.dp, if (focused) ReflexColors.Warning else Color.White, CircleShape)
                    .clickable(enabled = choice.enabled && !locked) { vm.choose(choice.id) }.focusable()
                    .alpha(if (choice.enabled) 1f else 0f),
                contentAlignment = Alignment.Center
            ) { Text(resolve(choice.label), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 21.sp) }
        }
    }
}

@Composable
private fun MovingTarget(round: TaskRound, locked: Boolean, vm: GameViewModel) {
    Box(Modifier.fillMaxSize().padding(top = 48.dp)) {
        round.choices.firstOrNull()?.let { choice ->
            var focused by remember(choice.id) { mutableStateOf(false) }
            val alignment = BiasAlignment(choice.x * 2 - 1, choice.y * 2 - 1)
            Box(
                Modifier.align(alignment).size(72.dp).onFocusChanged { focused = it.isFocused }.drawBehind { drawCircle(Color(0xFFD84315), center = center + Offset(0f, 6.dp.toPx())) }
                    .clip(CircleShape).background(Color(0xFFFF5722)).border(if (focused) 4.dp else 2.dp, if (focused) Color.White else ReflexColors.Warning, CircleShape)
                    .clickable(enabled = !locked) { vm.choose(choice.id) },
                contentAlignment = Alignment.Center
            ) { Text(resolve(choice.label), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp) }
        }
    }
}

@Composable
private fun ResultsScreen(state: GameUiState, vm: GameViewModel, setLanguage: (String) -> Unit) {
    val result = state.result ?: return
    val context = LocalContext.current
    val contextLabels = result.context.joinToString { context.getString(it) }
    Box(Modifier.fillMaxSize().background(ReflexColors.Overlay).padding(16.dp), contentAlignment = Alignment.Center) {
        ReflexPanel(Modifier.fillMaxWidth().widthIn(max = 560.dp).heightIn(max = 760.dp), PaddingValues(0.dp)) {
            Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 22.dp, vertical = 22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(stringResource(R.string.session_over), color = ReflexColors.Muted, fontFamily = TerminalFamily, letterSpacing = 4.sp)
                Text(stringResource(R.string.result_title), color = ReflexColors.Primary, fontFamily = TerminalFamily, fontSize = 40.sp, letterSpacing = 3.sp)
                Spacer(Modifier.height(10.dp))
                Text(resolve(result.failure), color = ReflexColors.DangerSoft, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, fontSize = 17.sp)
                Spacer(Modifier.height(10.dp))
                Text(stringResource(result.sarcasmRes), color = ReflexColors.Muted, fontFamily = TerminalFamily, fontSize = 20.sp)
                Spacer(Modifier.height(16.dp))
                StatBox(stringResource(R.string.player_stat, result.nickname))
                StatBox(stringResource(R.string.reached_level, result.level))
                StatBox(stringResource(R.string.session_score, result.score))
                StatBox(stringResource(R.string.best_level, result.bestLevel))
                StatBox(stringResource(R.string.best_score, result.bestScore))
                StatBox(stringResource(R.string.session_time, result.durationSeconds))
                StatBox(stringResource(R.string.highest_combo, result.highestCombo))
                Spacer(Modifier.height(10.dp))
                Column(Modifier.fillMaxWidth().border(1.dp, ReflexColors.BorderMuted).background(Color(0x47000000)).padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(stringResource(R.string.failed_task, stringResource(result.taskNameRes), stringResource(result.categoryRes)), color = ReflexColors.Ink, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(5.dp))
                    Text(if (result.context.isEmpty()) stringResource(R.string.no_active_context) else stringResource(R.string.active_context, contextLabels), color = ReflexColors.Muted, textAlign = TextAlign.Center)
                }
                if (result.newBest) { Spacer(Modifier.height(12.dp)); Text(stringResource(R.string.new_best), color = ReflexColors.Warning, fontFamily = TerminalFamily, fontSize = 23.sp) }
                Spacer(Modifier.height(20.dp))
                ReflexActionButton(stringResource(R.string.retry), vm::retry, Modifier.fillMaxWidth(), ReflexButtonKind.PRIMARY)
                Spacer(Modifier.height(9.dp))
                ReflexActionButton(stringResource(R.string.main_menu), vm::returnToMenu, Modifier.fillMaxWidth(), ReflexButtonKind.SECONDARY)
                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    LanguageSelector(state.preferences.language, setLanguage)
                    ReflexTextControl(stringResource(if (state.preferences.soundEnabled) R.string.sound_on else R.string.sound_off), vm::toggleSound)
                }
            }
        }
    }
}

@Composable private fun StatBox(text: String) {
    Box(Modifier.fillMaxWidth().padding(vertical = 4.dp).border(1.dp, ReflexColors.BorderMuted).background(Color(0x47000000)).padding(12.dp), contentAlignment = Alignment.Center) { Text(text, color = ReflexColors.Ink, textAlign = TextAlign.Center) }
}

@Composable
private fun PauseOverlay(vm: GameViewModel) {
    TerminalOverlay {
        Text(stringResource(R.string.paused), color = ReflexColors.Primary, fontFamily = TerminalFamily, fontSize = 37.sp, letterSpacing = 3.sp)
        Spacer(Modifier.height(18.dp))
        Text(stringResource(R.string.pause_body), color = ReflexColors.Ink, textAlign = TextAlign.Center)
        Spacer(Modifier.height(18.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            ReflexActionButton(stringResource(R.string.resume), vm::resumeGame, Modifier.weight(1f), ReflexButtonKind.PRIMARY)
            ReflexActionButton(stringResource(R.string.main_menu), vm::returnToMenu, Modifier.weight(1f), ReflexButtonKind.SECONDARY)
        }
    }
}

@Composable
private fun OnboardingOverlay(close: () -> Unit) {
    TerminalOverlay {
        Text(stringResource(R.string.first_tip_title), color = ReflexColors.Primary, fontFamily = TerminalFamily, fontSize = 34.sp, letterSpacing = 3.sp, textAlign = TextAlign.Center)
        Spacer(Modifier.height(14.dp))
        Text(stringResource(R.string.first_tip_body), color = ReflexColors.Ink, textAlign = TextAlign.Center)
        Spacer(Modifier.height(16.dp))
        ReflexActionButton(stringResource(R.string.got_it), close, Modifier.widthIn(min = 130.dp).align(Alignment.CenterHorizontally), ReflexButtonKind.PRIMARY)
    }
}

@Composable
private fun TerminalOverlay(content: @Composable ColumnScope.() -> Unit) {
    Box(Modifier.fillMaxSize().background(ReflexColors.Overlay).padding(16.dp), contentAlignment = Alignment.Center) {
        ReflexPanel(Modifier.fillMaxWidth().widthIn(max = 560.dp), PaddingValues(horizontal = 24.dp, vertical = 28.dp)) {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, content = content)
        }
    }
}

@Composable
private fun Announcement(text: String) {
    Box(Modifier.fillMaxWidth().safeDrawingPadding().padding(horizontal = 16.dp, vertical = 8.dp), contentAlignment = Alignment.TopCenter) {
        Box(
            Modifier.fillMaxWidth().widthIn(max = 420.dp).background(Color(0xE0000000), RoundedCornerShape(9.dp))
                .border(1.dp, ReflexColors.Primary, RoundedCornerShape(9.dp)).padding(horizontal = 12.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) { Text(text, color = ReflexColors.Warning, fontFamily = TerminalFamily, fontSize = 19.sp, textAlign = TextAlign.Center) }
    }
}

@Composable
private fun resolve(text: UiText): String = when (text) {
    is UiText.Literal -> text.value
    is UiText.Resource -> stringResource(text.id, *text.args.map { if (it is UiText) resolve(it) else it }.toTypedArray())
}

@Composable
private fun rememberReducedMotion(): Boolean {
    val context = LocalContext.current
    return remember(context) { Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f }
}

@Composable
private fun Scanlines() {
    Canvas(Modifier.fillMaxSize().alpha(.14f)) {
        var y = 3f
        while (y < size.height) { drawLine(Color.Black.copy(alpha = .34f), Offset(0f, y), Offset(size.width, y), 1f); y += 4f }
    }
}

@Composable
private fun Vignette() {
    Canvas(Modifier.fillMaxSize()) {
        drawRect(Brush.radialGradient(listOf(Color.Transparent, Color.Black.copy(alpha = .72f)), center = center, radius = size.maxDimension * .72f))
    }
}
