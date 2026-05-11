package off.kys.libre2048.ui.game

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import off.kys.libre2048.R
import off.kys.libre2048.ui.common.GameBoard
import off.kys.libre2048.ui.common.ScoreCard
import org.koin.androidx.compose.koinViewModel

class Game2048Screen(
    val rows: Int = 4,
    val cols: Int = 4,
    val resume: Boolean = false
) : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val viewModel = koinViewModel<GameViewModel>()
        val state by viewModel.state.collectAsState()

        val highScore by viewModel.highScore.collectAsState()
        val navigator = LocalNavigator.currentOrThrow

        LaunchedEffect(Unit) {
            if (resume) {
                viewModel.onEvent(GameEvent.ResumeGame(rows, cols))
            } else {
                viewModel.onEvent(GameEvent.StartNewGame(rows, cols))
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("2048 (${state.rows}x${state.cols})") },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(
                                painter = painterResource(R.drawable.round_arrow_back_24),
                                contentDescription = "Back"
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            viewModel.onEvent(
                                GameEvent.StartNewGame(
                                    state.rows,
                                    state.cols
                                )
                            )
                        }) {
                            Icon(
                                painter = painterResource(R.drawable.round_refresh_24),
                                contentDescription = "New Game"
                            )
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ScoreCard("SCORE", state.score, modifier = Modifier.weight(1f))
                    ScoreCard("BEST", highScore, modifier = Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(24.dp))

                GameBoard(
                    grid = state.grid,
                    rows = state.rows,
                    cols = state.cols,
                    onMove = { viewModel.onEvent(GameEvent.Move(it)) }
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = { viewModel.onEvent(GameEvent.Undo) },
                        modifier = Modifier.weight(1f),
                        enabled = !state.isGameOver
                    ) {
                        Text("Undo")
                    }
                }

                if (state.isGameOver) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Game Over!",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                    Button(onClick = {
                        viewModel.onEvent(
                            GameEvent.StartNewGame(
                                state.rows,
                                state.cols
                            )
                        )
                    }) {
                        Text("Try Again")
                    }
                }
            }
        }
    }
}