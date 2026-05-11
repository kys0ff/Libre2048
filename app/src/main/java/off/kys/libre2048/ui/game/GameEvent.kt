package off.kys.libre2048.ui.game

import off.kys.libre2048.domain.model.Direction

sealed class GameEvent {
    data class StartNewGame(val rows: Int = 4, val cols: Int = 4) : GameEvent()
    data class Move(val direction: Direction) : GameEvent()
    object Undo : GameEvent()
    data class ResumeGame(val rows: Int, val cols: Int) : GameEvent()
}