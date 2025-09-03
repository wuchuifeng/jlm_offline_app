package com.jlm.common.pop

import android.content.Context
import android.text.method.LinkMovementMethod
import android.view.Gravity
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat
import androidx.core.text.buildSpannedString
import cn.chawloo.base.ext.appendClickable
import cn.chawloo.base.ext.doClick
import cn.chawloo.base.ext.gone
import cn.chawloo.base.ext.transparentHighlightColor
import cn.chawloo.base.ext.visible
import cn.chawloo.base.utils.MK
import cn.chawloo.base.utils.MKKeys
import com.jlm.common.R
import com.jlm.common.databinding.PopPrivacyPolicyBinding
import com.jlm.common.net.NetConstants
import com.jlm.common.router.openWeb
import org.joda.time.LocalDate
import razerdp.basepopup.BasePopupWindow

/**
 * 隐私政策弹窗
 * @author Create by 鲁超 on 2020/12/2 0002 16:58
 */
class PrivacyPolicyPop(context: Context, isOnlyAgree: Boolean = false, callBack: (Boolean) -> Unit = {}) : BasePopupWindow(context) {

    companion object {
        const val KEY_POLICY_VER = "key_policy_ver"

        fun show(context: Context, agree: (Boolean) -> Unit = {}) {
            if (MK.decodeBool(MKKeys.KEY_POLICY, true)) {
                PrivacyPolicyPop(context, isOnlyAgree = false, callBack = agree).showPopupWindow()
                return
            }
            agree(true)
        }
    }

    private var vb: PopPrivacyPolicyBinding

    init {
        popupGravity = Gravity.CENTER
        showAnimation = AnimationUtils.loadAnimation(context, cn.chawloo.base.R.anim.pop_middle_show)
        dismissAnimation = AnimationUtils.loadAnimation(context, cn.chawloo.base.R.anim.pop_middle_dismiss)
        setOutSideDismiss(false)
        setBackPressEnable(false)
        setContentView(R.layout.pop_privacy_policy)
        vb = PopPrivacyPolicyBinding.bind(contentView)
        if (isOnlyAgree) {
            vb.tvReject.gone()
            vb.tvAgree.text = "我已知晓"
        } else {
            vb.tvReject.visible()
        }
        vb.tvReject.doClick {
            callBack(false)
            dismiss()
        }
        vb.tvAgree.doClick {
            MK.encode(KEY_POLICY_VER, LocalDate.now().toString("yyyyMMdd"))
            callBack(true)
            dismiss()
        }
        vb.tvDesc.text = buildSpannedString {
            append("亲爱的用户，感谢您对“极力米”的信任！\n\n我们非常重视您的隐私及个人信息保护，我们根据相关法律制定了")
            appendClickable("《用户协议》", color = ContextCompat.getColor(context, cn.chawloo.base.R.color.theme_color)) {
                openWeb(webTitle = "用户协议", webUrl = NetConstants.USER_AGREEMENT)
            }
            append("、")
            appendClickable("《隐私政策》", color = ContextCompat.getColor(context, cn.chawloo.base.R.color.theme_color)) {
                openWeb(webTitle = "隐私政策", webUrl = NetConstants.PRIVACY_POLICY)
            }
            append("、")
            appendClickable("《第三方信息共享清单》", color = ContextCompat.getColor(context, cn.chawloo.base.R.color.theme_color)) {
                openWeb(webTitle = "第三方信息共享清单", webUrl = NetConstants.THIRD_SHARE)
            }
            append("、")
            appendClickable("《个人信息收集清单》", color = ContextCompat.getColor(context, cn.chawloo.base.R.color.theme_color)) {
                openWeb(webTitle = "个人信息收集清单", webUrl = NetConstants.PERSONAL_INFORMATION_COLLECTION)
            }
            append("及")
            appendClickable("《权限清单》", color = ContextCompat.getColor(context, cn.chawloo.base.R.color.theme_color)) {
                openWeb(webTitle = "权限清单", webUrl = NetConstants.PERMISSION_CHECKLIST)
            }
            append("，请您在点击同意之前仔细阅读并充分理解相关条款，其中的重点条款已为您标注，方便您了解自己的权利。\n\n")
            append("为了给您提供优质服务，我们会根据您使用的具体功能，向系统申请包括但不限于下列权限：\n")
            append("1.系统设备权限：收集设备信息、日志信息等，用于信息推送、安全风控及提供基于设备的SDK服务。\n")
            append("2.相机权限：方便您使用相机拍摄后设置为账户头像\n")
            append("3.存储权限：用于用户设置头像时，使用本地相册的图片作为头像、保存快照和分享时保存长图至相册\n")
            append("4.录音权限：方便您在首页通过口述来进行企业搜索")
        }
        vb.tvDesc.transparentHighlightColor()
        vb.tvDesc.movementMethod = LinkMovementMethod.getInstance()
    }
}