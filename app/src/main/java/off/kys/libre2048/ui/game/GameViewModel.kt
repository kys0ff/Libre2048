package off.kys.libre2048.ui.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import off.kys.libre2048.data.repository.GameRepository
import off.kys.libre2048.domain.model.Direction
import off.kys.libre2048.domain.model.GameMode
import off.kys.libre2048.domain.model.GameScore
import off.kys.libre2048.domain.model.GameState
import off.kys.libre2048.domain.model.Tile
import off.kys.libre2048.domain.model.UndoPolicy
import java.util.Stack
import kotlin.random.Random

class GameViewModel(private val repository: GameRepository) : ViewModel() {
    private val _state = MutableStateFlow(GameState(grid = emptyList()))
    val state = _state.asStateFlow()

    private val _highScore = MutableStateFlow(0)
    val highScore = _highScore.asStateFlow()

    private val history = Stack<GameState>()
    private var tileIdCounter = 0

    init {
        viewModelScope.launch {
            _state.collectLatest { state ->
                if (state.grid.isNotEmpty()) {
                    repository.saveCurrentState(state.rows, state.cols, state)
                    repository.saveHighScore(state.rows, state.cols, state.mode, state.score)
                    if (state.isGameOver) {
                        repository.saveScore(
                            GameScore(
                                score = state.score,
                                date = System.currentTimeMillis(),
                                rows = state.rows,
                                cols = state.cols,
                                mode = state.mode
                            )
                        )
                    }
                }
            }
        }

        viewModelScope.launch {
            _state.collectLatest { state ->
                if (state.grid.isNotEmpty()) {
                    repository.getHighScore(state.rows, state.cols, state.mode).collect {
                        _highScore.value = it
                    }
                }
            }
        }
    }


    fun onEvent(event: GameEvent) {
        when (event) {
            is GameEvent.StartNewGame -> startNewGame(event.rows, event.cols, event.mode)
            is GameEvent.Move -> move(event.direction)
            GameEvent.Undo -> undo()
            is GameEvent.ResumeGame -> resumeGame(event.rows, event.cols)
        }
    }

    private fun startNewGame(rows: Int, cols: Int, mode: GameMode) {
        history.clear()
        viewModelScope.launch {
            repository.saveHistory(rows, cols, emptyList())
        }
        tileIdCounter = 0
        val initialState = GameState(
            grid = List(rows) { List(cols) { null } },
            rows = rows,
            cols = cols,
            mode = mode
        ).spawnTile().spawnTile()
        _state.value = initialState
    }

    private fun resumeGame(rows: Int, cols: Int) {
        viewModelScope.launch {
            val savedState = repository.getCurrentState(rows, cols).first()
            if (savedState != null) {
                _state.value = savedState
                tileIdCounter =
                    (savedState.grid.flatten().filterNotNull().maxOfOrNull { it.id } ?: -1) + 1

                val savedHistory = repository.getHistory(rows, cols).first()
                history.clear()
                savedHistory.forEach { history.push(it) }

                _state.value = _state.value.copy(
                    canUndo = history.isNotEmpty() && _state.value.mode.undoPolicy != UndoPolicy.NONE
                )
            } else {
                startNewGame(rows, cols, GameMode.CLASSIC)
            }
        }
    }

    private fun undo() {
        if (_state.value.mode.undoPolicy == UndoPolicy.NONE) return

        val canUndo = history.isNotEmpty()

        if (canUndo) {
            val prevState = history.pop()
            _state.value = prevState
            val currentHistory = history.toList()
            viewModelScope.launch {
                repository.saveHistory(prevState.rows, prevState.cols, currentHistory)
            }
        }
        _state.value =
            _state.value.copy(canUndo = history.isNotEmpty() && _state.value.mode.undoPolicy != UndoPolicy.NONE)
    }

