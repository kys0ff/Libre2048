package off.kys.libre2048.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class GameState(
    val grid: List<List<Tile?>>,
    val score: Int = 0,
    val isGameOver: Boolean = false,
    val rows: Int = 4,
    val cols: Int = 4
)