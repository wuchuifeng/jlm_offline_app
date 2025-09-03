package com.jlm.translator.act

import androidx.core.content.ContextCompat
import cn.chawloo.base.ext.dp
import cn.chawloo.base.ext.toJson
import cn.chawloo.base.ext.toast
import cn.chawloo.base.model.BaseResult
import coil3.load
import com.drake.brv.annotaion.DividerOrientation
import com.drake.brv.utils.divider
import com.drake.brv.utils.grid
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.drake.net.Post
import com.drake.net.utils.scopeNetLife
import com.dylanc.viewbinding.binding
import com.gyf.immersionbar.ktx.immersionBar
import com.jlm.common.compat.BaseActCompat
import com.jlm.common.entity.DeviceListModel
import com.jlm.common.entity.DeviceModel
import com.jlm.common.router.Rt
import com.jlm.common.router.goto
import com.jlm.common.util.ApiUtil
import com.jlm.translator.R
import com.jlm.translator.databinding.ActSelectProductByAllEncyclopediaBinding
import com.jlm.translator.databinding.ItemProductBinding
import com.jlm.translator.entity.DeviceDto
import com.jlm.translator.entity.ProductType
import com.therouter.router.Route

/***发现页-产品百科-选择产品*/
@Route(path = Rt.SelectProductByAllEncyclopediaAct)
class SelectProductByAllEncyclopediaAct : BaseActCompat() {

    private val vb by binding<ActSelectProductByAllEncyclopediaBinding>()

    private val pList = mutableListOf<ProductType>()

    override fun initialize() {
        super.initialize()
        initStatusBar()
        initView()
        // 获取设备
        getDeviceList()
    }

    /***初始化状态栏颜色*/
    private fun initStatusBar() {
        immersionBar {
            statusBarColor(android.R.color.white)
        }
    }

    /***初始化视图*/
    private fun initView() {
        vb.refresh.onRefresh {
            finishRefresh()
        }
        vb.allProductList.grid(2)
            .divider {
                setDivider(width = 20.dp)
                setColor(
                    ContextCompat.getColor(
                        this@SelectProductByAllEncyclopediaAct,
                        android.R.color.white
                    )
                )
                orientation = DividerOrientation.GRID
                includeVisible = true
            }
            .setup {
                addType<ProductType>(R.layout.item_product)
                onBind {
                    with(getBinding<ItemProductBinding>()) {
                        with(getModel<ProductType>()) {
                            tvProductName.text = this.productName
                            ivProductIcon.load(this.productIconUrl)
//                                buildSpannedString {
//                                color(Color.BLACK) {
//                                    append(productName)
//                                }
//                                if (productDesc.isNotBlank()) {
//                                    color(
//                                        ContextCompat.getColor(
//                                            context,
//                                            cn.chawloo.base.R.color.color_ab
//                                        )
//                                    ) {
//                                        append(productDesc)
//                                    }
//                                }
//                            }
//                            ivProductIcon.setImageResource(productIcon)
                        }
                    }
                }
                onClick(R.id.item_view) {
                    goto(Rt.ProductInstructionsAct)
                }
            }
    }

    private fun getProductList(list: List<DeviceModel>): List<ProductType> {
        pList.clear()
        if(!list.isNullOrEmpty()) {
            list.forEach(){
                pList.add(ProductType(productName = it.device_name, productIconUrl = it.thumb_url))
            }
        }
        return pList
    }

    /**
     * 获取设备列表
     * */
    private fun getDeviceList() {
        scopeNetLife {
            Post<BaseResult<DeviceListModel?>>(ApiUtil.deviceListUrl) {
                json(DeviceDto("66293f815cfe249dba383fbc").toJson())
            }.await().data?.run {
//                MK.encode(MKKeys.KEY_USER, this)
                vb.allProductList.models = getProductList(this.result)
            }?: run {
                toast("获取产品列表失败")
            }
        }
    }
}