    private fun move(direction: Direction) {
        val currentState = _state.value
        val (newGrid, moveScore) = calculateMove(currentState.grid, direction, currentState.rows, currentState.cols)

        if (isGridChanged(currentState.grid, newGrid, currentState.rows, currentState.cols)) {
            if (currentState.mode.undoPolicy != UndoPolicy.NONE) {
                if (currentState.mode.undoPolicy == UndoPolicy.SINGLE) {
                    history.clear()
                }
                history.push(currentState)
                val currentHistory = history.toList()
                viewModelScope.launch {
                    repository.saveHistory(currentState.rows, currentState.cols, currentHistory)
                }
            }
            
            val nextState = currentState.copy(
                grid = newGrid,
                score = currentState.score + moveScore,
                canUndo = currentState.mode.undoPolicy != UndoPolicy.NONE
            ).spawnTile()

            _state.value = nextState.copy(isGameOver = checkGameOver(nextState.grid, nextState.rows, nextState.cols))
        }
    }

    private fun calculateMove(grid: List<List<Tile?>>, direction: Direction, rows: Int, cols: Int): Pair<List<List<Tile?>>, Int> {
        var scoreGain = 0
        val newGrid = MutableList(rows) { MutableList<Tile?>(cols) { null } }

        val isVertical = direction == Direction.UP || direction == Direction.DOWN
        val outerLimit = if (isVertical) cols else rows
        val innerLimit = if (isVertical) rows else cols

        for (i in 0 until outerLimit) {
            val originalLine = mutableListOf<Tile?>()
            for (j in 0 until innerLimit) {
                val tile = when (direction) {
                    Direction.LEFT -> grid[i][j]
                    Direction.RIGHT -> grid[i][innerLimit - 1 - j]
                    Direction.UP -> grid[j][i]
                    Direction.DOWN -> grid[innerLimit - 1 - j][i]
                }
                if (tile != null) originalLine.add(tile)
            }

            val mergedLine = mutableListOf<Tile?>()
            var j = 0
            while (j < originalLine.size) {
                val current = originalLine[j]!!
                if (j + 1 < originalLine.size && current.value == originalLine[j + 1]?.value) {
                    val combinedValue = current.value * 2
                    scoreGain += combinedValue
                    mergedLine.add(current.copy(value = combinedValue))
                    j += 2
                } else {
                    mergedLine.add(current)
                    j++
                }
            }

            for (k in 0 until innerLimit) {
                val tile = mergedLine.getOrNull(k)
                val finalTile = tile?.let {
                    when (direction) {
                        Direction.LEFT -> it.copy(x = k, y = i)
                        Direction.RIGHT -> it.copy(x = innerLimit - 1 - k, y = i)
                        Direction.UP -> it.copy(x = i, y = k)
                        Direction.DOWN -> it.copy(x = i, y = innerLimit - 1 - k)
                    }
                }

                when (direction) {
                    Direction.LEFT -> newGrid[i][k] = finalTile
                    Direction.RIGHT -> newGrid[i][innerLimit - 1 - k] = finalTile
                    Direction.UP -> newGrid[k][i] = finalTile
                    Direction.DOWN -> newGrid[innerLimit - 1 - k][i] = finalTile
                }
            }
        }
        return newGrid to scoreGain
    }

    private fun GameState.spawnTile(): GameState {
        val emptyCells = mutableListOf<Pair<Int, Int>>()
        grid.forEachIndexed { r, row ->
            row.forEachIndexed { c, tile -> if (tile == null) emptyCells.add(r to c) }
        }
        if (emptyCells.isEmpty()) return this

        val (r, c) = emptyCells.random()
        val newValue = if (Random.nextFloat() < 0.9f) 2 else 4
        val nextGrid = grid.map { it.toMutableList() }

        nextGrid[r][c] = Tile(tileIdCounter++, newValue, c, r)
        return copy(grid = nextGrid)
    }

    private fun isGridChanged(old: List<List<Tile?>>, new: List<List<Tile?>>, rows: Int, cols: Int): Boolean {
        for (i in 0 until rows) {
            for (j in 0 until cols) {
                if (old[i][j]?.value != new[i][j]?.value) return true
            }
        }
        return false
    }

    private fun checkGameOver(grid: List<List<Tile?>>, rows: Int, cols: Int): Boolean {
        if (grid.any { row -> row.any { it == null } }) return false
        for (i in 0 until rows) {
            for (j in 0 until cols) {
                val current = grid[i][j]?.value ?: continue
                if (i < rows - 1 && grid[i + 1][j]?.value == current) return false
                if (j < cols - 1 && grid[i][j + 1]?.value == current) return false
            }
        }
        return true
    }
}