package cn.chawloo.base.ext

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * TODO
 * @author Create by 鲁超 on 2022/5/9 0009 16:14:21
 *----------Dragon be here!----------/
 *       ┌─┐      ┌─┐
 *     ┌─┘─┴──────┘─┴─┐
 *     │              │
 *     │      ─       │
 *     │  ┬─┘   └─┬   │
 *     │              │
 *     │      ┴       │
 *     │              │
 *     └───┐      ┌───┘
 *         │      │神兽保佑
 *         │      │代码无BUG！
 *         │      └──────┐
 *         │             ├┐
 *         │             ┌┘
 *         └┐ ┐ ┌───┬─┐ ┌┘
 *          │ ┤ ┤   │ ┤ ┤
 *          └─┴─┘   └─┴─┘
 *─────────────神兽出没───────────────/
 */
@OptIn(ExperimentalSerializationApi::class)
val json = Json {
    encodeDefaults = true//编码默认值
    ignoreUnknownKeys = true//忽略未知键
    coerceInputValues = true//强制输入值，如果json属性与对象格式不符，则使用对象默认值
    explicitNulls = true //序列化时是否忽略null
    isLenient = true //宽松解析，json格式异常也可解析，如：{name:"小红",age:"18"} + Person(val name:String,val age:Int) ->Person("小红",18)
}

inline fun <reified T> T.toJson(): String = json.encodeToString(this)

inline fun <reified T> String.fromJson(): T = json.decodeFromString(this)

fun String.fromJson(): JsonObject = json.decodeFromString(this)
fun JsonObject.getString(key: String, default: String = ""): String {
    return this[key]?.jsonPrimitive?.content ?: default
}

fun JsonObject.getInt(key: String, default: Int = 0): Int {
    return this[key]?.jsonPrimitive?.content?.toIntOrNull() ?: default
}

fun JsonObject.getBoolean(key: String, default: Boolean = false): Boolean {
    return this[key]?.jsonPrimitive?.content?.toBoolean() ?: default
}