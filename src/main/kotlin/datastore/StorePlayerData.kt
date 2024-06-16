package userunp.hyperstom.datastore

import net.kyori.adventure.nbt.BinaryTag
import net.kyori.adventure.nbt.IntBinaryTag
import userunp.hyperstom.WorldMode
import java.util.UUID

fun inPlayMode(state: StorePlayerState?) = state?.mode == WorldMode.PLAY
fun inBuildMode(state: StorePlayerState?) = state?.mode == WorldMode.BUILD
fun inDevMode(state: StorePlayerState?) = state?.mode == WorldMode.DEV

@JvmRecord
@DataStoreRecord("state")
data class StorePlayerState(val modeIndex: Int, val id: UUID) {
    val mode get() = WorldMode.entries[modeIndex]
    fun withMode(newMode: WorldMode): StorePlayerState {
        if (mode == newMode) throw RuntimeException("Already in ${mode.name} mode!")
        return StorePlayerState(newMode.ordinal, id)
    }

    companion object : TagStoreCompanion {
        override fun defaultFunc(missing: String): BinaryTag {
            return when(missing) {
                "modeIndex" -> IntBinaryTag.intBinaryTag(WorldMode.PLAY.ordinal)
                "id" -> throw NullPointerException("Player not in a world!")
                else -> throw RuntimeException("Unexpected value! $missing")
            }
        }

    }
}
