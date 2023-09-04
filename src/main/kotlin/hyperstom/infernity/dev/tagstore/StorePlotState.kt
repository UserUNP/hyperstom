package hyperstom.infernity.dev.tagstore

import hyperstom.infernity.dev.plot.mode.ModeHandler
import org.jglrxavpok.hephaistos.nbt.NBT

@JvmRecord
data class StorePlotState(val mode: Int, val id: Int) : TagStore.Companion.StoreData {

    fun withMode(newMode: ModeHandler.Mode) = StorePlotState(newMode.ordinal, id)

    companion object : TagStore.Companion.StoreComp {
        override fun defaultFunc(missing: String): NBT {
            return when(missing) {
                "mode" -> NBT.Int(0)
                "id" -> throw RuntimeException("Player not in a plot!")
                else -> throw RuntimeException("Unexpected value: $missing")
            }
        }

        fun usingPlay(state: StorePlotState?) = state?.mode == ModeHandler.Mode.PLAY.ordinal
        fun usingBuild(state: StorePlotState?) = state?.mode == ModeHandler.Mode.BUILD.ordinal
        fun usingDev(state: StorePlotState?) = state?.mode == ModeHandler.Mode.DEV.ordinal
    }
}
