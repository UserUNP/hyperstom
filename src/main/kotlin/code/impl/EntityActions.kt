package ma.userunp.hyperstom.code.impl

import ma.userunp.hyperstom.code.*
import net.kyori.adventure.title.Title
import net.kyori.adventure.util.Ticks
import net.minestom.server.entity.damage.DamageType
import net.minestom.server.inventory.TransactionOption

// communication

object InstSendMessage : InstType {
    override val name = "MSG"
    override val targetClass = TargetClass.PLAYER
    private val msgParam = ParamSingle("msg", ParamTypeTxt)
    override val params = arrayOf(msgParam)
    override fun exec(ctx: InstContext) = ctx.target.sendMessage(ctx.arg(msgParam)[0])
}

object InstSendTitle : InstType {
    override val name = "TITLE"
    override val targetClass = TargetClass.PLAYER
    private val titleParam = ParamSingle("title", ParamTypeTxt)
    private val subtitleParam = ParamSingle("subtitle", ParamTypeTxt)
    private val inTicksParam = ParamSingle("in", ParamOptType(ParamTypeNum, ValTypeNum.get(10f)))
    private val stayTicksParam = ParamSingle("stay", ParamOptType(ParamTypeNum, ValTypeNum.get(70f)))
    private val outTicksParam = ParamSingle("out", ParamOptType(ParamTypeNum, ValTypeNum.get(20f)))
    override val params = arrayOf(titleParam, subtitleParam)
    override fun exec(ctx: InstContext) = ctx.target.showTitle(Title.title(
        ctx.arg(titleParam)[0],
        ctx.arg(subtitleParam)[0],
        Title.Times.times(
            Ticks.duration(ctx.arg(inTicksParam)[0].toLong()),
            Ticks.duration(ctx.arg(stayTicksParam)[0].toLong()),
            Ticks.duration(ctx.arg(outTicksParam)[0].toLong()),
        ),
    ))
}

// inventory management

object InstGiveItems : InstType {
    override val name = "GIVE"
    override val targetClass = TargetClass.PLAYER //TODO: living non player entities are a thing
    private val itemsParam = ParamMulti("items", ParamTypeItem)
    override val params = arrayOf(itemsParam)
    override fun exec(ctx: InstContext) {
        for (p in ctx.target.players()) p.inventory.addItemStacks(ctx.arg(itemsParam), TransactionOption.ALL)
    }
}

// statistics

object InstDamage : InstType {
    override val name = "DMG"
    override val targetClass = TargetClass.ALL
    private val amountParam = ParamSingle("amount", ParamTypeNum)
    override val params = arrayOf(amountParam)
    override fun exec(ctx: InstContext) {
        for (e in ctx.target.livingEntities()) e.damage(DamageType.GENERIC, ctx.arg(amountParam)[0])
    }
}
