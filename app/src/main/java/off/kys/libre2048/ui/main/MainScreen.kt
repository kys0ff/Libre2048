package off.kys.libre2048.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import off.kys.libre2048.R
import off.kys.libre2048.data.repository.GameRepository
import off.kys.libre2048.di.appModule
import off.kys.libre2048.domain.model.BoardConfig
import off.kys.libre2048.domain.model.GameMode
import off.kys.libre2048.domain.model.UndoPolicy
import off.kys.libre2048.ui.about.AboutScreen
import off.kys.libre2048.ui.common.GameBoardPreview
import off.kys.libre2048.ui.game.Game2048Screen
import off.kys.libre2048.ui.stats.StatisticsScreen
import off.kys.libre2048.ui.theme.Libre2048Theme
import org.koin.android.ext.koin.androidContext
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import org.koin.dsl.koinConfiguration

class MainScreen : Screen {

    companion object {
        private val SQUARE_MODES: List<BoardConfig>
            get() = listOf(
                BoardConfig(4, 4),
                BoardConfig(5, 5),
                BoardConfig(6, 6),
                BoardConfig(8, 8)
            )

        private val RECT_MODES: List<BoardConfig>
            get() = listOf(
                BoardConfig(3, 5),
                BoardConfig(4, 6),
                BoardConfig(5, 8),
                BoardConfig(6, 9)
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

        BoxWithConstraints {
            val isSmallScreen = maxWidth < 360.dp || maxHeight < 600.dp

            Scaffold(
                topBar = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = if (isSmallScreen) 4.dp else 12.dp)
                            .padding(top = if (isSmallScreen) 4.dp else 12.dp)
                    ) {
                        Surface(
                            onClick = { navigator += AboutScreen() },
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.align(Alignment.TopEnd)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.round_sentiment_satisfied_alt_24), // Using a smiley as a placeholder for info/about
                                contentDescription = stringResource(R.string.about_title),
                                modifier = Modifier
                                    .padding(if (isSmallScreen) 8.dp else 12.dp)
                                    .size(if (isSmallScreen) 20.dp else 24.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = { navigator += StatisticsScreen() },
                        modifier = Modifier.padding(if (isSmallScreen) 4.dp else 16.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.round_bar_chart_24),
                            contentDescription = stringResource(R.string.common_stats)
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
                    Spacer(modifier = Modifier.height(if (isSmallScreen) 20.dp else 48.dp))

                    Text(
                        text = stringResource(R.string.game_title),
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.Black,
                        fontSize = if (isSmallScreen) 48.sp else 80.sp,
                        color = MaterialTheme.colorScheme.primary,
                        lineHeight = if (isSmallScreen) 52.sp else 86.sp
                    )

                    Spacer(modifier = Modifier.height(if (isSmallScreen) 16.dp else 32.dp))

                    Text(
                        text = stringResource(R.string.main_square_modes),
                        style = if (isSmallScreen) MaterialTheme.typography.titleMedium else MaterialTheme.typography.headlineSmall,
                        modifier = Modifier
                            .padding(horizontal = if (isSmallScreen) 16.dp else 24.dp)
                            .align(Alignment.Start)
                    )
                    ModeRow(
                        SQUARE_MODES,
                        repository,
                        navigator,
                        isSmallScreen
                    ) { selectedBoardConfig.value = it }

                    Spacer(modifier = Modifier.height(if (isSmallScreen) 12.dp else 24.dp))

                    Text(
                        text = stringResource(R.string.main_rectangular_modes),
                        style = if (isSmallScreen) MaterialTheme.typography.titleMedium else MaterialTheme.typography.headlineSmall,
                        modifier = Modifier
                            .padding(horizontal = if (isSmallScreen) 16.dp else 24.dp)
                            .align(Alignment.Start)
                    )
                    ModeRow(
                        RECT_MODES,
                        repository,
                        navigator,
                        isSmallScreen
                    ) { selectedBoardConfig.value = it }

                    Spacer(modifier = Modifier.height(if (isSmallScreen) 16.dp else 32.dp))
                }
            }
        }
    }

    @Composable
    private fun ModeRow(
        modes: List<BoardConfig>,
        repository: GameRepository,
        navigator: Navigator,
        isSmallScreen: Boolean,
        onBoardClick: (BoardConfig) -> Unit
    ) {
        LazyRow(
            contentPadding = PaddingValues(
                horizontal = if (isSmallScreen) 12.dp else 16.dp,
                vertical = 8.dp
            ),
            horizontalArrangement = Arrangement.spacedBy(if (isSmallScreen) 8.dp else 12.dp)
        ) {
            items(modes) { mode ->
                ModeCard(
                    config = mode,
                    repository = repository,
                    navigator = navigator,
                    isSmallScreen = isSmallScreen,
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
        isSmallScreen: Boolean,
        onBoardClick: (BoardConfig) -> Unit
    ) {
        val savedState by repository.getCurrentState(config.rows, config.cols)
            .collectAsState(initial = null)

        val cardWidth = if (isSmallScreen) 115.dp else 140.dp
        val previewSize = if (isSmallScreen) 75.dp else 100.dp

        Card(
            modifier = Modifier.width(cardWidth),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                    alpha = 0.5f
                )
            )
        ) {
            Column(
                modifier = Modifier.padding(if (isSmallScreen) 8.dp else 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                GameBoardPreview(
                    rows = config.rows,
                    cols = config.cols,
                    modifier = Modifier.size(previewSize),
                    gameState = savedState
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.stats_grid_format, config.rows, config.cols),
                    style = if (isSmallScreen) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))

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
                        Text(
                            stringResource(R.string.main_resume),
                            fontSize = if (isSmallScreen) 10.sp else 12.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    TextButton(
                        onClick = { onBoardClick(config) },
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            stringResource(R.string.main_new_game),
                            fontSize = if (isSmallScreen) 10.sp else 12.sp
                        )
                    }
                } else {
                    Button(
                        onClick = { onBoardClick(config) },
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            stringResource(R.string.main_play),
                            fontSize = if (isSmallScreen) 10.sp else 12.sp
                        )
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
                    text = stringResource(R.string.main_select_difficulty),
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 4.dp)
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
                    Text(stringResource(R.string.common_close))
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
            UndoPolicy.UNLIMITED -> stringResource(R.string.undo_unlimited)
            UndoPolicy.SINGLE -> stringResource(R.string.undo_single)
            UndoPolicy.NONE -> stringResource(R.string.undo_none)
        }

        val modeLabel = when (mode) {
            GameMode.CASUAL -> stringResource(R.string.mode_casual)
            GameMode.CLASSIC -> stringResource(R.string.mode_classic)
            GameMode.HARDCORE -> stringResource(R.string.mode_hardcore)
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
                    .padding(12.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
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
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = modeLabel,
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
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Preview(device = "id:pixel_tablet")
@Composable
private fun MainScreenPreview() {
    val context = LocalContext.current
    KoinApplication(
        configuration = koinConfiguration(
            declaration = {
                androidContext(context)
                modules(appModule)
            }
        ),
        content = {
            Libre2048Theme {
                Navigator(MainScreen())
            }
        }
    )
}