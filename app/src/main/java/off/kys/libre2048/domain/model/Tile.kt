package off.kys.libre2048.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Tile(
    val id: Int,
    val value: Int,
    val x: Int,
    val y: Int,
)