package com.jlm.translator.act

import android.net.Uri
import androidx.core.content.ContextCompat
import cn.chawloo.base.ext.toJson
import cn.chawloo.base.model.BaseResult
import coil3.load
import coil3.request.error
import coil3.request.placeholder
import com.drake.brv.utils.divider
import com.drake.brv.utils.linear
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.drake.net.Post
import com.drake.net.utils.scopeNetLife
import com.dylanc.viewbinding.binding
import com.gyf.immersionbar.ktx.immersionBar
import com.jlm.common.compat.BaseActCompat
import com.jlm.common.entity.DeviceEncyclopediaListModel
import com.jlm.common.entity.DeviceEncyclopediaModel
import com.jlm.common.router.Rt
import com.jlm.common.router.openRichText
import com.jlm.common.util.ApiUtil
import com.jlm.translator.R
import com.jlm.translator.databinding.ActProductInstructionsBinding
import com.jlm.translator.databinding.ItemFoundProductEncyclopediaBinding
import com.jlm.translator.databinding.ItemProductInstructionsBinding
import com.jlm.translator.entity.DeviceEncyclopediaDto
import com.jlm.translator.entity.ProductInstructionsType
import com.jlm.translator.entity.ProductType
import com.therouter.router.Route

/***产品使用说明*/
@Route(path = Rt.ProductInstructionsAct)
class ProductInstructionsAct : BaseActCompat() {
    private val vb by binding<ActProductInstructionsBinding>()
    private val aList: MutableList<ProductType> = mutableListOf<ProductType>();


    override fun initialize() {
        super.initialize()
        initStatusBar()
        initView()
        //拉取数据
        getDeviceEncyclopediaList()
    }
    /***初始化状态栏颜色*/
    private fun initStatusBar() {
        immersionBar {
            statusBarColor(android.R.color.white)
        }
    }

    /***初始化视图*/
    private fun initView() {
    //初始化列表
        vb.rvProductTip.linear()
            .divider {
                setColor(ContextCompat.getColor(this@ProductInstructionsAct, cn.chawloo.base.R.color.color_f5))
                setDivider(2)
            }
            .setup {
                addType<ProductType>(R.layout.item_found_product_encyclopedia)
                onBind {
                    with(getBinding<ItemFoundProductEncyclopediaBinding>()) {
                        with(getModel<ProductType>()) {
                            tvProductName.text = productName
                            val url = Uri.parse(productIconUrl);
                            ivProductIcon.load(url) {
                                error(R.mipmap.ic_launcher)
                                placeholder(com.jlm.common.R.drawable.ic_launcher)
                            }
                        }
                    }
                }
                onClick(R.id.cl_root) {
                    // 进入详情页
                    aList.get(this.modelPosition).apply {
                        openRichText(this.productName, this.productDesc)
                    }
                }
            }
            //.models=getProductInstructions()
    }

    /***获取使用教程内容项*/
    private fun getProductInstructions():MutableList<ProductInstructionsType>{
        return mutableListOf(
            ProductInstructionsType("","M3如何连接蓝牙？"),
            ProductInstructionsType("","M3如何使用外放模式？")
        )
    }

    /**
     * 获取xtb设备百科列表
     * */
    private fun getDeviceEncyclopediaList() {
        scopeNetLife {
            Post<BaseResult<DeviceEncyclopediaListModel?>>(ApiUtil.deviceQsListUrl) {
                json(DeviceEncyclopediaDto(DeviceEncyclopediaDto.Param("66293fb85cfe249dba383fce")).toJson())
            }.await().data?.run {
                // 更新Ui
                vb.rvProductTip.models = getProductEncyclopediaList(this.result)
            }?: run {
            }
        }
    }

    /***获取产品百科数据*/
    private fun getProductEncyclopediaList(list: List<DeviceEncyclopediaModel>): List<ProductType> {
        aList.clear()
        if(!list.isNullOrEmpty()) {
            list.forEach(){
                aList.add(ProductType(productIconUrl = it.devices?.thumb_url, productName = it.title, productDesc = it.content))
            }
        }
        return aList
    }

}