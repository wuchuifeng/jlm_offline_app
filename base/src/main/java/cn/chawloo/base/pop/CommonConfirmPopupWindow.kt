package cn.chawloo.base.pop

import android.content.Context
import android.view.Gravity
import android.view.animation.AnimationUtils
import cn.chawloo.base.R
import cn.chawloo.base.databinding.PopCustomConfirmBinding
import cn.chawloo.base.ext.doClick
import cn.chawloo.base.ext.gone
import cn.chawloo.base.ext.visible
import razerdp.basepopup.BasePopupWindow

class CommonConfirmPopupWindow(
    var context: Context,
    var title: String? = null,
    var content: String?,
    leftStr: String? = null,
    rightStr: String? = "确定",
    cancel: (() -> Unit)? = null,
    var confirm: () -> Unit
) : BasePopupWindow(context) {
    private val vb: PopCustomConfirmBinding

    init {
        setContentView(R.layout.pop_custom_confirm)
        vb = PopCustomConfirmBinding.bind(contentView)
        popupGravity = Gravity.CENTER
        showAnimation = AnimationUtils.loadAnimation(context, R.anim.pop_middle_show)
        dismissAnimation = AnimationUtils.loadAnimation(context, R.anim.pop_middle_dismiss)

        if (leftStr != null) {
            vb.vLine.visible()
            vb.tvCancel.visible()
            vb.tvCancel.text = leftStr
            vb.tvCancel.doClick {
                dismiss()
                cancel?.invoke()
            }
        } else {
            vb.vLine.gone()
            vb.tvCancel.gone()
        }
        vb.tvConfirm.text = rightStr
        vb.tvConfirm.doClick {
            dismiss()
            confirm()
        }
        if (!title.isNullOrBlank()) {
            vb.tvTitle.text = title
        }
        vb.tvContent.text = content
    }

    fun setCancelTxt(leftTxt: String, cancel: (() -> Unit)? = null) {
        leftTxt.takeIf { it.isNotBlank() }?.run {
            vb.tvCancel.text = this
            vb.tvCancel.visible()
            vb.tvCancel.doClick {
                dismiss()
                cancel?.invoke()
            }
        } ?: run {
            vb.tvCancel.gone()
        }
    }
}