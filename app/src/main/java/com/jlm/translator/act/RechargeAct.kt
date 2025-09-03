package com.jlm.translator.act

import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import cn.chawloo.base.ext.doClick
import cn.chawloo.base.ext.dp
import cn.chawloo.base.ext.drawableLeftAndRightToText
import cn.chawloo.base.ext.size
import cn.chawloo.base.ext.sp
import com.drake.brv.annotaion.DividerOrientation
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.dividerSpace
import com.drake.brv.utils.grid
import com.drake.brv.utils.setup
import com.dylanc.viewbinding.binding
import com.gyf.immersionbar.ktx.immersionBar
import com.jlm.common.compat.BaseActCompat
import com.jlm.common.router.Rt
import com.jlm.translator.R
import com.jlm.translator.databinding.ActRechargeBinding
import com.jlm.translator.databinding.ItemRechargeBinding
import com.jlm.translator.entity.PayMethod
import com.jlm.translator.entity.RechargeProduct
import com.jlm.translator.pop.PayMethodPop
import com.jlm.translator.pop.RechargeFaqPop
import com.therouter.router.Route

@Route(path = Rt.RechargeAct)
class RechargeAct : BaseActCompat() {
    private val vb by binding<ActRechargeBinding>()
    private var payMethod = PayMethod(name = "谷歌内购", icon = R.drawable.charge_icon_google)
    override fun initialize() {
        super.initialize()
        immersionBar {
            statusBarColor(android.R.color.white)
        }
        vb.headBanner.setAction(R.drawable.tools_nav_icon_question) {
            RechargeFaqPop(this).showPopupWindow()
        }
        vb.tvBalance.text = buildSpannedString {
            size(18.sp) {
                bold {
                    append("${userModel?.balance ?: 0} ")
                }
            }
            size(12.sp) {
                append("天")
            }
        }
        vb.rvRecharge.grid(3)
            .dividerSpace(12.dp, DividerOrientation.VERTICAL)
            .setup {
                singleMode = true
                addType<RechargeProduct>(R.layout.item_recharge)
                onBind {
                    with(getBinding<ItemRechargeBinding>()) {
                        with(getModel<RechargeProduct>()) {
                            itemView.isSelected = isChecked
                            tvRechargeName.text = buildSpannedString {
                                size(24.sp) {
                                    bold {
                                        append("$name ")
                                    }
                                }
                                size(14.sp) {
                                    append("天")
                                }
                            }
                            tvRechargeName.isSelected = isChecked
                            tvPrice.text = "$currency${price}"
                            tvPrice.isSelected = isChecked
                        }
                    }
                }
                onChecked { position, isChecked, _ ->
                    val model = getModel<RechargeProduct>(position)
                    model.isChecked = isChecked
                    notifyItemChanged(position) // 通知UI跟随数据变化
                }
                onClick(R.id.item_view) {
                    setChecked(layoutPosition, true)
                }
            }
            .models = getTempRechargeProductList()
        vb.rvRecharge.bindingAdapter.setChecked(0, true)
    }

    private fun getTempRechargeProductList(): List<RechargeProduct> {
        return listOf(
            RechargeProduct(name = "30", price = 38.0, isChecked = true),
            RechargeProduct(name = "60", price = 68.0),
            RechargeProduct(name = "120", price = 128.0),
            RechargeProduct(name = "180", price = 188.0),
            RechargeProduct(name = "360", price = 388.0)
        )
    }

    override fun onClick() {
        super.onClick()
        vb.llPayMethod.doClick {
            PayMethodPop(this@RechargeAct) {
                payMethod = this
                this.setPayMethod()
            }.showPopupWindow()
        }
    }

    private fun PayMethod.setPayMethod() {
        vb.tvPayMethod.text = name
        vb.tvPayMethod.drawableLeftAndRightToText(leftRes = icon, rightRes = R.drawable.list_right_icon_next)
    }
}