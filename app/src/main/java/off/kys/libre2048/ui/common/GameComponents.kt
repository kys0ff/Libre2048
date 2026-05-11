package off.kys.libre2048.ui.common

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import off.kys.libre2048.domain.model.Direction
import off.kys.libre2048.domain.model.Tile
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
        tonalElevation = 4.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .widthIn(min = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (icon != null) {
                Icon(
                    painter = icon,
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

    BoxWithConstraints(
        modifier = modifier
            .aspectRatio(cols.toFloat() / rows.toFloat())
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(8.dp)
            .pointerInput(rows, cols) {
                detectDragGestures(
                    onDragStart = { totalDragX = 0f; totalDragY = 0f; hasMovedThisGesture = false },
                    onDragEnd = { hasMovedThisGesture = false },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        if (!hasMovedThisGesture) {
                            totalDragX += dragAmount.x
                            totalDragY += dragAmount.y
                            val absX = kotlin.math.abs(totalDragX)
                            val absY = kotlin.math.abs(totalDragY)
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
        val gap = 4.dp
        val tileSize = (maxWidth - (gap * (cols - 1))) / cols

        // Draw the empty background grid
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

        // Draw the actual moving tiles
        grid.flatten().filterNotNull().forEach { tile ->
            key(tile.id) {
                AnimatedTileItem(tile = tile, tileSize = tileSize, gap = gap)
            }
        }
    }
}

@Composable
fun AnimatedTileItem(tile: Tile, tileSize: Dp, gap: Dp) {
    val density = LocalDensity.current

    val xOffset by animateDpAsState(
        targetValue = (tileSize + gap) * tile.x,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = 300f),
        label = "xOffset"
    )
    val yOffset by animateDpAsState(
        targetValue = (tileSize + gap) * tile.y,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = 300f),
        label = "yOffset"
    )

    val (targetBg, targetText) = getTileColors(tile.value)
    val animatedBg by animateColorAsState(targetValue = targetBg, label = "bgColor")
    val animatedText by animateColorAsState(targetValue = targetText, label = "textColor")

    val scale = remember { Animatable(0f) }

    LaunchedEffect(tile.id) {
        if (scale.value == 0f) {
            scale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
        }
    }

    LaunchedEffect(tile.value) {
        if (scale.value >= 0.9f) {
            scale.animateTo(1.15f, spring(stiffness = Spring.StiffnessHigh))
            scale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
        }
    }

    Box(
        modifier = Modifier
            .offset { IntOffset(xOffset.toPx().roundToInt(), yOffset.toPx().roundToInt()) }
            .size(tileSize)
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
                shadowElevation = with(density) { 2.dp.toPx() }
                shape = RoundedCornerShape(4.dp)
                clip = true
            }
            .background(animatedBg),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = tile.value.toString(),
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = animatedText
        )
    }
}

@Composable
fun GameBoardPreview(
    rows: Int,
    cols: Int,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier
            .aspectRatio(cols.toFloat() / rows.toFloat())
            .clip(RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(4.dp)
    ) {
        val gap = 2.dp
        val tileSize = (maxWidth - (gap * (cols - 1))) / cols

        Column(verticalArrangement = Arrangement.spacedBy(gap)) {
            repeat(rows) {
                Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
                    repeat(cols) {
                        Box(
                            modifier = Modifier
                                .size(tileSize)
                                .background(
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                                    RoundedCornerShape(2.dp)
                                )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun getTileColors(value: Int?): Pair<Color, Color> {
    val theme = MaterialTheme.colorScheme
    return when (value) {
        null -> theme.surfaceVariant.copy(alpha = 0.3f) to Color.Transparent
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
}