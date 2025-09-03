package cn.chawloo.base.widget

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.widget.RelativeLayout
import androidx.annotation.DrawableRes
import cn.chawloo.base.R
import cn.chawloo.base.base.BaseAct
import cn.chawloo.base.databinding.HeadBannerBinding
import cn.chawloo.base.ext.asActivity
import cn.chawloo.base.ext.doClick
import cn.chawloo.base.ext.gone
import cn.chawloo.base.ext.if2Gone
import cn.chawloo.base.ext.sp
import cn.chawloo.base.ext.visible

/**
 * TODO
 * @author Create by 鲁超 on 2021/3/11 0011 14:41
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
class HeadBanner @JvmOverloads constructor(context: Context, attributeSet: AttributeSet? = null, defAttrStyle: Int = 0) : RelativeLayout(context, attributeSet, defAttrStyle) {
    private var vb: HeadBannerBinding = HeadBannerBinding.bind(inflate(context, R.layout.head_banner, this))
    private var titleTextSize: Float = 18F.sp
    var title: String? = ""
        set(value) {
            value?.takeIf { it.isNotBlank() }?.run {
                vb.tvTitle.text = value
//                vb.tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, titleTextSize)
            }
            vb.tvTitle.if2Gone(value.isNullOrBlank())
            field = value
            setActTitle()
        }

    fun goneBack() {
        vb.ivBack.gone()
    }

    fun setAction(@DrawableRes drawableId: Int, listener: () -> Unit) {
        vb.ivAction.setImageResource(drawableId)
        vb.ivAction.doClick {
            listener()
        }
    }

    init {
        val ta = context.obtainStyledAttributes(attributeSet, R.styleable.HeadBanner)
        title = ta.getString(R.styleable.HeadBanner_android_title) ?: ""
        titleTextSize = ta.getDimension(R.styleable.HeadBanner_android_textSize, 18F.sp)
        ta.recycle()
        vb.ivBack.apply {
            doClick {
                (this.context as? BaseAct)?.backPressed()
            }
            visible()
        }
    }

    private fun setActTitle() {
        context.asActivity()?.title = title
    }
}