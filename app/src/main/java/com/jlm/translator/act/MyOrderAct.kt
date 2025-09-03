package com.jlm.translator.act

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.dylanc.viewbinding.binding
import com.google.android.material.tabs.TabLayoutMediator
import com.gyf.immersionbar.ktx.immersionBar
import com.jlm.common.compat.BaseActCompat
import com.jlm.common.router.Rt
import com.jlm.translator.databinding.ActMyOrderBinding
import com.jlm.translator.fragment.BuyFragment
import com.jlm.translator.fragment.RechargeFragment
import com.therouter.router.Route

@Route(path = Rt.MyOrderAct)
class MyOrderAct : BaseActCompat() {
    private val vb by binding<ActMyOrderBinding>()
    private val titleList = listOf("充值", "购买")
    private val fragments = listOf(RechargeFragment(), BuyFragment())
    override fun initialize() {
        super.initialize()
        immersionBar {
            statusBarColor(android.R.color.white)
        }
        vb.vpMyOrder.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int {
                return fragments.size
            }

            override fun createFragment(position: Int): Fragment {
                return fragments[position]
            }

        }
        vb.vpMyOrder.offscreenPageLimit = 1
        vb.vpMyOrder.isUserInputEnabled = false
        TabLayoutMediator(vb.tabMyOrder, vb.vpMyOrder) { tab, position ->
            tab.text = titleList[position]
        }.attach()
    }
}