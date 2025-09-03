package com.jlm.common.pop

import android.content.Context
import android.view.Gravity
import android.view.animation.AnimationUtils
import cn.chawloo.base.ext.doClick
import com.jlm.common.R
import com.jlm.common.databinding.PopChooseAlbumBinding
import razerdp.basepopup.BasePopupFlag
import razerdp.basepopup.BasePopupWindow

/**
 * 选择相册和拍照的弹窗
 * @author Create by 鲁超 on 2020/11/20 0020 10:11
 */
class ChooseAlbumPop(context: Context, choose: (Int) -> Unit) : BasePopupWindow(context) {
    init {
        setContentView(R.layout.pop_choose_album)
        popupGravity = Gravity.BOTTOM
        showAnimation = AnimationUtils.loadAnimation(getContext(), cn.chawloo.base.R.anim.pop_bottom_show)
        dismissAnimation = AnimationUtils.loadAnimation(getContext(), cn.chawloo.base.R.anim.pop_bottom_dismiss)
        setOverlayNavigationBarMode(BasePopupFlag.OVERLAY_MASK)
        setOverlayStatusbarMode(BasePopupFlag.OVERLAY_MASK)
        val vb = PopChooseAlbumBinding.bind(contentView)
        vb.tvCancel.doClick { dismiss() }
        vb.tvCamera.doClick {
            dismiss()
            choose(1)
        }
        vb.tvAlbum.doClick {
            dismiss()
            choose(2)
        }
    }
}