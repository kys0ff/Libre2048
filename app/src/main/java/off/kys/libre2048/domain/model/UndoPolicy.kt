package off.kys.libre2048.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class UndoPolicy {
    UNLIMITED,
    SINGLE,
    NONE
}
