package com.jlm.translator.pop

import android.content.Context
import android.view.Gravity
import android.view.animation.AnimationUtils
import cn.chawloo.base.ext.doClick
import com.jlm.translator.R
import com.jlm.translator.databinding.PopChooseOutputTypeBinding
import razerdp.basepopup.BasePopupFlag
import razerdp.basepopup.BasePopupWindow

/**
 * 翻译记录导出弹窗
 */
class ChooseOutputTypePop(context: Context, choose: (Int) -> Unit) : BasePopupWindow(context) {
    init {
        setContentView(R.layout.pop_choose_output_type)
        popupGravity = Gravity.BOTTOM
        showAnimation =
            AnimationUtils.loadAnimation(getContext(), cn.chawloo.base.R.anim.pop_bottom_show)
        dismissAnimation =
            AnimationUtils.loadAnimation(getContext(), cn.chawloo.base.R.anim.pop_bottom_dismiss)
        setOverlayNavigationBarMode(BasePopupFlag.OVERLAY_MASK)
        setOverlayStatusbarMode(BasePopupFlag.OVERLAY_MASK)
        val vb = PopChooseOutputTypeBinding.bind(contentView)
        //拷贝
        vb.tvCopy.doClick {
            dismiss()
            choose(1)
        }

        //导出到本地txt文档
        vb.tvOutputEmail.doClick {
            dismiss()
            choose(2)
        }

        //取消
        vb.tvCancel.doClick {
            dismiss()
        }
    }
}