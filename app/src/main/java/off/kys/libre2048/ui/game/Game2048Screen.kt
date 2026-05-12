package off.kys.libre2048.ui.game

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import off.kys.libre2048.R
import off.kys.libre2048.domain.model.GameMode
import off.kys.libre2048.ui.common.GameBoard
import off.kys.libre2048.ui.common.ScoreCard
import off.kys.libre2048.ui.stats.StatisticsScreen
import org.koin.androidx.compose.koinViewModel

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

        LaunchedEffect(Unit) {
            if (resume) viewModel.onEvent(GameEvent.ResumeGame(rows, cols))
            else viewModel.onEvent(GameEvent.StartNewGame(rows, cols, mode))
        }

        val modeLabel = when (state.mode) {
            GameMode.CASUAL -> stringResource(R.string.mode_casual)
            GameMode.CLASSIC -> stringResource(R.string.mode_classic)
            GameMode.HARDCORE -> stringResource(R.string.mode_hardcore)
        }

        Scaffold(
            topBar = {
                GameTopBar(
                    rows = state.rows,
                    cols = state.cols,
                    modeLabel = modeLabel,
                    onBack = { navigator.pop() },
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
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                GameScoreHeader(
                    currentScore = state.score,
                    highScore = highScore
                )

                Spacer(modifier = Modifier.height(32.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(state.cols.toFloat() / state.rows.toFloat())
                ) {
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

                Spacer(modifier = Modifier.height(32.dp))

                GameControls(
                    canUndo = state.canUndo,
                    onUndo = { viewModel.onEvent(GameEvent.Undo) },
                    onStatistics = { navigator += StatisticsScreen() }
                )
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun GameTopBar(
        rows: Int,
        cols: Int,
        modeLabel: String,
        onBack: () -> Unit,
        onRestart: () -> Unit
    ) {
        LargeTopAppBar(
            title = {
                Column {
                    Text("2048")
                    Text(
                        stringResource(
                            R.string.game_mode_title_format,
                            rows,
                            cols,
                            modeLabel
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        painterResource(R.drawable.round_arrow_back_24),
                        stringResource(R.string.common_back)
                    )
                }
            },
            actions = {
                IconButton(onClick = onRestart) {
                    Icon(
                        painterResource(R.drawable.round_refresh_24),
                        stringResource(R.string.common_restart)
                    )
                }
            }
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
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        stringResource(R.string.game_over),
                        style = MaterialTheme.typography.displaySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        stringResource(R.string.game_final_score_format, score),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(bottom = 24.dp)
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
        onUndo: () -> Unit,
        onStatistics: () -> Unit
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onUndo,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                enabled = canUndo,
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(painterResource(R.drawable.round_undo_24), null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.common_undo))
            }

            FilledTonalButton(
                onClick = onStatistics,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(painterResource(R.drawable.round_leaderboard_24), null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.common_stats))
            }
        }
    }
}