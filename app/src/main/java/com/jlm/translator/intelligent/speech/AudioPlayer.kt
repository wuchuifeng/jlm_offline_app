package com.jlm.translator.intelligent.speech

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Handler
import android.os.Looper
import cn.chawloo.base.ext.toast
import com.jlm.translator.utils.LogUtil
import com.safframework.log.L
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.max

/**
 * AudioPlayer 单例类 - 确保应用中只有一个活动的音频播放实例
 */
object AudioPlayer {
    private const val TAG = "AudioPlayer"
    
    // 配置常量
    private const val QUEUE_POLL_TIMEOUT_MS = 20L
    private const val PROCESSING_DELAY_MS = 10L
    private const val BUFFER_SIZE_MULTIPLIER = 6
    private const val MIN_BUFFER_SIZE = 32768
    private const val MAX_RETRY_ATTEMPTS = 2

    // 播放状态枚举
    enum class PlayState {
        IDLE,       // 空闲状态
        PLAYING,    // 正在播放
        PAUSED,
        STOPED,     // 已暂停
        RELEASED    // 已释放资源
    }

    // 声道类型
    enum class ChannelType {
        LEFT, RIGHT, STEREO
    }

    // 回调接口
    interface Callback {
        fun onPlayStart()
        fun onPlayOver()
    }

    // 音频轨道状态类
    data class TrackState(
        var audioTrack: AudioTrack? = null,
        var playState: PlayState = PlayState.RELEASED,
        val queue: LinkedBlockingQueue<ByteArray> = LinkedBlockingQueue(),
        var leftVolume: Float = 1.0f,
        var rightVolume: Float = 1.0f,
        var job: Job ? = null
    )

    // 左右声道状态管理
    private val leftState = TrackState() //左声道
    private val rightState = TrackState() //右声道
    private val stereoState = TrackState() //全声道

    // 音频参数
    private var sampleRate = 24000
    private var channels = AudioFormat.CHANNEL_OUT_MONO
    private var encoding = AudioFormat.ENCODING_PCM_16BIT

    // 状态控制
//    private val isInitialized = AtomicBoolean(false)
    private val callback: AtomicReference<Callback?> = AtomicReference(null)
    private val playStates = AtomicReference<PlayState>(PlayState.RELEASED)

    // 协程作用域
    private val playerScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // 处理器线程
    private val mainHandler = Handler(Looper.getMainLooper())

    /**
     * 初始化 AudioPlayer - 优化版本
     * @param cb 回调接口
     */
    fun initialize(channelType: ChannelType, cb: Callback) {
        val trackState = getTrackState(channelType)
        
        // 如果已经初始化且状态正常，只更新回调
        if (trackState.audioTrack?.state == AudioTrack.STATE_INITIALIZED) {
            callback.set(cb)
            if (trackState.job?.isActive != true) {
                startTrackJob(trackState)
            }
            return
        }
        
        // 创建新的音频轨道
        if (createTracks(channelType)) {
            startTrackJob(trackState)
        }
        
        callback.set(cb)
    }

    /**
     * 创建双轨道播放器
     * @return 创建是否成功
     */
    private fun createTracks(channelType: ChannelType): Boolean {
        return try {
            when (channelType) {
                ChannelType.LEFT -> {
                    leftState.audioTrack = createTrack()
                    leftState.leftVolume = 1.0f
                    leftState.rightVolume = 0f
                    applyVolumeSettings(leftState)
                }
                ChannelType.RIGHT -> {
                    rightState.audioTrack = createTrack()
                    rightState.rightVolume = 1.0f
                    rightState.leftVolume = 0f
                    applyVolumeSettings(rightState)
                }
                ChannelType.STEREO -> {
                    stereoState .audioTrack = createTrack()
                    stereoState.leftVolume = 1.0f
                    stereoState.rightVolume = 1.0f
                    applyVolumeSettings(stereoState)
                }
            }
            true
        } catch (e: Exception) {
            LogUtil.e(TAG, "创建音频轨道失败: ${e.message}")
            release()
            false
        }
    }

