package off.kys.libre2048.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class GameMode(val undoPolicy: UndoPolicy) {
    CASUAL(UndoPolicy.UNLIMITED),
    CLASSIC(UndoPolicy.SINGLE),
    HARDCORE(UndoPolicy.NONE)
}
