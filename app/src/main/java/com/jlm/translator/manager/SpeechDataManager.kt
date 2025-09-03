package com.jlm.translator.manager

import cn.chawloo.base.ext.toast
import com.jlm.translator.intelligent.model.SpeechErrorEnum
import com.safframework.log.L
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

/**
 * 数据状态
 */
sealed class SpeechState {
    object RecordStart: SpeechState()
    object RecordStop: SpeechState()
    object RecordRelease: SpeechState()
    data class Recognized(val result: String) : SpeechState() // 识别结果
    data class Recognizing(val result: String) : SpeechState() // 识别中
    data class ConversationResult(val result: String) : SpeechState() // 会话结果
    data class ConversationChanging(val result: String) : SpeechState() // 会话中
    data class LanguageIdentified(val result: String, val langKey: String) : SpeechState() // 语言识别结果
    data class RecognizedAndTranslated(val result: String, val transResults: ArrayList<String>) : SpeechState() // 识别并翻译结果
    data class LangIdentiRecogAndTranslated(val result: String, val recogKey: String, val transResults: Map<String, String>) : SpeechState() //语种识别 并返回识别结果和翻译结果
    object TranslateStart: SpeechState() // 翻译开始
    data class Translating(val result: String) : SpeechState() // 翻译中（实时结果）
    data class Translated(val result: String, val index: Int, val langMode: LanguageModeEnum) : SpeechState()
    data class TranslationCompleted(val result: String, val result1: String, val index: Int, val langMode: LanguageModeEnum) : SpeechState()
    object SynthesisStart: SpeechState()
    object SynthesisCompleted: SpeechState()
    data class AudioPlayStart(val type: Int) : SpeechState()
    data class AudioPlayOver(val type: Int) : SpeechState()
    object TaskFinish: SpeechState() // 任务完成
    data class TaskError(val error: SpeechErrorEnum): SpeechState() // 任务错误
    data class Error(val message: String): SpeechState() // 通用错误
}

/**
 * 智能语音数据管理类，主要用于语音数据的处理和分发，包括识别结果、翻译结果和错误信息等。
 */
class SpeechDataManager {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    // 使用 SharedFlow 处理识别结果，多个观察者可以共享数据流
    private val _speechDataFlow = MutableSharedFlow<SpeechState>(
        replay = 0,
        extraBufferCapacity = 32,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val speechDataFlow = _speechDataFlow.asSharedFlow()

    var job: Job? = null

    companion object {
        const val TAG = "SpeechDataManager"

        @Volatile
        private var instance: SpeechDataManager? = null

        fun getInstance(): SpeechDataManager {
            return instance ?: synchronized(this) {
                instance ?: SpeechDataManager().also { instance = it }
            }
        }
    }

    suspend fun emit(state: SpeechState) {
        job = scope.launch {
            _speechDataFlow.emit(state)
        }
    }

    fun tryEmit(state: SpeechState) {
//        L.d(TAG, "tryEmit: $state")
//        toast("发送数据")
        val success = _speechDataFlow.tryEmit(state)
        if (!success) {
            toast("发送数据失败")
        }
    }

    /**
     * 发送识别结果
     */
    suspend fun sendRecognitionResult(text: String) {
//        _recognitionFlow.emit(RecognitionState.Recognizing(text))
    }

    /**
     * 发送翻译结果
     */
    suspend fun sendTranslationResult(text: String) {
//        _recognitionFlow.emit(RecognitionState.Translated(text))
    }

    /**
     * 发送错误信息
     */
    suspend fun sendError(error: String) {
//        _recognitionFlow.emit(RecognitionState.Error(error))
    }

    /**
     * 模拟语音识别过程
     */
    fun startRecognition() {
//        scope.launch {
//            try {
//                // 模拟实时识别过程
//                sendRecognitionResult("正在识别...")
//                delay(1000)
//                sendRecognitionResult("你好")
//                delay(500)
//
//                // 模拟翻译过程
//                sendTranslationResult("Hello")
//            } catch (e: Exception) {
//                sendError(e.message ?: "Unknown error")
//            }
//        }
    }

    /**
     * 释放资源
     */
    fun release() {
        job?.cancel()
        scope.cancel()
    }
}