package com.jlm.common.router

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import cn.chawloo.base.base.BUNDLE_NAME
import cn.chawloo.base.ext.toast
import cn.chawloo.base.utils.MK
import cn.chawloo.base.utils.MKKeys
import com.jlm.common.act.WebViewAct
import com.jlm.common.entity.UserModel
import com.therouter.TheRouter
import com.therouter.router.Navigator
import com.therouter.router.interceptor.NavigationCallback

fun isLogin(autoLogin: Boolean = false): Boolean {
    val token = MK.decodeString(MKKeys.KEY_TOKEN)
    val user = MK.decodeParcelable(MKKeys.KEY_USER, UserModel::class.java)
    val isLogin = token.isNotBlank() && user != null
    if (!isLogin && autoLogin) {
        goto(Rt.LoginAct)
    }
    return isLogin
}

fun logout() {
    MK.removeKeys(MKKeys.KEY_USER, MKKeys.KEY_TOKEN)
    TheRouter.build(Rt.LoginAct)
        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        .navigation()
}


fun goto(route: String, createBundle: Bundle.() -> Unit = {}) {
    TheRouter.build(route).withBundle(BUNDLE_NAME, Bundle().apply {
        createBundle(this)
    }).navigation()
}

fun Activity.goto(route: String, isFinish: Boolean = false, createBundle: Bundle.() -> Unit = {}) {
    TheRouter.build(route).withBundle(BUNDLE_NAME, Bundle().apply {
        createBundle(this)
    }).navigation(this, object : NavigationCallback() {
        override fun onArrival(navigator: Navigator) {
            if (isFinish) {
                finish()
            }
        }
    })
}

fun goHome() {
    TheRouter
        .build(Rt.MainAct)
        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        .navigation()
}

fun openWeb(webTitle: String? = "", webUrl: String? = null) {
    if (webUrl.isNullOrBlank()) {
        toast("地址为空，请重试")
        return
    }
    val bundle = Bundle().apply {
        putString(WebViewAct.WEB_TITLE, webTitle)
        putString(WebViewAct.WEB_URL, webUrl)
    }
    TheRouter.build(Rt.WebViewAct).withBundle(BUNDLE_NAME, bundle).navigation()
}

fun openRichText(title: String? = "", htmlStr: String? = "") {
    val bundle = Bundle().apply {
        putString(WebViewAct.WEB_TITLE, title)
        putString(WebViewAct.WEB_HTML, htmlStr)
    }
    TheRouter.build(Rt.RichTextAct).withBundle(BUNDLE_NAME, bundle).navigation()
}

object Rt {
    const val SplashAct = "com.jlm.translator.act.SplashAct" // 开屏页
    const val SplashNewAct = "com.jlm.translator.act.SplashNewAct" // 原始方式实现的开屏页
    const val MainAct = "com.jlm.translator.act.MainAct" //首页
    const val TranslationRecordsAct = "com.jlm.translator.act.TranslationRecordsAct"//首页-翻译记录页
    const val DialogueRecordsDetailsAct = "com.jlm.translator.act.DialogueRecordsDetailsAct"//首页-翻译记录详情页
    const val TranslationRecordsDetailsAct = "com.jlm.translator.act.TranslationRecordsDetailsAct"//首页-对话记录详情页
    const val TranslationRecordsSearchAct = "com.jlm.translator.act.TranslationRecordsSearchAct"//首页-翻译记录搜索

    const val SelectProductByAllEncyclopediaAct = "com.jlm.translator.act.SelectProductByAllEncyclopediaAct"//产品页-产品百科-选择产品页
    const val ProductInstructionsAct = "com.jlm.translator.act.ProductInstructionsAct"//产品页-产品百科-选择产品页-产品说明页

    const val ListenerModeSettingAct = "com.jlm.translator.act.speech.ListenerModeSettingAct"//翻译-同声模式-设置
    const val ListenModeAct = "com.jlm.translator.act.speech.ListenModeAct"//翻译-同声模式
    const val ListenBilingualModeAct = "com.jlm.translator.act.ListenBilingualModeAct"//翻译-同声模式(双语输出版)
    const val ListenBilingualOriginalModeAct = "com.jlm.translator.act.ListenBilingualOriginalModeAct" //翻译-同声模式(双语输出带原声)
    const val ListenFreeForMultiModeAct = "com.jlm.translator.act.speech.ListenFreeForMultiModeAct" //翻译-自由对话版
    const val ListenOriginalModeAct = "com.jlm.translator.act.ListenOriginalModeAct" //原声输出
    const val SpeakerModeAct = "com.jlm.translator.act.SpeakerModeAct"//翻译-短语对话模式
    const val ListenFreeForTwoTalkModeAct = "com.jlm.translator.act.speech.ListenFreeForTwoTalkModeAct" //翻译-双人自由对话
    const val DialogueModeSettingAct = "com.jlm.translator.act.DialogueModeSettingAct"//翻译-短语对话模式设置
    const val TextTranslationAct = "com.jlm.translator.act.TextTranslationAct"//文本翻译
    const val FavoriteRecordAct = "com.jlm.translator.act.FavoriteRecordAct"//文本翻译-收藏记录


    const val WebViewAct = "com.jlm.common.act.WebViewAct" //内置浏览器
    const val RichTextAct = "com.jlm.translator.act.RichTextAct" //富文本

    const val LoginAct = "com.jlm.translator.act.LoginAct" //登录
    const val PersonInfoAct = "com.jlm.translator.act.PersonInfoAct" //个人信息
    const val TransDeviceInfoAct = "com.jlm.translator.act.TransDeviceInfoAct"
    const val DeviceInfoAct = "com.jlm.translator.act.DeviceInfoAct" //设备信息
    const val UpdateNicknameAct = "com.jlm.translator.act.UpdateNicknameAct" //设置昵称
    const val RechargeAct = "com.jlm.translator.act.RechargeAct" //充值
    const val SettingAct = "com.jlm.translator.act.SettingAct" //设置
    const val AboutUsAct = "com.jlm.translator.act.AboutUsAct" //关于我们
    const val CompanyIntroAct = "com.jlm.translator.act.CompanyIntroAct" //公司介绍
    const val StorageSpaceAct = "com.jlm.translator.act.StorageSpaceAct" //存储空间
    const val FaqAct = "com.jlm.translator.act.FaqAct" //帮助与反馈
    const val ContactUsAct = "com.jlm.translator.act.ContactUsAct" //联系我们
    const val MyOrderAct = "com.jlm.translator.act.MyOrderAct" //我的订单
    const val RechargeDetailsAct = "com.jlm.translator.act.RechargeDetailsAct" //我的订单-充值详情
}