    /**
     * 创建单个AudioTrack
     * @return 创建的音频轨道
     */
    private fun createTrack(): AudioTrack {
        val minBufferSize = AudioTrack.getMinBufferSize(sampleRate, channels, encoding)
        if (minBufferSize <= 0) {
            throw RuntimeException("不支持的音频配置: sampleRate=$sampleRate, channels=$channels, encoding=$encoding")
        }

        val bufferSize = max(minBufferSize * BUFFER_SIZE_MULTIPLIER, MIN_BUFFER_SIZE)

        return AudioTrack(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build(),
            AudioFormat.Builder()
                .setEncoding(encoding)
                .setSampleRate(sampleRate)
                .setChannelMask(channels)
                .build(),
            bufferSize,
            AudioTrack.MODE_STREAM,
            AudioManager.AUDIO_SESSION_ID_GENERATE
        ).apply {
            if (state != AudioTrack.STATE_INITIALIZED) {
                release()
                throw RuntimeException("AudioTrack初始化失败，状态: $state")
            }
        }
    }

    /**
     * 启动播放协程
     */
//    private fun startPlayerJobs() {
//        startTrackJob(leftState, "LeftTrack")
//        startTrackJob(rightState, "RightTrack")
//    }

    /**
     * 启动单个轨道播放协程 - 优化版本
     */
    private fun startTrackJob(trackState: TrackState) {
        trackState.job?.cancel() // 确保取消之前的协程
        trackState.job = playerScope.launch {
            // L.d(TAG, "播放协程启动")
            while (isActive) {
                try {
                    if (trackState.playState == PlayState.PLAYING) {
                        processTrackQueue(trackState)
                    } else {
                        delay(PROCESSING_DELAY_MS * 2) // 非播放状态延迟更长
                    }
                } catch (e: Exception) {
                    L.e(TAG, "播放协程异常: ${e.message}")
                    delay(20L) // 异常时短暂休息
                }
            }
            // L.d(TAG, "播放协程结束")
        }
    }

    /**
     * 处理轨道队列 - 优化版本
     */
    private suspend fun processTrackQueue(trackState: TrackState) {
        try {
            val data = trackState.queue.poll(QUEUE_POLL_TIMEOUT_MS, TimeUnit.MILLISECONDS) ?: return

            if (!writeTrackData(trackState, data)) {
                retryWrite(trackState, data)
            }
        } catch (e: Exception) {
            LogUtil.e(TAG, "处理音频队列错误: ${e.message}")
        }
    }

    /**
     * 写入音频数据 - 优化版本
     */
    private fun writeTrackData(trackState: TrackState, data: ByteArray): Boolean {
        val track = trackState.audioTrack ?: return false

        return try {
            // 确保AudioTrack处于正确状态
            if (track.state != AudioTrack.STATE_INITIALIZED) {
                return false
            }
            
            // 确保播放状态
            if (track.playState != AudioTrack.PLAYSTATE_PLAYING) {
                track.play()
            }
            
            val written = track.write(data, 0, data.size)
            when {
                written < 0 -> {
                    LogUtil.e(TAG, "音频写入错误: $written")
                    false
                }
                written != data.size -> {
                    // LogUtil.e(TAG, "音频写入不完整: 期望${data.size}, 实际$written")
                    false
                }
                else -> true
            }
        } catch (e: Exception) {
            LogUtil.e(TAG, "音频写入异常: ${e.message}")
            false
        }
    }

    /**
     * 写入失败重试机制
     */
    private suspend fun retryWrite(trackState: TrackState, data: ByteArray, attempts: Int = MAX_RETRY_ATTEMPTS) {
        repeat(attempts) { attemptCount ->
            if (writeTrackData(trackState, data)) return
            delay(50L * (attemptCount + 1))
        }
        notifyError("音频播放失败，达到最大重试次数($attempts)")
    }

