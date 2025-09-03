package com.jlm.common.pop

import android.content.Context
import android.graphics.Color
import android.view.animation.AnimationUtils
import com.jlm.common.R
import com.jlm.common.databinding.PopPermissionDescriptionBinding
import razerdp.basepopup.BasePopupWindow

/**
 * 权限说明弹窗
 * @author Create by 鲁超 on 2023/1/3 10:28
 *----------Dragon be here!----------/
 *       ┌─┐      ┌─┐
 *     ┌─┘─┴──────┘─┴─┐
 *     │              │
 *     │      ─       │
 *     │  ┬─┘   └─┬   │
 *     │              │
 *     │      ┴       │
 *     │              │
 *     └───┐      ┌───┘
 *         │      │神兽保佑
 *         │      │代码无BUG！
 *         │      └──────┐
 *         │             ├┐
 *         │             ┌┘
 *         └┐ ┐ ┌───┬─┐ ┌┘
 *          │ ┤ ┤   │ ┤ ┤
 *          └─┴─┘   └─┴─┘
 *─────────────神兽出没───────────────/
 */
class PermissionDescriptionPop(context: Context, permission: PermissionEnum) : BasePopupWindow(context) {
    private val vb: PopPermissionDescriptionBinding

    init {
        showAnimation = AnimationUtils.loadAnimation(context, cn.chawloo.base.R.anim.pop_top_show)
        dismissAnimation = AnimationUtils.loadAnimation(context, cn.chawloo.base.R.anim.pop_top_dismiss)
        setBackgroundColor(Color.TRANSPARENT)
        setContentView(R.layout.pop_permission_description)
        vb = PopPermissionDescriptionBinding.bind(contentView)
        vb.tvPermissionTitle.text = permission.permissionTitle
        vb.tvPermissionDesc.text = permission.permissionDesc
    }

    enum class PermissionEnum(val permissionTitle: String, val permissionDesc: String) {
        CAMERA("相机权限使用说明", "用于拍摄照片，设置成账户头像和问题反馈上传照片。"),
        STORAGE("存储权限使用说明", "用于选择照片设置账户头像、保存快照到相册、分享时保存长图和问题反馈上传照片。"),
        RECORD_AUDIO("麦克风权限使用说明", "用于语音识别口述的。"),
    }
}