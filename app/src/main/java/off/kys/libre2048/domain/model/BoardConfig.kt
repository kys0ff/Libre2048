package off.kys.libre2048.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class BoardConfig(
    val rows: Int,
    val cols: Int
)
