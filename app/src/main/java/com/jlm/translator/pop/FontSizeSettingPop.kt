package com.jlm.translator.pop

import android.content.Context
import android.view.Gravity
import android.view.animation.AnimationUtils
import cn.chawloo.base.ext.doClick
import com.jaygoo.widget.OnRangeChangedListener
import com.jaygoo.widget.RangeSeekBar
import com.jlm.translator.R
import com.jlm.translator.databinding.PopFontSizePreviewBinding
import com.jlm.translator.utils.ListenSettingUtil
import com.safframework.log.L
import razerdp.basepopup.BasePopupFlag
import razerdp.basepopup.BasePopupWindow

class FontSizeSettingPop(
    context: Context,
    var fontCode: Int,
    private val callbackSelect: Int.() -> Unit
    ) : BasePopupWindow(context) {

    companion object{
        const val defaultProgress = 50F
        val fontSizeList = arrayOf(12F, 14F, 16F, 19F, 21F)
        val progressFactors = arrayOf(0F, 25F, 50F, 75F, 100F)
        val defaultFontSize = fontSizeList[2]
    }

    init {
        setContentView(R.layout.pop_font_size_preview)
        showAnimation = AnimationUtils.loadAnimation(context, cn.chawloo.base.R.anim.pop_bottom_show)
        dismissAnimation = AnimationUtils.loadAnimation(context, cn.chawloo.base.R.anim.pop_bottom_dismiss)
        popupGravity = Gravity.BOTTOM
        setOverlayNavigationBar(true)
        setOverlayNavigationBarMode(BasePopupFlag.OVERLAY_CONTENT)
        val vb = PopFontSizePreviewBinding.bind(contentView)
//        vb.ivClose.doClick { dismiss() }
        //初始化值
        getProgress().apply {
            vb.seekBar.setProgress(this)
            getFontsize().apply {
                vb.tvFontSizePreviewEn.textSize = this
                vb.tvFontSizePreviewCh.textSize = this
            }
        }
//        vb.seekBar.setProgress(getProgress())
        vb.seekBar.setOnRangeChangedListener(object : OnRangeChangedListener {
            override fun onRangeChanged(view: RangeSeekBar?, leftValue: Float, rightValue: Float, isFromUser: Boolean) {

            }

            override fun onStartTrackingTouch(view: RangeSeekBar?, isLeft: Boolean) {}

            override fun onStopTrackingTouch(view: RangeSeekBar?, isLeft: Boolean) {
                L.d("xxxxxxxxxx", "onStopTrackingTouch---${view?.left}--${view?.leftSeekBar?.progress}")
                fontCode = ListenSettingUtil.getFontCode(view?.leftSeekBar?.progress ?: 0)
                getFontsize().apply {
                    vb.tvFontSizePreviewEn.textSize = this
                    vb.tvFontSizePreviewCh.textSize = this
                }
                fontCode.callbackSelect()
            }
        })

    }

    private fun getProgress(): Float {
        return ListenSettingUtil.getFontProgress(fontCode)
    }

    private fun getFontsize(): Float {
        return ListenSettingUtil.getFontSize(fontCode)
    }


}