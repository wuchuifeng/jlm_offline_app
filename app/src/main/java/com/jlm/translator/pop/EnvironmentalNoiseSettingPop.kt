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
import com.jlm.translator.databinding.PopEnvironmentalNoiseSettingBinding
import com.safframework.log.L
import razerdp.basepopup.BasePopupFlag
import razerdp.basepopup.BasePopupWindow

/**
 * 环境噪音设置
 * 噪音参数阈值，取值范围：-1～+1。
 * 取值越接近-1，判定为语音的概率越大，亦即有可能更多噪声被当作语音被误识别。
 * 取值越接近+1，判定为噪音的越多，亦即有可能更多语音被当作噪音被拒绝掉。
 * */
class EnvironmentalNoiseSettingPop(context: Context, var noise: Float, callbackSelect: Float.() -> Unit) : BasePopupWindow(context) {
    val TAG: String = javaClass.name
    init {
        setContentView(R.layout.pop_environmental_noise_setting)
        showAnimation = AnimationUtils.loadAnimation(context, cn.chawloo.base.R.anim.pop_bottom_show)
        dismissAnimation = AnimationUtils.loadAnimation(context, cn.chawloo.base.R.anim.pop_bottom_dismiss)
        popupGravity = Gravity.BOTTOM
        setOverlayNavigationBar(true)
        setOverlayNavigationBarMode(BasePopupFlag.OVERLAY_CONTENT)
        val vb = PopEnvironmentalNoiseSettingBinding.bind(contentView)
        vb.ivClose.doClick { dismiss() }
        // 音量调整
//        val speakVolume = MK.decodeFloat(MKKeys.KEY_RECOGNIZE_noise, 0.2F)
        vb.seekBar.setProgress(noise)
        vb.seekBar.setOnRangeChangedListener(object : OnRangeChangedListener {
            override fun onRangeChanged(
                view: RangeSeekBar?,
                leftValue: Float,
                rightValue: Float,
                isFromUser: Boolean
            ) {
//                MK.encode(MKKeys.KEY_RECOGNIZE_noise, leftValue)
//                //回调
//                leftValue.callbackSelect()
            }

            override fun onStartTrackingTouch(view: RangeSeekBar?, isLeft: Boolean) {
            }

            override fun onStopTrackingTouch(view: RangeSeekBar?, isLeft: Boolean) {
                val value = view?.leftSeekBar?.progress
                value?.let {
                    it.callbackSelect()
                }
            }

        })
    }
}