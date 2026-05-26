package off.kys.libre2048.ui.common

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import off.kys.libre2048.domain.model.Direction
import off.kys.libre2048.domain.model.GameState
import off.kys.libre2048.domain.model.Tile
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun ScoreCard(
    label: String,
    score: Int,
    modifier: Modifier = Modifier,
    icon: Painter? = null
) {
    val animatedScore by animateIntAsState(
        targetValue = score,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "ScoreAnimation"
    )

    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .widthIn(min = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            icon?.let {
                Icon(
                    painter = it,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.sp
            )

            Text(
                text = animatedScore.toString(),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun GameBoard(
    grid: List<List<Tile?>>,
    rows: Int,
    cols: Int,
    onMove: (Direction) -> Unit,
    modifier: Modifier = Modifier
) {
    var totalDragX by remember { mutableFloatStateOf(0f) }
    var totalDragY by remember { mutableFloatStateOf(0f) }
    var hasMovedThisGesture by remember { mutableStateOf(false) }
    val threshold = with(LocalDensity.current) { 50.dp.toPx() }

    val gap = 4.dp
    val outerPadding = 8.dp

    BoxWithConstraints(
        modifier = modifier
            .aspectRatio(cols.toFloat() / rows.toFloat())
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(outerPadding)
            .pointerInput(rows, cols) {
                detectDragGestures(
                    onDragStart = { totalDragX = 0f; totalDragY = 0f; hasMovedThisGesture = false },
                    onDragEnd = { hasMovedThisGesture = false },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        if (!hasMovedThisGesture) {
                            totalDragX += dragAmount.x
                            totalDragY += dragAmount.y
                            val absX = abs(totalDragX)
                            val absY = abs(totalDragY)
                            if (absX > threshold || absY > threshold) {
                                if (absX > absY) {
                                    onMove(if (totalDragX > 0) Direction.RIGHT else Direction.LEFT)
                                } else {
                                    onMove(if (totalDragY > 0) Direction.DOWN else Direction.UP)
                                }
                                hasMovedThisGesture = true
                            }
                        }
                    }
                )
            }
    ) {
        val tileSize =
            minOf((maxWidth - (gap * (cols - 1))) / cols, (maxHeight - (gap * (rows - 1))) / rows)
        val boardWidth = (tileSize * cols) + (gap * (cols - 1))
        val boardHeight = (tileSize * rows) + (gap * (rows - 1))

        Box(
            modifier = Modifier
                .size(width = boardWidth, height = boardHeight)
                .align(Alignment.Center)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(gap)) {
                repeat(rows) {
                    Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
                        repeat(cols) {
                            Box(
                                modifier = Modifier
                                    .size(tileSize)
                                    .background(
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                                        RoundedCornerShape(4.dp)
                                    )
                            )
                        }
                    }
                }
            }

            grid.flatten().filterNotNull().forEach { tile ->
                androidx.compose.runtime.key(tile.id) {
                    AnimatedTileItem(tile = tile, tileSize = tileSize, gap = gap)
                }
            }
        }
    }
}

@Composable
fun AnimatedTileItem(tile: Tile, tileSize: Dp, gap: Dp) {
    val xOffset by animateDpAsState(
        targetValue = (tileSize + gap) * tile.x,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "xOffset"
    )
    val yOffset by animateDpAsState(
        targetValue = (tileSize + gap) * tile.y,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "yOffset"
    )

    val (targetBg, targetText) = getTileColors(tile.value, MaterialTheme.colorScheme)
    val animatedBg by animateColorAsState(
        targetValue = targetBg,
        animationSpec = tween(durationMillis = 150),
        label = "bgColor"
    )
    val animatedText by animateColorAsState(
        targetValue = targetText,
        animationSpec = tween(durationMillis = 150),
        label = "textColor"
    )

    val fontSize = when {
        tile.value < 100 -> 30.sp
        tile.value < 1000 -> 24.sp
        tile.value < 10000 -> 20.sp
        else -> 16.sp
    }

    val scale = remember { Animatable(1f) }
    val lastValue = remember { mutableIntStateOf(tile.value) }

    LaunchedEffect(tile.id, tile.value) {
        if (scale.value == 1f && tile.value == lastValue.intValue) {
            scale.snapTo(0f)
            scale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
        } else if (tile.value != lastValue.intValue) {
            lastValue.intValue = tile.value
            scale.animateTo(
                targetValue = 1.2f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessHigh
                )
            )
            scale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
        }
    }

    Box(
        modifier = Modifier
            .offset { IntOffset(xOffset.toPx().roundToInt(), yOffset.toPx().roundToInt()) }
            .size(tileSize)
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
                shape = RoundedCornerShape(4.dp)
                clip = true
            }
            .background(animatedBg),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = tile.value.toString(),
            fontSize = fontSize,
            fontWeight = FontWeight.ExtraBold,
            color = animatedText,
            maxLines = 1,
            softWrap = false
        )
    }
}

