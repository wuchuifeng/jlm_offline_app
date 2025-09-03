package com.jlm.translator.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import com.safframework.log.L

/**
 * 音量增强工具类
 * 提供多种音量增强方案
 */
object VolumeEnhanceUtil {
    private const val TAG = "VolumeEnhanceUtil"
    
    /**
     * 为MediaPlayer设置音量增强
     */
    fun enhanceMediaPlayerVolume(mediaPlayer: MediaPlayer, volumeLevel: Float = 1.0f) {
        try {
            // 设置MediaPlayer音量（0.0 - 1.0）
            mediaPlayer.setVolume(volumeLevel, volumeLevel)
            
            // 设置音频属性
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .setFlags(AudioAttributes.FLAG_AUDIBILITY_ENFORCED)
                    .build()
                mediaPlayer.setAudioAttributes(audioAttributes)
            }
            
            L.d(TAG, "MediaPlayer音量增强完成，音量级别: $volumeLevel")
            
        } catch (e: Exception) {
            L.e(TAG, "MediaPlayer音量增强失败: ${e.message}")
        }
    }
    
    /**
     * 临时增强系统音量
     */
    fun temporaryVolumeBoost(context: Context, boostDuration: Long = 5000L) {
        try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val originalVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
            val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            
            // 设置最大音量
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, 0)
            L.d(TAG, "临时音量增强激活，持续时间: ${boostDuration}ms")
            
            // 延时恢复原音量
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                try {
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, originalVolume, 0)
                    L.d(TAG, "音量已恢复到原始级别: $originalVolume")
                } catch (e: Exception) {
                    L.e(TAG, "恢复原音量失败: ${e.message}")
                }
            }, boostDuration)
            
        } catch (e: Exception) {
            L.e(TAG, "临时音量增强失败: ${e.message}")
        }
    }
    
    /**
     * 检查并调整音频输出路径
     */
    fun optimizeAudioRouting(context: Context) {
        try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            
            // 检查蓝牙耳机连接状态
            val isBluetoothA2dpOn = audioManager.isBluetoothA2dpOn
            val isBluetoothScoOn = audioManager.isBluetoothScoOn
            val isWiredHeadsetOn = audioManager.isWiredHeadsetOn
            
            L.d(TAG, "音频设备状态:")
            L.d(TAG, "  蓝牙A2DP: $isBluetoothA2dpOn")
            L.d(TAG, "  蓝牙SCO: $isBluetoothScoOn") 
            L.d(TAG, "  有线耳机: $isWiredHeadsetOn")
            
            // 如果没有耳机连接，强制使用扬声器
            if (!isBluetoothA2dpOn && !isBluetoothScoOn && !isWiredHeadsetOn) {
                audioManager.isSpeakerphoneOn = true
                L.d(TAG, "强制启用扬声器模式")
            }
            
        } catch (e: Exception) {
            L.e(TAG, "优化音频路径失败: ${e.message}")
        }
    }
    
    /**
     * 获取音量信息字符串
     */
    fun getVolumeInfoString(context: Context): String {
        return try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val musicVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
            val maxMusicVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            val callVolume = audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL)
            val maxCallVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL)
            val isSpeakerOn = audioManager.isSpeakerphoneOn
            val audioMode = when (audioManager.mode) {
                AudioManager.MODE_NORMAL -> "正常模式"
                AudioManager.MODE_RINGTONE -> "铃声模式"
                AudioManager.MODE_IN_CALL -> "通话模式"
                AudioManager.MODE_IN_COMMUNICATION -> "通信模式"
                else -> "未知模式"
            }
            
            """
            音量信息:
              媒体音量: $musicVolume/$maxMusicVolume (${(musicVolume * 100 / maxMusicVolume)}%)
              通话音量: $callVolume/$maxCallVolume (${(callVolume * 100 / maxCallVolume)}%)
              扬声器状态: ${if (isSpeakerOn) "开启" else "关闭"}
              音频模式: $audioMode
            """.trimIndent()
            
        } catch (e: Exception) {
            "获取音量信息失败: ${e.message}"
        }
    }
    
    /**
     * 音量百分比设置
     */
    fun setVolumePercentage(context: Context, percentage: Int) {
        try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            val targetVolume = (maxVolume * percentage / 100).coerceIn(0, maxVolume)
            
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, targetVolume, 0)
            L.d(TAG, "音量设置为 $percentage% ($targetVolume/$maxVolume)")
            
        } catch (e: Exception) {
            L.e(TAG, "设置音量百分比失败: ${e.message}")
        }
    }
    
    /**
     * 渐进式音量调整
     */
    fun fadeVolumeUp(context: Context, durationMs: Long = 3000L) {
        try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
            val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            
            if (currentVolume >= maxVolume) {
                L.d(TAG, "音量已是最大值")
                return
            }
            
            val steps = maxVolume - currentVolume
            val stepDuration = durationMs / steps
            val handler = android.os.Handler(android.os.Looper.getMainLooper())
            
            for (i in 1..steps) {
                handler.postDelayed({
                    val newVolume = currentVolume + i
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVolume, 0)
                    L.d(TAG, "渐进调整音量: $newVolume/$maxVolume")
                }, stepDuration * i)
            }
            
        } catch (e: Exception) {
            L.e(TAG, "渐进式音量调整失败: ${e.message}")
        }
    }
} 