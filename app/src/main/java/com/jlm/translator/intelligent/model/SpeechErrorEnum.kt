package com.jlm.translator.intelligent.model

import cn.chawloo.base.ext.toast

enum class ErrorLevelEnum {
    FATAL,
    CRITICAL,
    ERROR,
    WARN,
    INFO
}

/**
 * 智能语音错误信息实体类
 * */
enum class SpeechErrorEnum(
    val code: Int,
    val message: String,
    val errorLevel: ErrorLevelEnum //模型类型，如 语音识别、翻译、合成
) {
    // 配置或参数错误
    internal_default_error(240999, "内部默认错误", ErrorLevelEnum.ERROR),
    config_file_error(240001, "配置文件错误", ErrorLevelEnum.ERROR),
    parameter_exception(240002, "参数异常", ErrorLevelEnum.ERROR),
    parameter_exception_2(240003, "参数异常", ErrorLevelEnum.ERROR),
    parameter_exception_3(240004, "参数异常", ErrorLevelEnum.ERROR),
    parameter_exception_4(240005, "参数异常", ErrorLevelEnum.ERROR),
    invalid_dialog_instance(240007, "无有效对话实例，一般在内部状态错误时发生", ErrorLevelEnum.ERROR),
    invalid_engine_instance(240008, "无有效引擎实例，请检查是否初始化成功", ErrorLevelEnum.ERROR),
    audio_data_length_exception(240009, "传入音频数据长度异常", ErrorLevelEnum.ERROR),
    // SDK状态错误
    sdk_interface_call_after_exit(240010, "退出后调用SDK接口", ErrorLevelEnum.ERROR),
    sdk_not_correctly_initialized(240011, "SDK未正确初始化", ErrorLevelEnum.ERROR),
    sdk_initialization_interface_called_again(240012, "重复调用SDK初始化接口", ErrorLevelEnum.ERROR),
    internal_state_error(240013, "内部状态错误", ErrorLevelEnum.ERROR),
    internal_state_error_2(240014, "内部状态错误", ErrorLevelEnum.ERROR),
    interface_call_not_allowed_in_mode(240015, "该模式无法调用接口", ErrorLevelEnum.ERROR),
    // 系统调用错误
    memory_allocation_error(240020, "内存分配错误,请检查内存是否不足", ErrorLevelEnum.ERROR),
    file_access_error(240021, "文件访问错误,请检查App的读写权限", ErrorLevelEnum.ERROR),
    file_access_error_2(240022, "文件访问错误,请检查App的读写权限", ErrorLevelEnum.ERROR),
    system_resource_insufficient(240030, "系统资源不足，引擎创建失败", ErrorLevelEnum.ERROR),
    local_engine_model_invalid(240040, "请确认本地引擎的模型是否有效、目录是否可读写", ErrorLevelEnum.ERROR),
    pushed_audio_length_illegal(240051, "确认推送的音频长度是否非法", ErrorLevelEnum.ERROR),
    no_audio_received(240052, "连续2s未获取到音频", ErrorLevelEnum.ERROR),
    // 网络错误
    server_error(240062, "服务端发生错误", ErrorLevelEnum.ERROR),
    network_connection_error(240063, "请检查翻译机网络连接是否正常", ErrorLevelEnum.ERROR),
    network_connection_error_2(240064, "请检查翻译机网络连接是否正常", ErrorLevelEnum.ERROR),
    network_connection_error_3(240065, "请检查翻译机网络连接是否正常", ErrorLevelEnum.ERROR),
    network_connection_error_4(240066, "请检查翻译机网络连接是否正常", ErrorLevelEnum.ERROR),
    network_connection_error_5(240067, "请检查翻译机网络连接是否正常", ErrorLevelEnum.ERROR),
    network_connection_error_6(240068, "请检查翻译机网络连接是否正常", ErrorLevelEnum.ERROR),
    network_connection_error_7(240069, "请检查翻译机网络连接是否正常", ErrorLevelEnum.ERROR),
    authentication_failed(240070, "鉴权失败，请检查传入的key是否正确", ErrorLevelEnum.ERROR),
    client_ip_connection_failed(240071, "使用客户端传入的IP连接失败", ErrorLevelEnum.ERROR),
    level_error_default(240000, "内部错误", ErrorLevelEnum.ERROR),
    // 自定义code
    recog_params_error(1003, "识别参数错误", ErrorLevelEnum.ERROR),
    record_not_init(1000, "录音未开启", ErrorLevelEnum.ERROR),
    recog_init_error(1002, "语音识别初始化失败", ErrorLevelEnum.ERROR),
    recog_lang_error(1004, "请检查语言参数", ErrorLevelEnum.ERROR),
    azure_record_error(1001, "azure的语音识别错误码", ErrorLevelEnum.ERROR),
    translate_response_null(100, "翻译失败，请检查网络环境", ErrorLevelEnum.INFO),
    translate_response_error(101, "翻译结果异常,请检查语言", ErrorLevelEnum.INFO),
    normal_lang_identify_error(2000, "语种识别异常", ErrorLevelEnum.WARN),
    normal_lang_match_error(2001, "识别语种不匹配", ErrorLevelEnum.WARN),
    synthesis_play_error(2500, "语音播报异常，请检查token、voicer或网络是否正常", ErrorLevelEnum.WARN),
    recogAndTrans_cancel_error(2501, "语音识别取消", ErrorLevelEnum.ERROR),
    recogIdenti_lang_error(2510, "请检查语言参数", ErrorLevelEnum.ERROR),

    synthesis_param_error(2600, "语音播报参数异常", ErrorLevelEnum.ERROR),
    synthesis_play_error_2(2601, "语音播报异常", ErrorLevelEnum.ERROR),
    synthesis_init_error(2602, "语音播报初始化错误", ErrorLevelEnum.ERROR),
    synthesis_cancel_error(2603, "语音合成取消", ErrorLevelEnum.ERROR);


    companion object {

//        fun fromCode(code: Int, modeType: Int, message: String = ""): SpeechErrorResult? {
//            return values().find { it.code == code && it.modeType == modeType } ?: when (modeType) {
//                modeType_speech -> INTERNAL_DEFAULT_ERROR
//                modeType_translate -> TRANSLATE_RESPONSE_NULL
//                modeType_synthesis -> SYNTHESIS_PLAY_ERROR
//                modeType_normal -> NORMAL_LANG_IDENTIFY_ERROR
//                else -> null
//            }
//        }
    }

    fun toastMsg() {
        toast(getErrorMessage())
    }

    fun getErrorMessage(): String {
        return "$code: $message"
    }

    fun needStop(): Boolean{
        toast(getErrorMessage())
        return when (this.errorLevel) {
            ErrorLevelEnum.FATAL, ErrorLevelEnum.CRITICAL ,ErrorLevelEnum.ERROR  -> true
            else -> false
        }
    }
}