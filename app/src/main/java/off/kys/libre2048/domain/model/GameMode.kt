package off.kys.libre2048.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class GameMode(val label: String, val undoPolicy: UndoPolicy) {
    CASUAL("Casual", UndoPolicy.UNLIMITED),
    CLASSIC("Classic", UndoPolicy.SINGLE),
    HARDCORE("Hardcore", UndoPolicy.NONE)
}
