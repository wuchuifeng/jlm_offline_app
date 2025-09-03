package com.jlm.translator.pop

import android.content.Context
import android.view.Gravity
import android.view.animation.AnimationUtils
import cn.chawloo.base.ext.doClick
import com.jaygoo.widget.OnRangeChangedListener
import com.jaygoo.widget.RangeSeekBar
import com.jlm.translator.R
import com.jlm.translator.databinding.PopSpeakSpeedSettingBinding
import razerdp.basepopup.BasePopupFlag
import razerdp.basepopup.BasePopupWindow

class PlaySpeedSettingPop(context: Context) : BasePopupWindow(context) {
    init {
        setContentView(R.layout.pop_speak_speed_setting)
        showAnimation = AnimationUtils.loadAnimation(context, cn.chawloo.base.R.anim.pop_bottom_show)
        dismissAnimation = AnimationUtils.loadAnimation(context, cn.chawloo.base.R.anim.pop_bottom_dismiss)
        popupGravity = Gravity.BOTTOM
        setOverlayNavigationBar(true)
        setOverlayNavigationBarMode(BasePopupFlag.OVERLAY_CONTENT)
        val vb = PopSpeakSpeedSettingBinding.bind(contentView)
//        vb.ivClose.doClick { dismiss() }
        vb.seekBar.setProgress(50F)
        vb.seekBar.setOnRangeChangedListener(object : OnRangeChangedListener {
            override fun onRangeChanged(view: RangeSeekBar?, leftValue: Float, rightValue: Float, isFromUser: Boolean) {
            }

            override fun onStartTrackingTouch(view: RangeSeekBar?, isLeft: Boolean) {
            }

            override fun onStopTrackingTouch(view: RangeSeekBar?, isLeft: Boolean) {
            }
        })
    }
}