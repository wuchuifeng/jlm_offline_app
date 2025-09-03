package com.jlm.translator.fragment

import android.annotation.SuppressLint
import com.drake.brv.utils.divider
import com.drake.brv.utils.linear
import com.drake.brv.utils.setup
import com.dylanc.viewbinding.binding
import com.jlm.common.compat.BaseLazyFragmentCompat
import com.jlm.common.router.Rt
import com.jlm.common.router.goto
import com.jlm.translator.R
import com.jlm.translator.databinding.FragmentOrderBinding
import com.jlm.translator.databinding.ItemRechargeRecordBinding
import com.jlm.translator.entity.RechargeRecordModel
import org.joda.time.LocalDateTime

class RechargeFragment : BaseLazyFragmentCompat(R.layout.fragment_order) {
    private val vb by binding<FragmentOrderBinding>()

    @SuppressLint("SetTextI18n")
    override fun lazyLoad() {
        vb.refresh.onRefresh { finishRefresh() }
        vb.rvRecharge.linear()
            .divider {
                setDivider(40)
            }
            .setup {
                addType<RechargeRecordModel>(R.layout.item_recharge_record)
                onBind {
                    with(getBinding<ItemRechargeRecordBinding>()) {
                        with(getModel<RechargeRecordModel>()) {
                            tvName.text = rechargeName
                            tvPrice.text = "+$price"
                            tvDate.text = LocalDateTime(date).toString("yyyy/MM/dd HH:mm")
                            tvPriceUnit.text = unit
                        }
                    }
                }

                onClick(R.id.root_view_recharge_record) {//查看详情
                    goto(Rt.RechargeDetailsAct)
                }
            }
            .models = getModels()

    }

    private fun getModels(): MutableList<RechargeRecordModel> {
        return mutableListOf(
            RechargeRecordModel("Fish卡充值", 1712751695000, "35"),
            RechargeRecordModel("Fish卡充值", 1712751695000, "30"),
            RechargeRecordModel("Fish卡充值", 1712751695000, "25"),
            RechargeRecordModel("Fish卡充值", 1712751695000, "10")
        )
    }
}