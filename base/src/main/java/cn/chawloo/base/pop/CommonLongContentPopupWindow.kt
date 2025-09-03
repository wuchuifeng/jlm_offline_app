package cn.chawloo.base.pop

import android.content.Context
import android.text.method.ScrollingMovementMethod
import android.view.Gravity
import android.view.animation.AnimationUtils
import cn.chawloo.base.R
import cn.chawloo.base.databinding.PopCustomLongTextBinding
import cn.chawloo.base.ext.doClick
import cn.chawloo.base.ext.dp
import razerdp.basepopup.BasePopupWindow

/**
 * 通用长文本弹窗
 * @author Create by 鲁超 on 2020/11/18 0018 13:39
 */
class CommonLongContentPopupWindow(context: Context?, title: String, content: String) : BasePopupWindow(context) {
    init {
        setContentView(R.layout.pop_custom_long_text)
        popupGravity = Gravity.CENTER
        showAnimation = AnimationUtils.loadAnimation(context, R.anim.pop_middle_show)
        dismissAnimation = AnimationUtils.loadAnimation(context, R.anim.pop_middle_dismiss)
        setBackPressEnable(true)
        setMaxHeight(300.dp)
        val vb = PopCustomLongTextBinding.bind(contentView)
        vb.tvTitle.text = title
        vb.tvContent.text = content
        vb.tvContent.movementMethod = ScrollingMovementMethod.getInstance()
        vb.ivClose.doClick { dismiss() }
    }
}