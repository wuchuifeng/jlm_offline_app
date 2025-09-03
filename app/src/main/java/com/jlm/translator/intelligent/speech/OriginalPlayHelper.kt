package com.jlm.translator.intelligent.speech

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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.LinkedBlockingQueue

/**
 * 原声输出
 * */
@SuppressLint("MissingPermission")
class OriginalPlayHelper {
    private lateinit var mAudioRecorder: AudioRecord
    val SAMPLE_RATE = 16000
    val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    lateinit var writeBuffer: ByteArray
    var writeBufferSize: Int = 0

    private lateinit var mAudioTrack: AudioTrack

    lateinit var recordJob: Job
    var recordRunning: Boolean = true

    private var canceler: AcousticEchoCanceler? = null
    private var mNoiseSuppressor: NoiseSuppressor? = null

    private val audioQueue: LinkedBlockingQueue<ByteArray> = LinkedBlockingQueue<ByteArray>()
    private var tempData: ByteArray = ByteArray(0)

    init {
        // 获得缓冲区字节大小
        initTrack()
    }

    fun initTrack() {
        val bufferSizeInBytes = AudioTrack.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO, audioFormat) * 2
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

    fun setAudioRecorder(record: AudioRecord) {
        mAudioRecorder = record
        initAEC(mAudioRecorder.audioSessionId)
    }

    @SuppressLint("MissingPermission")
    fun startPlay() {
        recordRunning = true
        recordJob = CoroutineScope(Dispatchers.IO).launch {
            // 开始录制
            mAudioTrack.play()
            mAudioTrack.setStereoVolume(0f, 1.0f)
            while (recordRunning) {
//                mAudioRecorder.read(writeBuffer, 0, writeBufferSize)
//                mAudioTrack.write(writeBuffer, 0, writeBufferSize) ;
                if (audioQueue.size == 0) {
//                    Thread.sleep(30)
                    delay(20)
                    continue
                }
                try {
                    tempData = audioQueue.take()
                    mAudioTrack.write(tempData, 0, tempData.size)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
            // 停止录音 播放
            recordJob.cancel()
        }
    }

    fun setAudioData(data: ByteArray) {
        audioQueue.offer(data)
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
//        if (mAudioRecorder !== null && mAudioRecorder.state === AudioRecord.RECORDSTATE_RECORDING) {
//            // 停止录音 播放
//            mAudioRecorder.stop()
//        }
        if (mAudioTrack !== null && (mAudioTrack.state === AudioTrack.PLAYSTATE_PLAYING || mAudioTrack.state === AudioTrack.PLAYSTATE_PAUSED)) {
            mAudioTrack.stop()
        }
    }

    fun release() {
//        mAudioRecorder.release()
//        mAudioTrack.release()
//        if (mAudioRecorder != null && mAudioRecorder.state == AudioRecord.RECORDSTATE_STOPPED) {
//            // 停止录音 播放
//            mAudioRecorder.release()
//        }
        if (mAudioTrack != null && (mAudioTrack.state == AudioTrack.PLAYSTATE_STOPPED || mAudioTrack.state == AudioTrack.PLAYSTATE_PAUSED)) {
            mAudioTrack.release()
        }

    }
}