package com.jlm.translator.utils

import cn.chawloo.base.ext.toast
import com.safframework.log.L

/**
 * 智能语音错误信息实体类
 * */
class SpeechErrorResult(
    var code: Int,
    var message: String = "",
    var modeType: Int = modeType_speech, //模型类型，如 语音识别、翻译、合成
    var errorType: Int = 0, //具体问题的类型
    ){

    companion object{
        const val modeType_speech = 1 //语音识别
        const val modeType_translate = 2 //文字翻译
        const val modeType_synthesis = 3 //语音合成
        const val modeType_normal = 4 //普通类型，不需要stop语音录制

        //自定义的code
        const val code_recordNotInit = 1000
        const val code_azureRecordError = 1001 //azure的语音识别错误码

        const val code_translateResponseNull = 100 //翻译返回空的
        const val code_translateResponseError = 101 //返回数据异常

        const val code_normalLangIdentifyError = 2000 //语种识别异常
        const val code_normalLangMatchError = 2001 //语种匹配问题

        const val code_synthesisPlayError = 2500 //语音播报异常
    }

    fun toastMsg() {
        toast(getErrorMessage())
    }

    fun getErrorMessage(): String {
        var msg: String = ""
        when(modeType) {
            modeType_speech -> {
                msg = getSpeechMsg()
            }
            modeType_translate -> {
                msg = getTranslateMsg()
            }
            modeType_synthesis -> {
                msg = getSynthesisMsg()
            }
            modeType_normal -> {
                msg = getNormalMsg()
            }
        }
        return msg
    }

    private fun getSpeechMsg(): String {
        L.d("1111--$code--$modeType")
        var msg: String = "${code}:"
        msg += when(code) {
            //配置或参数错误
            240999 -> "内部默认错误"
            240001 -> "配置文件错误"
            in 240002..240005 -> "参数异常"
            240007 -> "无有效对话实例，一般在内部状态错误时发生"
            240008 -> "无有效引擎实例，请检查是否初始化成功"
            240009 -> "传入音频数据长度异常"
            //SDK状态错误
            240010 -> "退出后调用SDK接口"
            240011 -> "SDK未正确初始化"
            240012 -> "重复调用SDK初始化接口"
            in 240013..240014 -> "内部状态错误"
            240015 -> "该模式无法调用接口"
            //系统调用错误
            240020 -> "内存分配错误,请检查内存是否不足"
            240021,240022 -> "文件访问错误,请检查App的读写权限"
            240030 -> "系统资源不足，引擎创建失败"
            240040 -> "请确认本地引擎的模型是否有效、目录是否可读写"
            240051 -> "确认推送的音频长度是否非法"
            240052 -> "连续2s未获取到音频"
            //网络错误
            240062 -> "服务端发生错误"
            240063,240064,240065,240066,240067,240068,240069 -> "请检查翻译机网络连接是否正常"
            240070 -> "鉴权失败，请检查传入的key是否正确"
            240071 -> "使用客户端传入的IP连接失败"
            //自定义code
            1000 -> "录音未开启"
            1001 -> message
            else -> "未知错误，请退出当前页面，重新进入"
        }
        return msg
    }

    private fun getTranslateMsg(): String {
        var msg: String = "${code}:"
        msg += when(code) {
            code_translateResponseNull -> "翻译失败，请检查网络"
            code_translateResponseError -> message
            else -> "未知的翻译问题"
        }
        return msg
    }

    private fun getSynthesisMsg(): String {
        var msg: String = "${code}:"
        msg += when (code) {
            code_synthesisPlayError -> "语音播报异常，请检查token、voicer或网络是否正常"
            else -> "未知的语音播报异常"
        }
        return msg
    }

    private fun getNormalMsg(): String {
        var msg: String = "${code}:"
        msg += when(code) {
            code_normalLangIdentifyError -> msg
            code_normalLangMatchError -> msg
            else -> "出现了其他异常问题，请联系管理员"
        }
        return msg
    }

}
