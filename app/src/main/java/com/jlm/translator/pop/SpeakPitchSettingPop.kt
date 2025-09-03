package com.jlm.translator.pop

import android.content.Context
import android.view.Gravity
import android.view.animation.AnimationUtils
import cn.chawloo.base.ext.doClick
import cn.chawloo.base.utils.MK
import cn.chawloo.base.utils.MKKeys
import com.jaygoo.widget.OnRangeChangedListener
import com.jaygoo.widget.RangeSeekBar
import com.jlm.translator.R
import com.jlm.translator.databinding.PopSpeakSpeedSettingBinding
import razerdp.basepopup.BasePopupFlag
import razerdp.basepopup.BasePopupWindow

/**
 * 播报语调设置pop
 * */
class SpeakPitchSettingPop(context: Context, type: Int) : BasePopupWindow(context) {
    init {
        setContentView(R.layout.pop_speak_pitch_setting)
        showAnimation = AnimationUtils.loadAnimation(context, cn.chawloo.base.R.anim.pop_bottom_show)
        dismissAnimation = AnimationUtils.loadAnimation(context, cn.chawloo.base.R.anim.pop_bottom_dismiss)
        popupGravity = Gravity.BOTTOM
        setOverlayNavigationBar(true)
        setOverlayNavigationBarMode(BasePopupFlag.OVERLAY_CONTENT)
        val vb = PopSpeakSpeedSettingBinding.bind(contentView)
//        vb.ivClose.doClick { dismiss() }
        val speakPitch = MK.decodeFloat(MKKeys.KEY_SYNTHESIS_PITCH, 0F)
        vb.seekBar.setProgress(speakPitch)
        vb.seekBar.setOnRangeChangedListener(object : OnRangeChangedListener {
            override fun onRangeChanged(view: RangeSeekBar?, leftValue: Float, rightValue: Float, isFromUser: Boolean) {
                MK.encode(MKKeys.KEY_SYNTHESIS_PITCH, leftValue)
            }

            override fun onStartTrackingTouch(view: RangeSeekBar?, isLeft: Boolean) {

            }

            override fun onStopTrackingTouch(view: RangeSeekBar?, isLeft: Boolean) {
            }
        })
    }
}