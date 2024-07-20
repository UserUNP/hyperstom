package ma.userunp.hyperstom

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import ma.userunp.hyperstom.code.CodeValType
import ma.userunp.hyperstom.code.*
import ma.userunp.hyperstom.code.impl.*

//TODO: resource(s) for translation keys

interface Named { val name: String }

interface NamedRegistry<T : Named> : Named {
    val map: MutableMap<String, T>
    fun get(name: String): T
}

private fun <T : Named> implRegistry(n: String, vararg objs: T) = object : NamedRegistry<T> {
    override val name = n
    override val map = objs.associateByTo(Object2ObjectOpenHashMap(objs.size), Named::name)
    override fun get(name: String) = map[name] ?: throw RuntimeException("No such name $name! ${this.name}")
}

object RegistryLabelTypes : NamedRegistry<CodeLabelType<*>> by implRegistry("LABEL TYPES",
    LabelDataType, LabelScopedType, LabelEventType
)

object RegistryValTypes : NamedRegistry<CodeValType<*>> by implRegistry("VAL TYPES",
    ValTypeNull, ValTypeStr, ValTypeNum, ValTypeBool, ValTypeList, ValTypeExpr,
    ValTypeParamType, ValTypeLabelType, ValTypeLabel, ValTypeTarget, ValTypeEventVal, ValTypeVar, ValTypeParam, ValTypeGlobal,
    ValTypeTxt, ValTypeMat, ValTypePot, ValTypeVfx, ValTypeSnd, ValTypeItem,
)

object RegistryParamTypes : NamedRegistry<ParamType<*>> by implRegistry("PARAM TYPES",
    ParamTypeAny, ParamTypeStr, ParamTypeNum, ParamTypeBool,
    ParamTypeLabel, ParamTypeTarget, ParamTypeEventVal, ParamTypeVar,
    ParamTypeTxt, ParamTypeMat, ParamTypePot, ParamTypeVfx, ParamTypeSnd, ParamTypeItem,
)

object RegistryTargets : NamedRegistry<EventTarget<*>> by implRegistry("TARGETS",
    TargetNone, TargetPlayersAll, TargetPlayerRand, TargetNPCAll, TargetNPCRand, TargetDefault,
    TargetEntityClicked,
)

object RegistryEventVals : NamedRegistry<EventVal<*, *>> by implRegistry("EVENT VALS",
    EventValChatMsg,
)

object RegistryInstTypes : NamedRegistry<InstType> by implRegistry("INST TYPES",
    // debug
    InstDebugFrame,
    // control
    InstCallFunction,
    // var
    InstAssignVar, InstToTxt,
    // entity
    InstSendMessage, InstSendTitle, InstGiveItems, InstDamage

)
