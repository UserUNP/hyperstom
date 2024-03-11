package dev.bedcrab.hyperstom

import org.jglrxavpok.hephaistos.nbt.NBT
import java.util.UUID

@JvmRecord
@DataStoreRecord("state")
data class StorePlayerState(val modeIndex: Int, val id: UUID) {
    val mode get() = ModeHandler.Mode.entries[modeIndex]
    fun withMode(newMode: ModeHandler.Mode): StorePlayerState {
        if (mode == newMode) throw RuntimeException("Already in ${mode.name} mode!")
        return StorePlayerState(newMode.ordinal, id)
    }

    companion object : TagStoreCompanion {
        override fun defaultFunc(missing: String): NBT {
            return when(missing) {
                "mode" -> NBT.Int(ModeHandler.Mode.PLAY.ordinal)
                "id" -> throw NullPointerException("Player not in a world!")
                else -> throw RuntimeException("Unexpected value: $missing")
            }
        }

        fun usingPlay(state: StorePlayerState?) = state?.mode == ModeHandler.Mode.PLAY
        fun usingBuild(state: StorePlayerState?) = state?.mode == ModeHandler.Mode.BUILD
        fun usingDev(state: StorePlayerState?) = state?.mode == ModeHandler.Mode.DEV
    }
}