    /**
     * 设置音量
     */
    fun setVolume(left: Float, right: Float) {
        val leftVol = left.coerceIn(0.0f, 1.0f)
        val rightVol = right.coerceIn(0.0f, 1.0f)
        
        leftState.leftVolume = leftVol
        leftState.rightVolume = 0f
        rightState.leftVolume = 0f
        rightState.rightVolume = rightVol
        
//        applyVolumeSettings()
    }

    /**
     * 应用音量设置
     */
    private fun applyVolumeSettings(trackState: TrackState) {
        val track = trackState.audioTrack
        if (track == null || track.state == AudioTrack.STATE_UNINITIALIZED) {
            toast("设置声道失败，音频未初始化")
            return
        }
        
        // L.d(TAG, "应用音量设置 - ${trackState}")
        try {
            track.setStereoVolume(trackState.leftVolume, trackState.rightVolume)
        } catch (e: Exception) {
            LogUtil.e(TAG, "设置音量失败: ${e.message}")
        }
    }

    /**
     * 外部接口：传入音频数据 - 不丢弃数据版本
     * @param data 音频数据
     * @param channelType 播放声道类型
     */
    fun feedAudioData(data: ByteArray, channelType: ChannelType = ChannelType.STEREO) {        
        if (data.isEmpty()) {
            // L.w(TAG, "音频数据为空，忽略")
            return
        }
        
        // 直接添加到队列，不丢弃任何数据
        when (channelType) {
            ChannelType.LEFT -> leftState.queue.offer(data)
            ChannelType.RIGHT -> rightState.queue.offer(data)
            ChannelType.STEREO -> stereoState.queue.offer(data)
        }
        
        // 如果队列过大，记录警告但不丢弃数据
        val queueSize = when (channelType) {
            ChannelType.LEFT -> leftState.queue.size
            ChannelType.RIGHT -> rightState.queue.size
            ChannelType.STEREO -> stereoState.queue.size
        }
        
        if (queueSize > 50) {
            // L.w(TAG, "音频队列较大: $queueSize, 声道: $channelType")
        }
    }

    /**
     * 开始播放
     */
    public fun startPlayback(channelType: ChannelType = ChannelType.STEREO) {
        val trackState = getTrackState(channelType)
        if (trackState.playState == PlayState.PLAYING) return
        try {
            val track = trackState.audioTrack
            if (track?.playState != AudioTrack.PLAYSTATE_PLAYING) {
                applyVolumeSettings(trackState)
                track?.play()
            }
            trackState.playState = PlayState.PLAYING
        } catch (e: Exception) {
            LogUtil.e(TAG, "启动播放失败: ${e.message}")
        }
    }

    /**
     * 停止播放
     */
    fun stop(channelType: ChannelType = ChannelType.STEREO) {
        val trackState = getTrackState(channelType)
        stopTrackPlayback(trackState)
    }

    /**
     * 停止单个轨道播放
     */
    private fun stopTrackPlayback(trackState: TrackState) {
        val track = trackState.audioTrack
        try {
            when (track?.playState) {
                AudioTrack.PLAYSTATE_PLAYING -> {
                    // L.d(TAG, "停止播放中的轨道")
                    track.stop()
                    track.flush()
                }
                AudioTrack.PLAYSTATE_PAUSED -> {
                    // L.d(TAG, "停止暂停中的轨道")
                    track.stop()
                    track.flush()
                }
                else -> {
                    // L.d(TAG, "轨道已停止，状态: ${track?.playState}")
                }
            }
        } catch (e: Exception) {
            LogUtil.e(TAG, "停止播放异常: ${e.message}")
        } finally {
            trackState.playState = PlayState.STOPED
            trackState.queue.clear()
            trackState.job?.cancel()
        }
    }

