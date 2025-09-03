package com.jlm.translator.pop

import android.content.Context
import android.view.Gravity
import android.view.animation.AnimationUtils
import cn.chawloo.base.ext.doClick
import com.drake.brv.listener.ItemDifferCallback
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.dividerSpace
import com.drake.brv.utils.linear
import com.drake.brv.utils.setup
import com.jlm.translator.R
import com.jlm.translator.databinding.ItemPayMethodBinding
import com.jlm.translator.databinding.PopPayMethodBinding
import com.jlm.translator.entity.PayMethod
import razerdp.basepopup.BasePopupWindow

class PayMethodPop(context: Context, onConfirm: PayMethod.() -> Unit) : BasePopupWindow(context) {
    init {
        setContentView(R.layout.pop_pay_method)
        showAnimation = AnimationUtils.loadAnimation(context, cn.chawloo.base.R.anim.pop_middle_show)
        dismissAnimation = AnimationUtils.loadAnimation(context, cn.chawloo.base.R.anim.pop_middle_dismiss)
        popupGravity = Gravity.CENTER
        val vb = PopPayMethodBinding.bind(contentView)
        vb.rvPayMethod.linear()
            .dividerSpace(10)
            .setup {
                itemDifferCallback = object : ItemDifferCallback {
                    override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
                        return if (oldItem is PayMethod && newItem is PayMethod) {
                            oldItem.name == newItem.name
                        } else {
                            super.areItemsTheSame(oldItem, newItem)
                        }
                    }

                    override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
                        return if (oldItem is PayMethod && newItem is PayMethod) {
                            oldItem.name == newItem.name
                                    && oldItem.isChecked == newItem.isChecked
                        } else {
                            super.areContentsTheSame(oldItem, newItem)
                        }
                    }

                    override fun getChangePayload(oldItem: Any, newItem: Any) = true
                }
                addType<PayMethod>(R.layout.item_pay_method)
                onBind {
                    with(getBinding<ItemPayMethodBinding>()) {
                        with(getModel<PayMethod>()) {
                            ivPayMethodIcon.setImageResource(icon)
                            tvPayMethodName.text = name
                            rbChecked.isChecked = isChecked
                        }
                    }
                }
                singleMode = true
                onChecked { position, checked, _ ->
                    getModel<PayMethod>(position).isChecked = checked
                    notifyItemChanged(position)
                }
                onClick(R.id.item_view) {
                    setChecked(layoutPosition, true)
                }
            }
            .models = getPayMethod()
        vb.rvPayMethod.bindingAdapter.setChecked(0, true)
        vb.tvConfirm.doClick {
            vb.rvPayMethod.bindingAdapter.getCheckedModels<PayMethod>().firstOrNull()?.onConfirm()
            dismiss()
        }
        vb.ivClose.doClick {
            dismiss()
        }
    }

    private fun getPayMethod(): List<PayMethod> {
        return listOf(
            PayMethod(name = "支付宝支付", icon = R.drawable.charge_icon_alipay),
            PayMethod(name = "微信支付", icon = R.drawable.charge_icon_wechat),
        )
    }
}