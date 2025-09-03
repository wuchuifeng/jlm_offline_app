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
import com.safframework.log.L
import razerdp.basepopup.BasePopupFlag
import razerdp.basepopup.BasePopupWindow
import java.math.BigDecimal

/**
 * 播报语速设置pop
 * */
class SpeakSpeedSettingPop(context: Context, var speed: Float, private val callbackSelect: Float.() -> Unit) : BasePopupWindow(context) {
    init {
        setContentView(R.layout.pop_speak_speed_setting)
        showAnimation = AnimationUtils.loadAnimation(context, cn.chawloo.base.R.anim.pop_bottom_show)
        dismissAnimation = AnimationUtils.loadAnimation(context, cn.chawloo.base.R.anim.pop_bottom_dismiss)
        popupGravity = Gravity.BOTTOM
        setOverlayNavigationBar(true)
        setOverlayNavigationBarMode(BasePopupFlag.OVERLAY_CONTENT)
        val vb = PopSpeakSpeedSettingBinding.bind(contentView)
//        vb.ivClose.doClick { dismiss() }
//        val speakSpeed = MK.decodeFloat(MKKeys.KEY_SYNTHESIS_SPEED, 1F)
        vb.seekBar.setProgress(speed)
        vb.seekBar.setOnRangeChangedListener(object : OnRangeChangedListener {
            override fun onRangeChanged(view: RangeSeekBar?, leftValue: Float, rightValue: Float, isFromUser: Boolean) {
//                MK.encode(MKKeys.KEY_SYNTHESIS_SPEED, leftValue)
                L.d("speakspeedsettingPop>>>$leftValue")
//                leftValue.callbackSelect()
            }

            override fun onStartTrackingTouch(view: RangeSeekBar?, isLeft: Boolean) {

            }

            override fun onStopTrackingTouch(view: RangeSeekBar?, isLeft: Boolean) {
                val speed = view?.leftSeekBar?.progress
                if (speed != null) {
                    speed?.let {
                        it.callbackSelect()
                    }
                }
            }
        })
    }
}