    /**
     * 暂停播放
     */
    fun pause(channelType: ChannelType = ChannelType.STEREO) {
        val  trackState = getTrackState(channelType)
        pauseTrack(trackState)
    }

    /**
     * 暂停单个轨道
     */
    private fun pauseTrack(trackState: TrackState) {
        try {
            trackState.audioTrack?.pause()
            trackState.playState = PlayState.PAUSED
        } catch (e: Exception) {
            LogUtil.e(TAG, "暂停播放异常: ${e.message}")
        }
    }

    /**
     * 恢复播放
     */
    fun resume(channelType: ChannelType = ChannelType.STEREO) {
        val  trackState = getTrackState(channelType)
        resumeTrack(trackState)
    }

    /**
     * 恢复单个轨道
     */
    private fun resumeTrack(trackState: TrackState) {
        try {
            if (trackState.playState == PlayState.PAUSED) {
                trackState.audioTrack?.play()
                trackState.playState = PlayState.PLAYING
            }
        } catch (e: Exception) {
            LogUtil.e(TAG, "恢复播放异常: ${e.message}")
        }
    }

    /**
     * 释放资源
     */
    fun release() {
        // L.i(TAG, "开始释放AudioPlayer资源")

        releaseTrack(ChannelType.LEFT)
        releaseTrack(ChannelType.RIGHT)
        releaseTrack(ChannelType.STEREO)

        callback.set(null)
        playStates.set(PlayState.RELEASED)
        // L.i(TAG, "AudioPlayer资源释放完成")
    }

    /**
     * 释放单个轨道 - 优化版本
     */
    fun releaseTrack(channelType: ChannelType) {
        val trackState = getTrackState(channelType)
        
        // 先取消协程
        trackState.job?.cancel()
        trackState.job = null
        
        trackState.run {
            try {
                audioTrack?.let { track ->
                    when (track.playState) {
                        AudioTrack.PLAYSTATE_PLAYING, AudioTrack.PLAYSTATE_PAUSED -> {
                            track.stop()
                        }
                    }
                    track.flush()
                    track.release()
                }
            } catch (e: Exception) {
                LogUtil.e(TAG, "释放轨道异常: ${e.message}")
            } finally {
                audioTrack = null
                queue.clear()
                playState = PlayState.RELEASED
            }
        }
    }

    /**
     * 通知播放开始
     */
    private fun notifyPlayStart() {
        mainHandler.post {
            callback.get()?.onPlayStart()
        }
    }

    /**
     * 通知播放结束
     */
    private fun notifyPlayOver(channelType: ChannelType) {
        mainHandler.post {
            callback.get()?.onPlayOver()
        }
    }

    /**
     * 通知错误
     */
    private fun notifyError(message: String) {
        LogUtil.e(TAG, "音频播放错误: $message")
    }


    /**
     * 获取播放状态
     */
    fun getPlayState(): PlayState = playStates.get()

    /**
     * 获取单个轨道状态
     */
    fun getTrackState(channelType: ChannelType): TrackState {
        return when (channelType) {
            ChannelType.LEFT -> leftState
            ChannelType.RIGHT -> rightState
            ChannelType.STEREO -> stereoState
        }
    }

    /**
     * 获取队列大小
     */
    fun getQueueSize(channelType: ChannelType): Int {
        return when (channelType) {
            ChannelType.LEFT -> leftState.queue.size
            ChannelType.RIGHT -> rightState.queue.size
             ChannelType.STEREO -> stereoState.queue.size
        }
    }

    /**
     * 清空队列
     */
    fun clearQueue(channelType: ChannelType = ChannelType.STEREO) {
        when (channelType) {
            ChannelType.LEFT -> leftState.queue.clear()
            ChannelType.RIGHT -> rightState.queue.clear()
             ChannelType.STEREO -> stereoState.queue.clear()
        }
    }
}
