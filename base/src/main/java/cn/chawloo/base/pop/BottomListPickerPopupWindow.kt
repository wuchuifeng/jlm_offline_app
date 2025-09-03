package cn.chawloo.base.pop

import android.content.Context
import android.view.Gravity
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat
import cn.chawloo.base.R
import cn.chawloo.base.databinding.ItemPopSimpleListBinding
import cn.chawloo.base.databinding.PopSimpleBottomListBinding
import cn.chawloo.base.ext.doClick
import cn.chawloo.base.ext.dp
import com.drake.brv.utils.divider
import com.drake.brv.utils.linear
import com.drake.brv.utils.setup
import razerdp.basepopup.BasePopupFlag
import razerdp.basepopup.BasePopupWindow

/**
 * 多选列表弹窗
 * @author Create by 鲁超 on 2020/11/20 0020 18:16
 */
class BottomListPickerPopupWindow(context: Context, dataList: List<String>, picker: (String) -> Unit) : BasePopupWindow(context) {

    init {
        setContentView(R.layout.pop_simple_bottom_list)
        popupGravity = Gravity.BOTTOM
        setMaxHeight(300.dp)
        showAnimation = AnimationUtils.loadAnimation(context, R.anim.pop_bottom_show)
        dismissAnimation = AnimationUtils.loadAnimation(context, R.anim.pop_bottom_dismiss)
        setOverlayNavigationBarMode(BasePopupFlag.OVERLAY_MASK)
        setOverlayStatusbarMode(BasePopupFlag.OVERLAY_MASK)

        val vb = PopSimpleBottomListBinding.bind(contentView)
        vb.recyclerview.linear()
            .divider {
                setColor(ContextCompat.getColor(context, R.color.bg_color))
                setDivider(1)
            }
            .setup {
                addType<String>(R.layout.item_pop_simple_list)
                onBind {
                    val binding = ItemPopSimpleListBinding.bind(itemView)
                    binding.tvValue.text = getModel<String>()
                    binding.root.background = when (layoutPosition) {
                        0 -> ContextCompat.getDrawable(context, R.drawable.pop_white_bg)
                        dataList.lastIndex -> ContextCompat.getDrawable(context, R.drawable.pop_white_bg)
                        else -> null
                    }
                }
                onClick(R.id.tv_value) {
                    dismiss()
                    picker(getModel())
                }
            }.models = dataList
        vb.tvCancel.doClick { dismiss() }
    }
}