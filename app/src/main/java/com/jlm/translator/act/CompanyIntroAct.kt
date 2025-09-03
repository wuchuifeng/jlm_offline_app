package com.jlm.translator.act

import androidx.core.content.ContextCompat
import androidx.core.text.buildSpannedString
import cn.chawloo.base.ext.appVersionCode
import cn.chawloo.base.ext.appVersionName
import cn.chawloo.base.ext.appendClickable
import cn.chawloo.base.ext.doClick
import cn.chawloo.base.ext.if2Visible
import com.dylanc.viewbinding.binding
import com.gyf.immersionbar.ktx.immersionBar
import com.jlm.common.cache.GlobalManager.getAppConfig
import com.jlm.common.compat.BaseActCompat
import com.jlm.common.net.NetConstants
import com.jlm.common.router.Rt
import com.jlm.common.router.openWeb
import com.jlm.translator.databinding.ActAboutUsBinding
import com.jlm.translator.databinding.ActCompanyIntroductionBinding
import com.therouter.router.Route

@Route(path = Rt.CompanyIntroAct)
class CompanyIntroAct : BaseActCompat() {
    private val vb by binding<ActCompanyIntroductionBinding>()
    private var hasNew = false

    val content = "极力米（深圳）技术有限公司创立于2023年，专注于一对多/多对多的音频通信，致力于软硬件结合，通过敏锐新颖的市场需求洞察，为国际交流、会议讲演、旅游导览、政企接待、教育培训、娱乐生活等提供场景产品，持续为行业客户提供定制化音频通信解决方案。\n" +
            "\n" +
            "公司围绕“一对多通信”技术，先后上市一对多/二对多讲解设备、自动导览设备、分区讲解设备；围绕“娱乐生活”的场景化需求，打造了广场舞耳机、无声派对耳麦、小型影院耳麦等产品，应用于群体娱乐、个人生活。\n" +
            "\n" +
            "2024年6月，极力米倾力打造的AI智能同声翻译器/翻译耳机上市，自研硬件软件结合，依托全球领先的AI翻译中心巨头，首创多耳机同声传译、翻译双语同声播报、多人多语互译，致力跨语种交流尤其是群体沟通，为国际会议、跨国旅游、外贸往来等行业的跨语种沟通提供解决方案。\n" +
            "\n" +
            "极力米构建了涵盖“国内+海外”、“线上+线下”的全球化营销网络布局，在一对多通信领域拥有完全自主知识产权、自主研发生产能力，并获得欧盟、美国、日韩等主要市场的国际认证，为进一步发展打下坚实基础。"

    override fun initialize() {
        super.initialize()
        immersionBar {
            fitsSystemWindows(false)
        }

        vb.tvIntro.text = content
    }

}