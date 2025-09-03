package com.jlm.translator.intelligent.speech.original

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.media.audiofx.AcousticEchoCanceler
import android.media.audiofx.NoiseSuppressor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * 原声输出
 * */
@SuppressLint("MissingPermission")
class OriginalSpeechHelper {
    private var mAudioRecorder: AudioRecord
    val SAMPLE_RATE = 48000
    val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    lateinit var writeBuffer: ByteArray
    var writeBufferSize: Int = 0

    private lateinit var mAudioTrack: AudioTrack

    lateinit var recordJob: Job
    var recordRunning: Boolean = true

    private var canceler: AcousticEchoCanceler? = null
    private var mNoiseSuppressor: NoiseSuppressor? = null

    init {
        // 获得缓冲区字节大小
        initTrack()
        // 获得缓冲区字节大小
        val bufferSizeInBytes = AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, audioFormat)
        //录音初始化，录音参数中格式只支持16bit/单通道，采样率支持8K/16K
        mAudioRecorder = AudioRecord(MediaRecorder.AudioSource.DEFAULT, SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSizeInBytes)
        initAEC(mAudioRecorder.audioSessionId)
    }

    fun initTrack() {
        val bufferSizeInBytes = AudioTrack.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO, audioFormat)
        mAudioTrack = AudioTrack(
            AudioManager.STREAM_MUSIC, SAMPLE_RATE,
            AudioFormat.CHANNEL_OUT_MONO,
            audioFormat,
            bufferSizeInBytes,
            AudioTrack.MODE_STREAM
        )
        writeBuffer = ByteArray(bufferSizeInBytes)
        writeBufferSize = writeBuffer.size
    }

    @SuppressLint("MissingPermission")
    fun startRecord() {
        recordRunning = true
        recordJob = CoroutineScope(Dispatchers.IO).launch {
            // 开始录制
            mAudioRecorder.startRecording()
            mAudioTrack.play()
            while (recordRunning) {
                mAudioRecorder.read(writeBuffer, 0, writeBufferSize)
                mAudioTrack.write(writeBuffer, 0, writeBufferSize) ;
            }
            // 停止录音 播放
            recordJob.cancel()
        }
    }

    fun initAEC(audioSession: Int) {
        if (canceler != null) {
            return
        }
        mNoiseSuppressor = NoiseSuppressor.create(audioSession)
        if (mNoiseSuppressor != null) {
            val res: Int = mNoiseSuppressor!!.setEnabled(true)
        }
        canceler = AcousticEchoCanceler.create(audioSession)
        canceler?.run { setEnabled(true) }
    }

    fun stop() {
        recordRunning = false
        if (mAudioRecorder !== null && mAudioRecorder.state === AudioRecord.RECORDSTATE_RECORDING) {
            // 停止录音 播放
            mAudioRecorder.stop()
        }
        if (mAudioTrack !== null && (mAudioTrack.state === AudioTrack.PLAYSTATE_PLAYING || mAudioTrack.state === AudioTrack.PLAYSTATE_PAUSED)) {
            mAudioTrack.stop()
        }
    }

    fun release() {
        mAudioRecorder.release()
        mAudioTrack.release()
        if (mAudioRecorder != null && mAudioRecorder.state == AudioRecord.RECORDSTATE_STOPPED) {
            // 停止录音 播放
            mAudioRecorder.release()
        }
        if (mAudioTrack != null && (mAudioTrack.state == AudioTrack.PLAYSTATE_STOPPED || mAudioTrack.state == AudioTrack.PLAYSTATE_PAUSED)) {
            mAudioTrack.release()
        }

    }
}