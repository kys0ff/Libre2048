package off.kys.libre2048.ui.game

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import off.kys.libre2048.R
import off.kys.libre2048.di.appModule
import off.kys.libre2048.domain.model.GameMode
import off.kys.libre2048.domain.model.GameState
import off.kys.libre2048.ui.common.GameBoard
import off.kys.libre2048.ui.common.ScoreCard
import off.kys.libre2048.ui.stats.StatisticsScreen
import off.kys.libre2048.ui.theme.Libre2048Theme
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.KoinApplication
import org.koin.dsl.koinConfiguration

class Game2048Screen(
    private val rows: Int = 4,
    private val cols: Int = 4,
    private val resume: Boolean = false,
    private val mode: GameMode = GameMode.CLASSIC
) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = koinViewModel<GameViewModel>()
        val state by viewModel.state.collectAsState()
        val highScore by viewModel.highScore.collectAsState()
        val showRestartDialog = remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            if (resume) viewModel.onEvent(GameEvent.ResumeGame(rows, cols))
            else viewModel.onEvent(GameEvent.StartNewGame(rows, cols, mode))
        }

        val modeLabel = when (state.mode) {
            GameMode.CASUAL -> stringResource(R.string.mode_casual)
            GameMode.CLASSIC -> stringResource(R.string.mode_classic)
            GameMode.HARDCORE -> stringResource(R.string.mode_hardcore)
        }

        fun startNewGame() {
            viewModel.onEvent(
                GameEvent.StartNewGame(
                    state.rows,
                    state.cols,
                    state.mode
                )
            )
        }

        BoxWithConstraints {
            val isExpanded = maxWidth >= 600.dp
            val isCompactHeight = maxHeight < 580.dp

            Scaffold(
                topBar = {
                    GameTopBar(
                        rows = state.rows,
                        cols = state.cols,
                        modeLabel = modeLabel,
                        isExpanded = isExpanded,
                        isCompactHeight = isCompactHeight,
                        onBack = { navigator.pop() },
                        onRestart = {
                            if (state.isGameOver) {
                                startNewGame()
                                return@GameTopBar
                            }
                            showRestartDialog.value = true
                        }
                    )
                }
            ) { padding ->
                if (showRestartDialog.value) {
                    RestartGameDialog(
                        onConfirm = {
                            startNewGame()
                            showRestartDialog.value = false
                        },
                        onDismiss = { showRestartDialog.value = false }
                    )
                }

                if (isExpanded) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .padding(24.dp),
                        horizontalArrangement = Arrangement.spacedBy(32.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            GameScoreHeader(
                                currentScore = state.score,
                                highScore = highScore
                            )
                            Spacer(modifier = Modifier.height(32.dp))
                            GameControls(
                                canUndo = state.canUndo,
                                isCompact = false,
                                onUndo = { viewModel.onEvent(GameEvent.Undo) },
                                onStatistics = { navigator += StatisticsScreen() }
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1.2f)
                                .fillMaxHeight()
                                .sizeIn(maxWidth = 500.dp, maxHeight = 500.dp)
                                .aspectRatio(state.cols.toFloat() / state.rows.toFloat())
                                .align(Alignment.CenterVertically)
                        ) {
                            GameBoardBlock(state, viewModel)
                        }
                    }
                } else {
                    val dynamicSpacing = if (isCompactHeight) 12.dp else 24.dp
                    val outerPadding = if (isCompactHeight) 8.dp else 16.dp

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .padding(outerPadding)
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(dynamicSpacing)
                    ) {
                        GameScoreHeader(
                            currentScore = state.score,
                            highScore = highScore
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(weight = 1f, fill = !isCompactHeight)
                                .heightIn(max = if (isCompactHeight) 280.dp else 450.dp)
                                .aspectRatio(state.cols.toFloat() / state.rows.toFloat())
                        ) {
                            GameBoardBlock(state, viewModel)
                        }

                        GameControls(
                            canUndo = state.canUndo,
                            isCompact = isCompactHeight,
                            onUndo = { viewModel.onEvent(GameEvent.Undo) },
                            onStatistics = { navigator += StatisticsScreen() }
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun GameBoardBlock(state: GameState, viewModel: GameViewModel) {
        GameBoard(
            grid = state.grid,
            rows = state.rows,
            cols = state.cols,
            onMove = { if (!state.isGameOver) viewModel.onEvent(GameEvent.Move(it)) }
        )
        GameOverOverlay(
            isGameOver = state.isGameOver,
            score = state.score,
            onRestart = {
                viewModel.onEvent(
                    GameEvent.StartNewGame(
                        state.rows,
                        state.cols,
                        state.mode
                    )
                )
            }
        )
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun GameTopBar(
        rows: Int,
        cols: Int,
        modeLabel: String,
        isExpanded: Boolean,
        isCompactHeight: Boolean,
        onBack: () -> Unit,
        onRestart: () -> Unit
    ) {
        val titleContent = @Composable {
            Column {
                Text(
                    text = stringResource(R.string.game_title),
                    style = if (isCompactHeight) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge
                )
                if (!isCompactHeight) {
                    Text(
                        stringResource(R.string.game_mode_title_format, rows, cols, modeLabel),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        val navigationIconContent = @Composable {
            IconButton(onClick = onBack) {
                Icon(
                    painterResource(R.drawable.round_arrow_back_24),
                    stringResource(R.string.common_back)
                )
            }
        }

        val actionsContent = @Composable {
            IconButton(onClick = onRestart) {
                Icon(
                    painterResource(R.drawable.round_refresh_24),
                    stringResource(R.string.common_restart)
                )
            }
        }

        if (isExpanded || isCompactHeight) {
            TopAppBar(
                title = titleContent,
                navigationIcon = navigationIconContent,
                actions = { actionsContent() }
            )
        } else {
            LargeTopAppBar(
                title = titleContent,
                navigationIcon = navigationIconContent,
                actions = { actionsContent() }
            )
        }
    }

    @Composable
    private fun RestartGameDialog(
        onConfirm: () -> Unit,
        onDismiss: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        AlertDialog(
            modifier = modifier,
            onDismissRequest = onDismiss,
            icon = {
                Icon(
                    painter = painterResource(R.drawable.round_refresh_24),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = {
                Text(
                    text = stringResource(R.string.game_restart_dialog_title),
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.game_restart_dialog_message),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                FilledTonalButton(
                    onClick = onConfirm,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Text(stringResource(R.string.common_restart))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(
                        text = stringResource(R.string.common_cancel),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            shape = MaterialTheme.shapes.extraLarge,
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(6.dp)
        )
    }

    @Composable
    private fun GameScoreHeader(
        currentScore: Int,
        highScore: Int
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ScoreCard(
                label = stringResource(R.string.game_score),
                score = currentScore,
                icon = painterResource(R.drawable.round_star_24),
                modifier = Modifier.weight(1f)
            )
            ScoreCard(
                label = stringResource(R.string.stats_best),
                score = highScore,
                icon = painterResource(R.drawable.round_emoji_events_24),
                modifier = Modifier.weight(1f)
            )
        }
    }

    @Composable
    private fun GameOverOverlay(
        isGameOver: Boolean,
        score: Int,
        onRestart: () -> Unit
    ) {
        AnimatedVisibility(
            visible = isGameOver,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Card(
                modifier = Modifier.fillMaxSize(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        stringResource(R.string.game_over),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        stringResource(R.string.game_final_score_format, score),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Button(
                        onClick = onRestart,
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Icon(painterResource(R.drawable.round_refresh_24), null)
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.game_try_again))
                    }
                }
            }
        }
    }

    @Composable
    private fun GameControls(
        canUndo: Boolean,
        isCompact: Boolean,
        onUndo: () -> Unit,
        onStatistics: () -> Unit
    ) {
        val buttonHeight = if (isCompact) 44.dp else 56.dp

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onUndo,
                modifier = Modifier
                    .weight(1f)
                    .height(buttonHeight),
                enabled = canUndo,
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(painterResource(R.drawable.round_undo_24), null)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.common_undo),
                    maxLines = 1
                )
            }

            FilledTonalButton(
                onClick = onStatistics,
                modifier = Modifier
                    .weight(1f)
                    .height(buttonHeight),
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(painterResource(R.drawable.round_leaderboard_24), null)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.common_stats),
                    maxLines = 1
                )
            }
        }
    }
}

@Preview(device = "id:pixel_10")
@Composable
private fun Game2048ScreenPreview() {
    val context = LocalContext.current
    KoinApplication(
        configuration = koinConfiguration(
            declaration = {
                androidContext(context)
                modules(appModule)
            }
        ), content = {
            Libre2048Theme {
                Navigator(Game2048Screen())
            }
        }
    )
}