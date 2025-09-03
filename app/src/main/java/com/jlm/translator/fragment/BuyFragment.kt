package com.jlm.translator.fragment

import cn.chawloo.base.ext.empty
import com.dylanc.viewbinding.binding
import com.jlm.common.compat.BaseLazyFragmentCompat
import com.jlm.translator.R
import com.jlm.translator.databinding.FragmentOrderBinding

class BuyFragment : BaseLazyFragmentCompat(R.layout.fragment_order) {
    private val vb by binding<FragmentOrderBinding>()
    override fun lazyLoad() {
        vb.refresh.empty("暂无购买订单", R.drawable.order_img_unpty)
        vb.refresh.onRefresh { vb.refresh.finishRefreshWithNoMoreData() }
    }
}