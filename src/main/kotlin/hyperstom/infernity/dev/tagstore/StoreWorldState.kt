package hyperstom.infernity.dev.tagstore

import hyperstom.infernity.dev.world.mode.ModeHandler
import org.jglrxavpok.hephaistos.nbt.NBT

@JvmRecord
data class StoreWorldState(val mode: Int, val id: Int) : TagStore.Companion.StoreData {

    fun withMode(newMode: ModeHandler.Mode) = StoreWorldState(newMode.ordinal, id)

    companion object : TagStore.Companion.StoreComp {
        override fun defaultFunc(missing: String): NBT {
            return when(missing) {
                "mode" -> NBT.Int(ModeHandler.Mode.PLAY.ordinal)
                "id" -> throw NullPointerException("Player not in a world!")
                else -> throw RuntimeException("Unexpected value: $missing")
            }
        }

        fun usingPlay(state: StoreWorldState?) = state?.mode == ModeHandler.Mode.PLAY.ordinal
        fun usingBuild(state: StoreWorldState?) = state?.mode == ModeHandler.Mode.BUILD.ordinal
        fun usingDev(state: StoreWorldState?) = state?.mode == ModeHandler.Mode.DEV.ordinal
    }
}
