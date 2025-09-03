package com.jlm.translator.pop

import android.content.Context
import android.view.Gravity
import android.view.animation.AnimationUtils
import com.jlm.translator.R
import com.jlm.translator.databinding.PopPhraseDialogueRecordBinding
import razerdp.basepopup.BasePopupFlag
import razerdp.basepopup.BasePopupWindow

/****短语对话录制效果弹出框****/
class PhraseDialogueRecordPop(context: Context) : BasePopupWindow(context) {
    private val vb: PopPhraseDialogueRecordBinding

    init {
        setContentView(R.layout.pop_phrase_dialogue_record)
        popupGravity = Gravity.CENTER
        showAnimation =
            AnimationUtils.loadAnimation(context, cn.chawloo.base.R.anim.pop_middle_show)
        dismissAnimation =
            AnimationUtils.loadAnimation(context, cn.chawloo.base.R.anim.pop_middle_dismiss)
        setOverlayNavigationBarMode(BasePopupFlag.OVERLAY_MASK)
        setOverlayStatusbarMode(BasePopupFlag.OVERLAY_MASK)
        vb = PopPhraseDialogueRecordBinding.bind(contentView)
    }

    override fun showPopupWindow() {
        super.showPopupWindow()
        vb.lottieLeftView.playAnimation()
        vb.lottieRightView.playAnimation()
    }
    fun showPopupWindow(text: String) {
        super.showPopupWindow()
        vb.tvVoice.text = text
        vb.lottieLeftView.playAnimation()
        vb.lottieRightView.playAnimation()
    }
    override fun dismiss() {
        vb.lottieLeftView.cancelAnimation()
        vb.lottieRightView.cancelAnimation()
        super.dismiss()
    }
}