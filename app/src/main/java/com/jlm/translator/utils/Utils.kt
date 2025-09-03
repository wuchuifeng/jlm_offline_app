package com.jlm.translator.utils

import com.safframework.log.L
import java.io.File

/**
 * TODO
 * @author Create by 鲁超 on 2024/4/12 09:42
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
object Utils {
    fun createDir(dirPath: String): Int {
        var path = dirPath
        val dir = File(path)
        //文件夹是否已经存在
        if (dir.exists()) {
            L.e("The directory [ $path ] has already exists")
            return 1
        }

        if (!path.endsWith(File.separator)) { //不是以 路径分隔符 "/" 结束，则添加路径分隔符 "/"
            path += File.separator
        }

        //创建文件夹
        if (dir.mkdirs()) {
            L.e("create directory [ $path ] success")
            return 0
        }

        L.e("create directory [ $path ] failed")
        return -1
    }

    fun getMsgWithErrorCode(code: Int, status: String): String {
        var str = "错误码:$code"
        str += when (code) {
            140001 -> " 错误信息: 引擎未创建, 请检查是否成功初始化, 详情可查看运行日志."
            140008 -> " 错误信息: 鉴权失败, 请关注日志中详细失败原因."
            140011 -> " 错误信息: 当前方法调用不符合当前状态, 比如在未初始化情况下调用pause接口."
            140013 -> " 错误信息: 当前方法调用不符合当前状态, 比如在未初始化情况下调用pause接口."
            140900 -> " 错误信息: tts引擎创建失败, 请检查资源路径和资源文件是否正确."
            140901 -> " 错误信息: tts引擎初始化失败, 请检查使用的SDK是否支持离线语音合成功能."
            140903 -> " 错误信息: tts引擎创建失败, 请检查资源路径和资源文件是否正确."
            140908 -> " 错误信息: 发音人资源无法获得正确采样率, 请检查发音人资源是否正确."
            140910 -> " 错误信息: 发音人资源路径无效, 请检查发音人资源文件路径是否正确."
            144003 -> " 错误信息: token过期或无效, 请检查token是否有效."
            144006 -> " 错误信息: 云端返回未分类错误, 请看详细的错误信息."
            170008 -> " 错误信息: 鉴权成功, 但是存储鉴权信息的文件路径不存在或无权限."
            170806 -> " 错误信息: 请设置SecurityToken."
            170807 -> " 错误信息: SecurityToken过期或无效, 请检查SecurityToken是否有效."
            240005 -> if (status === "init") {
                " 错误信息: 请检查appkey、akId、akSecret等初始化参数是否无效或空."
            } else {
                " 错误信息: 传入参数无效, 请检查参数正确性."
            }

            240011 -> " 错误信息: SDK未成功初始化."
            240068 -> " 错误信息: 403 Forbidden, token无效或者过期."
            240070 -> " 错误信息: 鉴权失败, 请查看日志确定具体问题, 特别是关注日志 E/iDST::ErrMgr: errcode=."
            41010105 -> " 错误信息: 长时间未收到人声，触发静音超时."
            999999 -> " 错误信息: 库加载失败, 可能是库不支持当前activity, 或库加载时崩溃, 可详细查看日志判断."
            else -> " 未知错误信息, 请查看官网错误码和运行日志确认问题."
        }
        return str
    }
}