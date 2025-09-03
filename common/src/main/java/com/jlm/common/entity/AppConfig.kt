package com.jlm.common.entity

import android.os.Parcelable
import cn.chawloo.base.ext.appVersionCode
import cn.chawloo.base.model.IUpdateContract
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

/**
 * AppConfig
 */
@Parcelize
@Serializable
data class AppConfig(
    /**
     * 接⼝基本地址
     */
    val ApiUrl: String = "",

    /**
     * 版本号
     * */
    val Version: String = "",

    /**
     * 新版本下载地址
     */
    val apkLink: String? = null,

    /**
     * 更新内容说明，即升级的功能项
     */
    val description: String? = null,

    /**
     * 升级类型（0：默认不升级，1：升级不强制 2：强制升级）
     */
    val updateLevel: Int = UpdateTypeEnum.NORMAL,

    /**
     * 升级的版本号
     */
    val VersionCode: Int = 0,
) : Parcelable, IUpdateContract {
    override val needUpdate get() = VersionCode > appVersionCode
    override val ver: String get() = Version
    override val verInfo: String
        get() = description?.takeIf { it.isNotBlank() } ?: """更新说明：
            |1.修复部分已知问题
            |2.优化性能
            |""".trimMargin()
    override val isForceUpdate get() = updateLevel == UpdateTypeEnum.FORCE_UPDATE
    override val url: String? get() = apkLink
    override val apkName
        get() = try {
            url?.run { substring(lastIndexOf("/") + 1, lastIndexOf("?") - 1).ifBlank { "temp.apk" } } ?: "temp.apk"
        } catch (e: Exception) {
            "temp.apk"
        }

    companion object {
        const val APP_CONFIG = "app_config"

        const val NORMAL_THEME = 0
        const val MOURN_THEME = 1 //悼念主题，首页置灰
    }
}

object UpdateTypeEnum {
    const val NORMAL = 1 //普通不升级
    const val UPDATE = 2 //提示升级
    const val FORCE_UPDATE = 3 //强制升级
}
