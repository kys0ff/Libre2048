package off.kys.libre2048.ui.stats

import off.kys.libre2048.domain.model.GameMode
import off.kys.libre2048.domain.model.GameScore

data class StatisticsUiState(
    val isLoading: Boolean = true,
    val scores: List<GameScore> = emptyList(),
    val scoresByMode: Map<GameMode, List<GameScore>> = emptyMap()
)