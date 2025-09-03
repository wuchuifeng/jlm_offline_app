package com.jlm.translator.pop

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import cn.chawloo.base.ext.doClick
import cn.chawloo.base.ext.dp
import cn.chawloo.base.ext.if2Visible
import com.drake.brv.annotaion.DividerOrientation
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.dividerSpace
import com.drake.brv.utils.grid
import com.drake.brv.utils.setup
import com.jlm.translator.R
import com.jlm.translator.databinding.ItemProductBinding
import com.jlm.translator.databinding.PopSelectProductByHomeBinding
import com.jlm.translator.entity.ProductType
import razerdp.basepopup.BasePopupWindow

class SelectProductByHomePop(context: Context, productTypeList: List<ProductType>, position: Int, choose: ProductType.() -> Unit) : BasePopupWindow(context) {
    init {
        setContentView(R.layout.pop_select_product_by_home)
        popupGravity = Gravity.BOTTOM
        showAnimation = AnimationUtils.loadAnimation(context, cn.chawloo.base.R.anim.pop_top_show)
        dismissAnimation = AnimationUtils.loadAnimation(context, cn.chawloo.base.R.anim.pop_top_dismiss)
        val vb = PopSelectProductByHomeBinding.bind(contentView)
        vb.ivClose.doClick { dismiss() }
        vb.rvProduct.grid(2)
            .dividerSpace(20.dp, orientation = DividerOrientation.VERTICAL)
            .dividerSpace(10.dp, orientation = DividerOrientation.HORIZONTAL)
            .setup {
                addType<ProductType>(R.layout.item_product)
                onBind {
                    with(getBinding<ItemProductBinding>()) {
                        with(getModel<ProductType>()) {
                            tvProductName.text = buildSpannedString {
                                color(Color.BLACK) {
                                    append(productName)
                                }
                                if (productDesc.isNotBlank()) {
                                    color(ContextCompat.getColor(context, cn.chawloo.base.R.color.color_ab)) {
                                        append(productDesc)
                                    }
                                }
                            }
                            ivProductIcon.setImageResource(productIcon)
                            ivChecked.if2Visible(isSelected)
                        }
                    }
                }
                singleMode = true
                onChecked { position, checked, _ ->
                    val productType = getModel<ProductType>(position)
                    productType.isSelected = checked
                    notifyItemChanged(position)
                }
                onClick(R.id.item_view) {
                    setChecked(layoutPosition, true)
                    getModel<ProductType>().choose()
                    dismiss()
                }
            }.models = productTypeList
        vb.rvProduct.bindingAdapter.setChecked(position, true)
    }
}