@Composable
fun GameBoardPreview(
    rows: Int,
    cols: Int,
    modifier: Modifier = Modifier,
    gameState: GameState? = null
) {
    val theme = MaterialTheme.colorScheme
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val containerBgColor = theme.surfaceVariant.copy(alpha = 0.3f)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(containerBgColor)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val gapPx = 2.dp.toPx()
            val paddingPx = 4.dp.toPx()

            val availableWidth = size.width - (paddingPx * 2)
            val availableHeight = size.height - (paddingPx * 2)

            val tileSize = minOf(
                (availableWidth - (gapPx * (cols - 1))) / cols,
                (availableHeight - (gapPx * (rows - 1))) / rows
            )

            val startX =
                paddingPx + (availableWidth - ((tileSize * cols) + (gapPx * (cols - 1)))) / 2f
            val startY =
                paddingPx + (availableHeight - ((tileSize * rows) + (gapPx * (rows - 1)))) / 2f
            val cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())

            for (r in 0 until rows) {
                for (c in 0 until cols) {
                    val tile = gameState?.grid?.getOrNull(r)?.getOrNull(c)
                    val (tileBg, tileTextColor) = getTileColors(tile?.value, theme)

                    val xOffset = startX + c * (tileSize + gapPx)
                    val yOffset = startY + r * (tileSize + gapPx)

                    drawRoundRect(
                        color = tileBg,
                        topLeft = Offset(xOffset, yOffset),
                        size = Size(tileSize, tileSize),
                        cornerRadius = cornerRadius
                    )

                    if (tile != null) {
                        val textString = tile.value.toString()

                        val maxTextWidth = tileSize * 0.85f
                        val baseFontSize = tileSize * 0.4f

                        var textStyle = TextStyle(
                            color = tileTextColor,
                            fontSize = with(density) { baseFontSize.toSp() },
                            fontWeight = FontWeight.Bold
                        )
                        var textLayoutResult = textMeasurer.measure(textString, textStyle)

                        if (textLayoutResult.size.width > maxTextWidth && textString.isNotEmpty()) {
                            val scaleFactor = maxTextWidth / textLayoutResult.size.width
                            val shrunkFontSize = baseFontSize * scaleFactor

                            textStyle = textStyle.copy(
                                fontSize = with(density) { shrunkFontSize.toSp() }
                            )
                            textLayoutResult = textMeasurer.measure(textString, textStyle)
                        }

                        drawText(
                            textMeasurer = textMeasurer,
                            text = textString,
                            style = textStyle,
                            topLeft = Offset(
                                xOffset + (tileSize - textLayoutResult.size.width) / 2f,
                                yOffset + (tileSize - textLayoutResult.size.height) / 2f
                            )
                        )
                    }
                }
            }
        }
    }
}

private fun getTileColors(value: Int?, theme: ColorScheme): Pair<Color, Color> = when (value) {
    null -> theme.onSurface.copy(alpha = 0.1f) to Color.Transparent
    2 -> theme.primaryContainer to theme.onPrimaryContainer
    4 -> theme.secondaryContainer to theme.onSecondaryContainer
    8 -> theme.tertiaryContainer to theme.onTertiaryContainer
    16 -> theme.primary to theme.onPrimary
    32 -> theme.secondary to theme.onSecondary
    64 -> theme.tertiary to theme.onTertiary
    128 -> theme.errorContainer to theme.onErrorContainer
    256 -> theme.inversePrimary to theme.primary
    512 -> theme.outlineVariant to theme.outline
    1024 -> theme.scrim to theme.inverseOnSurface
    2048 -> theme.error to theme.onError
    else -> theme.onSurface to theme.surface
}