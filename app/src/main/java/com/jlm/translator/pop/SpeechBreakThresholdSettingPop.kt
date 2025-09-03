package com.jlm.translator.pop

import android.content.Context
import android.view.Gravity
import android.view.animation.AnimationUtils
import cn.chawloo.base.ext.doClick
import cn.chawloo.base.utils.MK
import cn.chawloo.base.utils.MKKeys
import cn.chawloo.base.utils.MKKeys.KEY_RECOGNIZE_BREAK
import com.jaygoo.widget.OnRangeChangedListener
import com.jaygoo.widget.RangeSeekBar
import com.jlm.translator.R
import com.jlm.translator.databinding.PopSpeechBreakSettingBinding
import com.safframework.log.L
import razerdp.basepopup.BasePopupFlag
import razerdp.basepopup.BasePopupWindow

/**
 * 语音断句阈值设置pop
 * */
class SpeechBreakThresholdSettingPop(context: Context, var silence: Int,callbackSelect: Int.() -> Unit) : BasePopupWindow(context) {
    val TAG: String = javaClass.name
    init {
        setContentView(R.layout.pop_speech_break_setting)
        showAnimation = AnimationUtils.loadAnimation(context, cn.chawloo.base.R.anim.pop_bottom_show)
        dismissAnimation = AnimationUtils.loadAnimation(context, cn.chawloo.base.R.anim.pop_bottom_dismiss)
        popupGravity = Gravity.BOTTOM
        setOverlayNavigationBar(true)
        setOverlayNavigationBarMode(BasePopupFlag.OVERLAY_CONTENT)
        val vb = PopSpeechBreakSettingBinding.bind(contentView)
//        vb.ivClose.doClick { dismiss() }
//        var speakSpeed = MK.decodeInt(KEY_RECOGNIZE_BREAK, 400)
        if(silence < 200) silence = 200
        vb.seekBar.setProgress(silence.toFloat())
        vb.seekBar.setOnRangeChangedListener(object : OnRangeChangedListener {
            override fun onRangeChanged(view: RangeSeekBar?, leftValue: Float, rightValue: Float, isFromUser: Boolean) {
//                var intValue = leftValue.toInt()
//                MK.encode(KEY_RECOGNIZE_BREAK, intValue)
//                intValue.callbackSelect()

            }

            override fun onStartTrackingTouch(view: RangeSeekBar?, isLeft: Boolean) {

            }

            override fun onStopTrackingTouch(view: RangeSeekBar?, isLeft: Boolean) {
                val fValue = view?.leftSeekBar?.progress
                if(fValue != null) {
                    fValue.let {
                        it.toInt().callbackSelect()
                    }
                }

            }
        })
    }
}