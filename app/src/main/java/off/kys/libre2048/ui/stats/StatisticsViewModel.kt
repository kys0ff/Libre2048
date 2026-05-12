package off.kys.libre2048.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import off.kys.libre2048.data.repository.GameRepository

class StatisticsViewModel(private val repository: GameRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    init {
        loadStatistics()
    }

    private fun loadStatistics() {
        repository.getAllScores()
            .onEach { scores ->
                _uiState.value = StatisticsUiState(
                    isLoading = false,
                    scores = scores,
                    scoresByMode = scores.groupBy { it.mode }
                )
            }
            .launchIn(viewModelScope)
    }
}
