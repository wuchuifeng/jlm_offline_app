package com.jlm.translator.act

import android.view.WindowManager
import cn.chawloo.base.ext.doClick
import cn.chawloo.base.ext.gone
import cn.chawloo.base.ext.visible
import com.dylanc.viewbinding.binding
import com.gyf.immersionbar.ktx.immersionBar
import com.jlm.common.compat.BaseActCompat
import com.jlm.common.router.Rt
import com.jlm.common.util.PermissionUtils.requestRecordAudioPermission
import com.jlm.translator.R
import com.jlm.translator.databinding.ActListenerOriginalModeBinding
import com.jlm.translator.intelligent.speech.original.OriginalSpeechHelper
import com.therouter.router.Route
import me.xfans.lib.voicewaveview.VoiceWaveView

/**
 * 原声模式，讲解器
 * */
@Route(path = Rt.ListenOriginalModeAct)
class ListenOriginalModeAct: BaseActCompat() {
    private val vb by binding<ActListenerOriginalModeBinding>()

    val originalHelper: OriginalSpeechHelper = OriginalSpeechHelper()
    var isRecord = false
    private var lastWave = R.raw.ani_volum_recording_longbig //R.raw.ani_volum_recording_longsmall

    override fun initialize() {
        super.initialize()
        immersionBar {
            statusBarColor(android.R.color.white)
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        voiceWaveAnimConfig(vb.voiceWave)
        updateUi()
    }

    private fun updateUi() {
        if(isRecord) {
//            vb.btnRecord.text = "暂停"
            vb.btnRecord.gone()
            //显示录制ui
            vb.slRecording.visible()
            //播放动画
            vb.voiceWave.start()
//            vb.voiceWave.setAnimation(lastWave)
//            vb.voiceWave.repeatCount = LottieDrawable.INFINITE
//            vb.voiceWave.playAnimation()
        }else {
//            vb.btnRecord.text = "开始"
            vb.btnRecord.visible()
            //隐藏录制ui
            vb.slRecording.gone()
            vb.voiceWave.stop()
//            vb.voiceWave.cancelAnimation()
//            vb.voiceWave.gone()
        }
    }

    fun voiceWaveAnimConfig(waveView: VoiceWaveView) {
        waveView?.apply {
            lineWidth = 6f
            lineSpace = 10f
            duration = 150
            addBody(30)
            addBody(60)
            addBody(90)
            addBody(60)
            addBody(30)
        }
    }

    override fun onClick() {
        super.onClick()
        vb.btnRecord.doClick {
            requestRecordAudioPermission {
                isRecord = true
                updateUi()
                originalHelper.startRecord()
//                if(isRecord) {
//                    originalHelper.startRecord()
//                }else{
//                    originalHelper.stop()
//                }
            }
        }
        //暂停录制
        vb.slRecording.doClick {
            isRecord = false
            updateUi()
            originalHelper.stop()
        }
    }

    override fun backPressed() {
        if (isRecord) {
            isRecord = false
            originalHelper.stop()
            updateUi()
        }else {
            super.backPressed()
        }
    }

    override fun onStop() {
        super.onStop()
        originalHelper.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        originalHelper.release()
    }

}