package com.jlm.translator.fragment

import android.annotation.SuppressLint
import cn.chawloo.base.ext.toast
import com.drake.brv.utils.divider
import com.drake.brv.utils.linear
import com.drake.brv.utils.setup
import com.dylanc.viewbinding.binding
import com.jlm.common.compat.BaseLazyFragmentCompat
import com.jlm.common.router.Rt
import com.jlm.common.router.goto
import com.jlm.translator.R
import com.jlm.translator.databinding.FragmentAiBinding
import com.jlm.translator.databinding.ItemAiBinding
import com.jlm.translator.entity.ProductMode

class AIFragment : BaseLazyFragmentCompat(R.layout.fragment_ai) {
    private val vb by binding<FragmentAiBinding>()

    @SuppressLint("SetTextI18n")
    override fun lazyLoad() {
        vb.refresh.onRefresh { finishRefresh() }
        vb.rvAi.linear()
            .divider {
                setDivider(20)
            }
            .setup {
                addType<ProductMode>(R.layout.item_ai)
                onBind {
                    with(getBinding<ItemAiBinding>()) {
                        with(getModel<ProductMode>()) {
                            ivProductModeIcon.setBackgroundResource(productModeIcon)//setImageResource(productModeIcon)
                            tvProductModeName.text = productModeName
                            tvProductModeDesc.text = productModeDesc
                        }
                    }
                }

                onClick(R.id.item_view) {
                    goto(getModel<ProductMode>().productModeUrl)
//                    if (IntelligentManager.getInstance().isConnectXHJDevice()) {
//                    goto(getModel<ProductMode>().productModeUrl)
//                    }
                }
            }
            .models = getModels()

    }

    private fun getModels(): MutableList<ProductMode> {
        return mutableListOf(
//            ProductMode(
//                productModeName = getString(R.string.ai_chat),
//                productModeDesc = getString(R.string.ai_chat_desc),
//                productModeIcon = R.mipmap.ic_home_item2,//R.drawable.home_mode_icon_original,
//                productModeUrl = Rt.AIChatAct,
//            ),
            ProductMode(
                productModeName = getString(R.string.ai_translation_record),
                productModeDesc = getString(R.string.ai_translation_record_desc),
                productModeIcon = R.mipmap.ic_home_item4,//R.drawable.home_mode_icon_freefortwo,
                productModeUrl = Rt.TranslationRecordsAct,
            ),
        )
    }
}