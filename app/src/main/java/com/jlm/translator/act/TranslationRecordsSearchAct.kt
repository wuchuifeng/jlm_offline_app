package com.jlm.translator.act

import cn.chawloo.base.ext.doClick
import com.dylanc.viewbinding.binding
import com.gyf.immersionbar.ktx.immersionBar
import com.jlm.common.compat.BaseActCompat
import com.jlm.common.router.Rt
import com.jlm.translator.databinding.ActTranslationRecordsSearchBinding
import com.therouter.router.Route

/**翻译记录搜索*/
@Route(path = Rt.TranslationRecordsSearchAct)
class TranslationRecordsSearchAct : BaseActCompat() {

    private val vb by binding<ActTranslationRecordsSearchBinding>()

    override fun initialize() {
        super.initialize()
        initStatusBar()
        initView()
        initData()
        initListener()
    }

    /***初始化状态栏颜色*/
    private fun initStatusBar() {
        immersionBar {
            statusBarColor(cn.chawloo.base.R.color.bg_color)
        }
    }

    /***初始化视图*/
    private fun initView() {
    }


    /***初始化数据*/
    private fun initData() {

    }

    /***初始化事件监听*/
    private fun initListener() {
        vb.imgClose.doClick {
            finish()
        }
    }


}