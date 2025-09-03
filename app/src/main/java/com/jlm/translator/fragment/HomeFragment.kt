package com.jlm.translator.fragment

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.registerForActivityResult
import androidx.core.content.ContextCompat
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import cn.chawloo.base.base.BaseLazyFragment
import cn.chawloo.base.ext.doClick
import cn.chawloo.base.ext.gone
import cn.chawloo.base.ext.if2Visible
import cn.chawloo.base.ext.toast
import cn.chawloo.base.ext.visible
import com.drake.brv.utils.dividerSpace
import com.drake.brv.utils.linear
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.dylanc.viewbinding.binding
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.jlm.common.router.Rt
import com.jlm.common.router.goto
import com.jlm.translator.R
import com.jlm.translator.databinding.FragmentHomeBinding
import com.jlm.translator.databinding.ItemProductModeBinding
import com.jlm.translator.entity.ProductMode
import com.jlm.translator.entity.ProductType
import com.jlm.translator.manager.IntelliFuncInfoEnum
import com.jlm.translator.manager.IntelliVersionManager
import com.jlm.translator.manager.IntelligentVersionEnum
import com.jlm.translator.pop.DeviceConnectionPop
import com.jlm.translator.pop.VersionSelectPop
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class HomeFragment : BaseLazyFragment(R.layout.fragment_home) {
    private val vb by binding<FragmentHomeBinding>()
    private var defaultTitle = "AI同声翻译"
    //pop相关
    private lateinit var deviceListConnectPop: DeviceConnectionPop
    private lateinit var versionSelectPop: VersionSelectPop

    private lateinit var intelliVersionManager: IntelliVersionManager

    private var funcInfoList = emptyList<IntelliFuncInfoEnum>()

    override fun initialize() {
        super.initialize()
        intelliVersionManager = IntelliVersionManager.getInstance(mContext)
        funcInfoList = intelliVersionManager.getFuncInfoList() // 功能列表信息
        
        // 设置网格布局，适配3.99寸屏幕
        vb.rvProductMode.layoutManager = GridLayoutManager(mContext, 2).apply {
            spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return 1 // 每个item占1个span，实现2列布局
                }
            }
        }
        
        vb.rvProductMode.setup {
            addType<IntelliFuncInfoEnum>(R.layout.item_product_mode)
            onBind {
                with(getBinding<ItemProductModeBinding>()) {
                    with(getModel<IntelliFuncInfoEnum>()) {
                        ivProductModeIcon.setBackgroundResource(icon)
                        tvProductModeName.text = title
                        tvProductModeDesc.text = desc
                    }
                }
            }
            onClick(R.id.item_view) {
                goto(getModel<IntelliFuncInfoEnum>().url)
            }
        }

        updateData()
    }

    override fun lazyLoad() {
//        with(currentProduct) {
//            vb.tvDeviceName.text = buildSpannedString {
//                color(Color.BLACK) {
//                    append(productName)
//                }
//                if (productDesc.isNotBlank()) {
//                    color(ContextCompat.getColor(mContext, cn.chawloo.base.R.color.color_ab)) {
//                        append(productDesc)
//                    }
//                }
//            }
//        }
//        vb.rvProductMode.models = funcInfoList //mockProductModeList
    }

    override fun onClick() {
        super.onClick()
        
        // 点击设置按钮
        vb.tvDeviceName.doClick {
            val version = intelliVersionManager.getCurVersion()
            versionSelectPop = VersionSelectPop(mContext, version) {
                intelliVersionManager.switchVersion(this)
                updateData()
            }
            versionSelectPop.showAsDropDown(vb.tvDeviceName, 100, 5)
        }
    }

    private fun updateData() {
        funcInfoList = intelliVersionManager.getFuncInfoList()
        vb.rvProductMode.models = funcInfoList
        
        // 更新设备名称，适配新的UI风格
        vb.tvDeviceName.text = "${defaultTitle}-${intelliVersionManager.getCurVersion().title}"
    }

}