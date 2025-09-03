package cn.chawloo.base.model

/**
 * 更新用实体接口
 * @author Create by 鲁超 on 2020/11/19 0019 15:09
 */
interface IUpdateContract {
    val ver: String
    val verInfo: String
    val isForceUpdate: Boolean
    val url: String?
    val needUpdate: Boolean
    val apkName
        get() = try {
            url?.run { substring(lastIndexOf("/") + 1, lastIndexOf("?") - 1).ifBlank { "temp.apk" } } ?: "temp.apk"
        } catch (e: Exception) {
            "temp.apk"
        }
}