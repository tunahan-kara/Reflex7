package com.tunahankara.reflex7.ui

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ReflexPanel(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(ReflexSpacing.Lg),
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier
            .shadow(24.dp, RoundedCornerShape(ReflexSpacing.PanelRadius), ambientColor = Color.Black, spotColor = Color.Black)
            .clip(RoundedCornerShape(ReflexSpacing.PanelRadius))
            .background(Brush.linearGradient(listOf(ReflexColors.PanelTop, ReflexColors.PanelBottom)))
            .border(1.dp, ReflexColors.Border, RoundedCornerShape(ReflexSpacing.PanelRadius))
            .padding(contentPadding),
        content = content
    )
}

@Composable
fun ReflexActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    kind: ReflexButtonKind = ReflexButtonKind.PRIMARY,
    enabled: Boolean = true,
    minHeight: Dp = 52.dp
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    var focused by remember { mutableStateOf(false) }
    val pressOffset by animateDpAsState(if (pressed) 4.dp else 0.dp, label = "reflex-button-press")
    val palette = when (kind) {
        ReflexButtonKind.PRIMARY -> ButtonPalette(ReflexColors.Primary, ReflexColors.PrimaryDim, ReflexColors.PrimaryBright, Color.White)
        ReflexButtonKind.FAST -> ButtonPalette(ReflexColors.Fast, ReflexColors.FastShadow, Color.Transparent, Color.White)
        ReflexButtonKind.SECONDARY -> ButtonPalette(Color(0xFF111111), Color.Black, ReflexColors.Border, ReflexColors.Ink)
        ReflexButtonKind.TERMINAL -> ButtonPalette(Color(0x8C000000), Color.Black, ReflexColors.Border, ReflexColors.Cyan)
    }
    val shape = RoundedCornerShape(if (kind == ReflexButtonKind.PRIMARY || kind == ReflexButtonKind.FAST) 12.dp else ReflexSpacing.ControlRadius)
    val lift = if (kind == ReflexButtonKind.PRIMARY || kind == ReflexButtonKind.FAST) 6.dp else 0.dp
    Box(modifier.height(minHeight + lift)) {
        if (lift > 0.dp) {
            Box(Modifier.fillMaxWidth().height(minHeight).offset(y = lift).clip(RoundedCornerShape(12.dp)).background(palette.shadow))
        }
        Box(
            Modifier
                .fillMaxWidth()
                .height(minHeight)
                .offset { IntOffset(0, pressOffset.roundToPx()) }
                .onFocusChanged { focused = it.isFocused }
                .clip(shape)
                .background(palette.background.copy(alpha = if (enabled) 1f else .55f))
                .then(
                    when {
                        focused -> Modifier.border(3.dp, ReflexColors.Warning, shape)
                        palette.border != Color.Transparent -> Modifier.border(1.dp, palette.border, shape)
                        else -> Modifier
                    }
                )
                .semantics { role = Role.Button }
                .clickable(interactionSource = interaction, indication = null, enabled = enabled, onClick = onClick)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text, color = palette.ink.copy(alpha = if (enabled) 1f else .7f), fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        }
    }
}

enum class ReflexButtonKind { PRIMARY, FAST, SECONDARY, TERMINAL }
private data class ButtonPalette(val background: Color, val shadow: Color, val border: Color, val ink: Color)

@Composable
fun ReflexToggle(text: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    var focused by remember { mutableStateOf(false) }
    Box(
        modifier
            .heightIn(min = 40.dp)
            .onFocusChanged { focused = it.isFocused }
            .clip(RoundedCornerShape(5.dp))
            .background(if (selected) ReflexColors.Primary.copy(alpha = .14f) else Color.Transparent)
            .border(if (focused) 3.dp else 1.dp, if (focused) ReflexColors.Warning else if (selected) ReflexColors.Primary else Color.Transparent, RoundedCornerShape(5.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) { Text(text, color = if (selected) Color.White else Color(0xFFAAAAAA), fontFamily = TerminalFamily, fontSize = 19.sp) }
}

@Composable
fun ReflexTextControl(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    ReflexActionButton(text, onClick, modifier, ReflexButtonKind.TERMINAL, minHeight = 40.dp)
}

@Composable
fun ReflexTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = TextStyle(fontFamily = InterfaceFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp)
) {
    var focused by remember { mutableStateOf(false) }
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = textStyle.copy(color = ReflexColors.Primary),
        cursorBrush = SolidColor(ReflexColors.Warning),
        visualTransformation = VisualTransformation.None,
        modifier = modifier
            .heightIn(min = 52.dp)
            .onFocusChanged { focused = it.isFocused }
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Black)
            .border(if (focused) 3.dp else 2.dp, if (focused) ReflexColors.Warning else ReflexColors.Primary, RoundedCornerShape(12.dp))
            .padding(horizontal = 15.dp, vertical = 14.dp),
        decorationBox = { inner ->
            Box(contentAlignment = Alignment.CenterStart) {
                if (value.isEmpty()) Text(placeholder, color = Color(0xFF777777), style = textStyle)
                inner()
            }
        }
    )
}

@Composable
fun ReflexBadge(text: String, color: Color, modifier: Modifier = Modifier) {
    Text(
        text,
        modifier = modifier.padding(horizontal = 3.dp, vertical = 1.dp),
        color = color,
        fontFamily = TerminalFamily,
        fontSize = 15.sp,
        letterSpacing = .4.sp,
        textAlign = TextAlign.Center
    )
}
