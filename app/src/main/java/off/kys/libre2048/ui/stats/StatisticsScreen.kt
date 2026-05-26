package off.kys.libre2048.ui.stats

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import off.kys.libre2048.R
import off.kys.libre2048.domain.model.GameMode
import off.kys.libre2048.domain.model.GameScore
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StatisticsScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        val viewModel = koinViewModel<StatisticsViewModel>()
        val uiState by viewModel.uiState.collectAsState()

        StatisticsContent(
            uiState = uiState,
            onBackClick = { navigator?.pop() }
        )
    }

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
    @Composable
    private fun StatisticsContent(
        uiState: StatisticsUiState,
        onBackClick: () -> Unit
    ) {
        val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

        BoxWithConstraints {
            val isSmallScreen = maxWidth < 360.dp || maxHeight < 600.dp

            Scaffold(
                modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
                topBar = {
                    if (isSmallScreen) {
                        TopAppBar(
                            title = {
                                Text(
                                    text = stringResource(R.string.stats_title),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            navigationIcon = {
                                IconButton(onClick = onBackClick) {
                                    Icon(
                                        painter = painterResource(R.drawable.round_arrow_back_24),
                                        contentDescription = stringResource(R.string.common_back)
                                    )
                                }
                            }
                        )
                    } else {
                        LargeTopAppBar(
                            title = {
                                Text(
                                    text = stringResource(R.string.stats_title),
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            navigationIcon = {
                                IconButton(onClick = onBackClick) {
                                    Icon(
                                        painter = painterResource(R.drawable.round_arrow_back_24),
                                        contentDescription = stringResource(R.string.common_back)
                                    )
                                }
                            },
                            scrollBehavior = scrollBehavior
                        )
                    }
                }
            ) { innerPadding ->
                when {
                    uiState.isLoading -> LoadingState(Modifier.padding(innerPadding))
                    uiState.scores.isEmpty() -> EmptyStatsState(
                        isSmallScreen,
                        Modifier.padding(innerPadding)
                    )

                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = innerPadding
                        ) {
                            item {
                                TotalSummary(uiState.scores, isSmallScreen)
                                Spacer(Modifier.height(if (isSmallScreen) 4.dp else 8.dp))
                            }

                            uiState.scoresByMode.forEach { (mode, modeScores) ->
                                stickyHeader {
                                    ModeHeader(mode, modeScores)
                                }

                                items(
                                    items = modeScores.sortedByDescending { it.date },
                                    key = { "${it.date}_${it.score}" }
                                ) { score ->
                                    ScoreItem(score, isSmallScreen)
                                }

                                item { Spacer(Modifier.height(if (isSmallScreen) 8.dp else 16.dp)) }
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun LoadingState(modifier: Modifier = Modifier) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }

    @Composable
    private fun EmptyStatsState(isSmallScreen: Boolean, modifier: Modifier = Modifier) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(R.drawable.round_bar_chart_24),
                contentDescription = null,
                modifier = Modifier.size(if (isSmallScreen) 72.dp else 120.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.stats_empty_title),
                style = if (isSmallScreen) MaterialTheme.typography.titleMedium else MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )

            Text(
                text = stringResource(R.string.stats_empty_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                textAlign = TextAlign.Center
            )
        }
    }

    @Composable
    private fun TotalSummary(scores: List<GameScore>, isSmallScreen: Boolean) {
        val outerPadding = if (isSmallScreen) 8.dp else 16.dp
        val innerPadding = if (isSmallScreen) 12.dp else 24.dp

        Surface(
            modifier = Modifier.padding(outerPadding),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.primaryContainer,
        ) {
            Column(modifier = Modifier.padding(innerPadding)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(R.drawable.round_analytics_24),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(if (isSmallScreen) 18.dp else 24.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.stats_lifetime_overview),
                        style = if (isSmallScreen) MaterialTheme.typography.titleSmall else MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(if (isSmallScreen) 12.dp else 20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    SummaryStat(
                        label = stringResource(R.string.stats_games),
                        value = scores.size.toString(),
                        icon = painterResource(R.drawable.round_history_24),
                        isSmallScreen = isSmallScreen
                    )
                    SummaryStat(
                        label = stringResource(R.string.stats_best),
                        value = scores.maxOfOrNull { it.score }?.toString() ?: "0",
                        icon = painterResource(R.drawable.round_emoji_events_24),
                        isSmallScreen = isSmallScreen
                    )
                    SummaryStat(
                        label = stringResource(R.string.stats_avg),
                        value = (scores.map { it.score }.average().takeIf { !it.isNaN() }?.toInt()
                            ?: 0).toString(),
                        icon = painterResource(R.drawable.round_functions_24),
                        isSmallScreen = isSmallScreen
                    )
                }
            }
        }
    }

    @Composable
    private fun SummaryStat(label: String, value: String, icon: Painter, isSmallScreen: Boolean) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                painter = icon,
                contentDescription = null,
                modifier = Modifier.size(if (isSmallScreen) 14.dp else 18.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
            )
            Text(
                text = value,
                style = if (isSmallScreen) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black
            )
            Text(
                text = label.uppercase(),
                style = if (isSmallScreen) MaterialTheme.typography.labelMedium else MaterialTheme.typography.labelSmall,
                fontSize = if (isSmallScreen) 9.sp else 11.sp,
                letterSpacing = if (isSmallScreen) 0.5.sp else 1.sp
            )
        }
    }

    @Composable
    private fun ModeHeader(mode: GameMode, scores: List<GameScore>) {
        val modeLabel = when (mode) {
            GameMode.CASUAL -> stringResource(R.string.mode_casual)
            GameMode.CLASSIC -> stringResource(R.string.mode_classic)
            GameMode.HARDCORE -> stringResource(R.string.mode_hardcore)
        }

        Surface(
            color = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = modeLabel,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = CircleShape
                ) {
                    Text(
                        text = stringResource(R.string.stats_count_games, scores.size),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
    }

    @Composable
    private fun ScoreItem(score: GameScore, isSmallScreen: Boolean) {
        val dateFormatPattern = stringResource(R.string.stats_date_format)
        val dateFormat =
            remember(dateFormatPattern) { SimpleDateFormat(dateFormatPattern, Locale.getDefault()) }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = if (isSmallScreen) 8.dp else 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(if (isSmallScreen) 38.dp else 48.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = MaterialTheme.shapes.medium
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${score.rows}",
                    style = if (isSmallScreen) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(
                modifier = Modifier
                    .padding(start = if (isSmallScreen) 10.dp else 16.dp)
                    .weight(1f)
            ) {
                Text(
                    text = stringResource(R.string.stats_grid_format, score.rows, score.cols),
                    style = if (isSmallScreen) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = dateFormat.format(Date(score.date)),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = String.format(
                    locale = LocalLocale.current.platformLocale,
                    format = stringResource(R.string.stats_score_format),
                    score.score
                ),
                style = if (isSmallScreen) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}