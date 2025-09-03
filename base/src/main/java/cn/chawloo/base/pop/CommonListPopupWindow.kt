package cn.chawloo.base.pop

import android.content.Context
import android.view.Gravity
import android.view.animation.AnimationUtils
import cn.chawloo.base.R
import cn.chawloo.base.databinding.ItemListTvBinding
import cn.chawloo.base.databinding.PopCustomListBinding
import cn.chawloo.base.ext.doClick
import cn.chawloo.base.ext.dp
import com.drake.brv.utils.dividerSpace
import com.drake.brv.utils.linear
import com.drake.brv.utils.setup
import razerdp.basepopup.BasePopupWindow

/**
 * TODO
 * @author Create by 鲁超 on 2021/3/3 0003 11:07
 */
class CommonListPopupWindow(c: Context, var title: String, var dataList: List<String>) : BasePopupWindow(c) {
    init {
        setContentView(R.layout.pop_custom_list)
        popupGravity = Gravity.CENTER
        setMaxHeight(300.dp)
        showAnimation = AnimationUtils.loadAnimation(context, R.anim.pop_middle_show)
        dismissAnimation = AnimationUtils.loadAnimation(context, R.anim.pop_middle_dismiss)
        val vb = PopCustomListBinding.bind(contentView)
        vb.tvTitle.text = title
        vb.ivClose.doClick { dismiss() }
        vb.recyclerview.linear()
            .dividerSpace(1.dp)
            .setup {
                addType<String>(R.layout.item_list_tv)
                onBind {
                    with(getBinding<ItemListTvBinding>()) {
                        textView.text = getModel<String>()
                    }
                }
            }.models = dataList
    }
}