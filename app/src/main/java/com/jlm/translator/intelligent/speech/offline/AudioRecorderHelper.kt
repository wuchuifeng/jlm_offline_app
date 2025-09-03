package com.jlm.translator.intelligent.speech.offline

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Process
import android.util.Log
import androidx.core.app.ActivityCompat

/**
 * 音频录制帮助类
 * 用于处理实时音频录制和数据回调
 */
class AudioRecorderHelper(private val context: Context) {
    
    companion object {
        private const val TAG = "AudioRecorderHelper"
        
        // 音频录制参数
        private const val SAMPLE_RATE = 16000 // 采样率
        private const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO // 单声道
        private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT // 16位PCM编码
        private const val AUDIO_SOURCE = MediaRecorder.AudioSource.MIC // 音频源
        
        /**
         * 获取采样率
         * @return 采样率
         */
        fun getSampleRate(): Int = SAMPLE_RATE
    }
    
    // 录制相关变量
    private var audioRecord: AudioRecord? = null
    private var recordThread: Thread? = null
    private var isRecording = false
    private var bufferSize: Int = 0
    private var audioDataCallback: AudioDataCallback? = null
    
    /**
     * 音频数据回调接口
     */
    interface AudioDataCallback {
        /**
         * 音频数据回调
         * @param audioData 音频数据
         * @param length 数据长度
         */
        fun onAudioData(audioData: ByteArray, length: Int)
        
        /**
         * 录音错误回调
         * @param errorMsg 错误信息
         */
        fun onError(errorMsg: String)
        
        /**
         * 录音状态回调
         * @param isRecording 是否正在录音
         */
        fun onRecordingStateChanged(isRecording: Boolean)
    }
    
    init {
        initAudioRecord()
    }
    
    /**
     * 初始化AudioRecord
     */
    private fun initAudioRecord() {
        try {
            // 计算最小缓冲区大小
            bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT)
            if (bufferSize == AudioRecord.ERROR_BAD_VALUE || bufferSize == AudioRecord.ERROR) {
                Log.e(TAG, "无法获取AudioRecord最小缓冲区大小")
                return
            }
            
            // 检查录音权限
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "缺少录音权限")
                return
            }
            
            // 创建AudioRecord实例
            audioRecord = AudioRecord(
                AUDIO_SOURCE,
                SAMPLE_RATE,
                CHANNEL_CONFIG,
                AUDIO_FORMAT,
                bufferSize * 2 // 使用2倍缓冲区大小以确保稳定性
            )
            
            audioRecord?.let { record ->
                if (record.state != AudioRecord.STATE_INITIALIZED) {
                    Log.e(TAG, "AudioRecord初始化失败")
                    audioRecord = null
                } else {
                    Log.i(TAG, "AudioRecord初始化成功，缓冲区大小: $bufferSize")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "初始化AudioRecord异常: ${e.message}")
            e.printStackTrace()
        }
    }
    
    /**
     * 设置音频数据回调
     * @param callback 回调接口
     */
    fun setAudioDataCallback(callback: AudioDataCallback?) {
        this.audioDataCallback = callback
    }
    
    /**
     * 开始录音
     * @return 是否成功开始录音
     */
    fun startRecording(): Boolean {
        val record = audioRecord
        if (record == null) {
            Log.e(TAG, "AudioRecord未初始化")
            audioDataCallback?.onError("AudioRecord未初始化")
            return false
        }
        
        if (isRecording) {
            Log.w(TAG, "录音已在进行中")
            return true
        }
        
        return try {
            record.startRecording()
            isRecording = true
            
            // 启动录音线程
            recordThread = Thread(recordRunnable, "AudioRecordThread").apply {
                start()
            }
            
            Log.i(TAG, "开始录音")
            audioDataCallback?.onRecordingStateChanged(true)
            true
        } catch (e: Exception) {
            Log.e(TAG, "开始录音异常: ${e.message}")
            e.printStackTrace()
            audioDataCallback?.onError("开始录音失败: ${e.message}")
            false
        }
    }
    
    /**
     * 停止录音
     */
    fun stopRecording() {
        if (!isRecording) {
            Log.w(TAG, "录音未在进行中")
            return
        }
        
        isRecording = false
        
        try {
            audioRecord?.stop()
            
            // 等待录音线程结束
            recordThread?.let { thread ->
                thread.interrupt()
                recordThread = null
            }
            
            Log.i(TAG, "停止录音")
            audioDataCallback?.onRecordingStateChanged(false)
        } catch (e: Exception) {
            Log.e(TAG, "停止录音异常: ${e.message}")
            e.printStackTrace()
        }
    }
    
    /**
     * 释放资源
     */
    fun release() {
        stopRecording()
        
        audioRecord?.let { record ->
            record.release()
            audioRecord = null
            Log.i(TAG, "AudioRecord资源已释放")
        }
    }
    
    /**
     * 检查是否正在录音
     * @return 是否正在录音
     */
    fun isRecording(): Boolean = isRecording
    
    /**
     * 录音线程
     */
    private val recordRunnable = Runnable {
        // 设置线程优先级
        Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO)
        
        val buffer = ByteArray(320) // 每次读取320字节（20ms@16kHz）
        
        while (isRecording && !Thread.currentThread().isInterrupted) {
            try {
                val readBytes = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                
                when {
                    readBytes > 0 -> {
                        // 回调音频数据
                        audioDataCallback?.onAudioData(buffer, readBytes)
                    }
                    readBytes == AudioRecord.ERROR_INVALID_OPERATION -> {
                        Log.e(TAG, "录音读取数据错误: ERROR_INVALID_OPERATION")
                        audioDataCallback?.onError("录音读取数据错误")
                        break
                    }
                    readBytes == AudioRecord.ERROR_BAD_VALUE -> {
                        Log.e(TAG, "录音读取数据错误: ERROR_BAD_VALUE")
                        audioDataCallback?.onError("录音参数错误")
                        break
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "录音线程异常: ${e.message}")
                audioDataCallback?.onError("录音异常: ${e.message}")
                break
            }
        }
        
        Log.i(TAG, "录音线程结束")
    }
    
    /**
     * 获取当前录音状态
     * @return 录音状态描述
     */
    fun getRecordingState(): String {
        val record = audioRecord ?: return "未初始化"
        
        return when (record.recordingState) {
            AudioRecord.RECORDSTATE_STOPPED -> "已停止"
            AudioRecord.RECORDSTATE_RECORDING -> "录音中"
            else -> "未知状态"
        }
    }
}