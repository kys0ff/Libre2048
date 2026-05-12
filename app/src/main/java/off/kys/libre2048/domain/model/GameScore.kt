package off.kys.libre2048.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class GameScore(
    val score: Int,
    val date: Long,
    val rows: Int,
    val cols: Int,
    val mode: GameMode
)
