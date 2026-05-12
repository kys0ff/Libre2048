package off.kys.libre2048.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import off.kys.libre2048.R
import off.kys.libre2048.data.repository.GameRepository
import off.kys.libre2048.domain.model.BoardConfig
import off.kys.libre2048.domain.model.GameMode
import off.kys.libre2048.domain.model.UndoPolicy
import off.kys.libre2048.ui.common.GameBoardPreview
import off.kys.libre2048.ui.game.Game2048Screen
import off.kys.libre2048.ui.stats.StatisticsScreen
import org.koin.compose.koinInject

class MainScreen : Screen {

    companion object {
        private val SQUARE_MODES = listOf(
            BoardConfig(4, 4, "4x4"),
            BoardConfig(5, 5, "5x5"),
            BoardConfig(6, 6, "6x6"),
            BoardConfig(8, 8, "8x8")
        )

        private val RECT_MODES = listOf(
            BoardConfig(3, 5, "3x5"),
            BoardConfig(4, 6, "4x6"),
            BoardConfig(5, 8, "5x8"),
            BoardConfig(6, 9, "6x9")
        )
    }

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val repository = koinInject<GameRepository>()
        val selectedBoardConfig = remember { mutableStateOf<BoardConfig?>(null) }

        selectedBoardConfig.value?.let { config ->
            ModeSelectionDialog(
                onModeSelected = { mode ->
                    navigator += Game2048Screen(
                        rows = config.rows,
                        cols = config.cols,
                        resume = false,
                        mode = mode
                    )
                    selectedBoardConfig.value = null
                },
                onDismiss = { selectedBoardConfig.value = null }
            )
        }

        Scaffold(
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { navigator += StatisticsScreen() }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.round_bar_chart_24),
                        contentDescription = "Statistics"
                    )
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Spacer(modifier = Modifier.height(48.dp))
                Text(
                    text = "2048",
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Black,
                    fontSize = 80.sp,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "Square Modes",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .align(Alignment.Start)
                )
                ModeRow(SQUARE_MODES, repository, navigator) { selectedBoardConfig.value = it }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Rectangular Modes",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .align(Alignment.Start)
                )
                ModeRow(RECT_MODES, repository, navigator) { selectedBoardConfig.value = it }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    @Composable
    private fun ModeRow(
        modes: List<BoardConfig>,
        repository: GameRepository,
        navigator: Navigator,
        onBoardClick: (BoardConfig) -> Unit
    ) {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(modes) { mode ->
                ModeCard(
                    config = mode,
                    repository = repository,
                    navigator = navigator,
                    onBoardClick = onBoardClick
                )
            }
        }
    }

    @Composable
    private fun ModeCard(
        config: BoardConfig,
        repository: GameRepository,
        navigator: Navigator,
        onBoardClick: (BoardConfig) -> Unit
    ) {
        val savedState by repository.getCurrentState(config.rows, config.cols)
            .collectAsState(initial = null)

        Card(
            modifier = Modifier.width(140.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                    alpha = 0.5f
                )
            )
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                GameBoardPreview(
                    rows = config.rows,
                    cols = config.cols,
                    modifier = Modifier.size(100.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(config.label, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                if (savedState != null) {
                    Button(
                        onClick = {
                            navigator += Game2048Screen(
                                rows = config.rows,
                                cols = config.cols,
                                resume = true
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("Resume", fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    TextButton(
                        onClick = { onBoardClick(config) },
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("New Game", fontSize = 12.sp)
                    }
                } else {
                    Button(
                        onClick = { onBoardClick(config) },
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("Play", fontSize = 12.sp)
                    }
                }
            }
        }
    }

    @Composable
    private fun ModeSelectionDialog(
        onModeSelected: (GameMode) -> Unit,
        onDismiss: () -> Unit
    ) {
        AlertDialog(
            onDismissRequest = onDismiss,
            icon = {
                Icon(
                    painter = painterResource(R.drawable.round_extension_24),
                    contentDescription = null
                )
            },
            title = {
                Text(
                    text = "Select Difficulty",
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    GameMode.entries.forEach { mode ->
                        ModeItem(
                            mode = mode,
                            onClick = {
                                onModeSelected(mode)
                                onDismiss()
                            }
                        )
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Close")
                }
            }
        )
    }

    @Composable
    private fun ModeItem(
        mode: GameMode,
        onClick: () -> Unit
    ) {
        val undoText = when (mode.undoPolicy) {
            UndoPolicy.UNLIMITED -> "Unlimited Undos"
            UndoPolicy.SINGLE -> "Single Undo"
            UndoPolicy.NONE -> "No Undos"
        }

        val containerColor = if (mode.undoPolicy == UndoPolicy.NONE) {
            MaterialTheme.colorScheme.tertiaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        }

        Surface(
            onClick = onClick,
            shape = MaterialTheme.shapes.large,
            color = containerColor,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    val icon = when (mode.undoPolicy) {
                        UndoPolicy.UNLIMITED -> R.drawable.round_sentiment_satisfied_alt_24
                        UndoPolicy.SINGLE -> R.drawable.round_hourglass_empty_24
                        UndoPolicy.NONE -> R.drawable.round_whatshot_24
                    }
                    Icon(
                        painter = painterResource(icon),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = mode.label,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = undoText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }

                Icon(
                    painter = painterResource(R.drawable.round_chevron_right_24),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        }
    }
}