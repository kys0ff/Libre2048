package off.kys.libre2048.ui.game

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
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

        // Use a scroll behavior to make the TopAppBar collapse elegantly
        val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

        LaunchedEffect(Unit) {
            if (resume) viewModel.onEvent(GameEvent.ResumeGame(rows, cols))
            else viewModel.onEvent(GameEvent.StartNewGame(rows, cols))
        }

        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                LargeTopAppBar(
                    title = {
                        Column {
                            Text("2048")
                            Text(
                                "${state.rows} × ${state.cols} Edition",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(painterResource(R.drawable.round_arrow_back_24), "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.onEvent(GameEvent.StartNewGame(state.rows, state.cols)) }) {
                            Icon(painterResource(R.drawable.round_refresh_24), "Restart")
                        }
                    },
                    scrollBehavior = scrollBehavior
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ScoreCard(
                        label = "Score",
                        score = state.score,
                        icon = painterResource(R.drawable.round_star_24),
                        modifier = Modifier.weight(1f)
                    )
                    ScoreCard(
                        label = "Best",
                        score = highScore,
                        icon = painterResource(R.drawable.round_emoji_events_24),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                ) {
                    GameBoard(
                        grid = state.grid,
                        rows = state.rows,
                        cols = state.cols,
                        onMove = { viewModel.onEvent(GameEvent.Move(it)) }
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Action Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.onEvent(GameEvent.Undo) },
                        modifier = Modifier.weight(1f).height(56.dp),
                        enabled = !state.isGameOver,
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Icon(painterResource(R.drawable.round_undo_24), null)
                        Spacer(Modifier.width(8.dp))
                        Text("Undo")
                    }

                    // Placeholder for other actions or a "Menu" button
                    FilledTonalButton(
                        onClick = { /* Settings or similar */ },
                        modifier = Modifier.weight(1f).height(56.dp),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text("Options")
                    }
                }

                // Game Over State as a sophisticated Modal or Card
                AnimatedVisibility(
                    visible = state.isGameOver,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Card(
                        modifier = Modifier.padding(top = 24.dp).fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "Game Over",
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(Modifier.height(8.dp))
                            Button(
                                onClick = { viewModel.onEvent(GameEvent.StartNewGame(state.rows, state.cols)) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Text("Try Again")
                            }
                        }
                    }
                }
            }
        }
    }
}