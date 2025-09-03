package com.jlm.translator.act

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.lifecycleScope
import cn.chawloo.base.ext.clear
import cn.chawloo.base.ext.doClick
import cn.chawloo.base.ext.gone
import cn.chawloo.base.ext.if2Visible
import cn.chawloo.base.ext.textString
import cn.chawloo.base.ext.toast
import cn.chawloo.base.ext.visible
import cn.chawloo.base.utils.MK
import cn.chawloo.base.utils.MKKeys
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.mutable
import com.drake.net.utils.debounce
import com.drake.net.utils.launchIn
import com.dylanc.viewbinding.binding
import com.jlm.common.compat.BaseActCompat
import com.jlm.common.router.Rt
import com.jlm.common.router.goto
import com.jlm.translator.R
//import com.jlm.translator.azure.IntelligentSpeechDelegate
import com.jlm.translator.database.manager.FavoriteRecordDBManager
import com.jlm.translator.database.table.FavoriteRecordModel
import com.jlm.translator.databinding.ActTextTranslationBinding
import com.jlm.translator.entity.Language
import com.jlm.translator.entity.LanguageGroup
import com.jlm.translator.entity.RecordModel
import com.jlm.translator.listener.SpeechUpdateDataListener
import com.jlm.translator.manager.IntelligentFuncModeEnum
import com.jlm.translator.utils.LanguageUtil
import com.safframework.log.L
import com.therouter.router.Route
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/***文本翻译页面*/
@Route(path = Rt.TextTranslationAct)
class TextTranslationAct : BaseActCompat() {
    private val vb by binding<ActTextTranslationBinding>()

    private var textResultSb = StringBuilder()
    private var translateResultSb = ""

//    private lateinit var speechDelegate: IntelligentSpeechDelegate

    //是否收藏
    private var isCollected = false
//
//    private val dataListener = object : SpeechUpdateDataListener() {
//
//        override fun updateTargetTextItem(content: String, position: Int, type: Int) {
//            super.updateTargetTextItem(content, position, type)
//            lifecycleScope.launch(Dispatchers.Main) {
//                //显示文字
//                translateResultSb = content
//                vb.tvOutput.text = content
//                if (vb.tvOutput.textString().isNotBlank()) {
//                    visibleOutputViews()
//                }
//                //向底部滚动
//                vb.scrollView.post { vb.scrollView.fullScroll(NestedScrollView.FOCUS_DOWN) }
//            }
//        }
//
//        override fun updateLanguageUI(language: Language?, type: Int) {
//            super.updateLanguageUI(language, type)
//            lifecycleScope.launch(Dispatchers.Main) {
//                when (type) {
//                    IntelligentSpeechDelegate.type_langSource -> {
//                        vb.tvFromLanguage.text = language?.getLangName()
//                        vb.etInput.textString().takeIf { it.isNotBlank() }?.run {
//                            translate(this)
//                        }
//                    }
//                    IntelligentSpeechDelegate.type_langTarget -> {
//                        vb.tvToLanguage.text = language?.getLangName()
//                        vb.etInput.textString().takeIf { it.isNotBlank() }?.run {
//                            translate(this)
//                        }
//                    }
//                }
//            }
//        }
//    }
//
//
//    override fun initialize() {
//        super.initialize()
//        speechDelegate = IntelligentSpeechDelegate(this, dataListener, IntelligentFuncModeEnum.MODE_TEXT)
//        vb.etInput.debounce(300).launchIn(this) {
//            //隐藏输入框删除图标
//            vb.ivClear.if2Visible(it.isNotBlank())
//            //隐藏输入文本播报图标
//            vb.ivSpeakSourceContent.if2Visible(it.isNotBlank())
//            translate(it)
//        }
//    }
//
//    private fun translate(sourceContent: String) {
//        if (sourceContent.isNotBlank()) {
//            textResultSb.append(sourceContent)
//            // 开启翻译
//            speechDelegate.startTranslate(sourceContent, "")
////            mAliTranslateHelper.close()
////            mAliTranslateHelper.translate(sourceContent, sourceLanguage.key, targetLanguage.key, curSceneValue, 0, 1)
//        } else {
//            vb.tvOutput.clear()
//            goneViews()
//        }
//    }
//
//    override fun onClick() {
//        super.onClick()
//        //返回
//        vb.ivBack.doClick { finish() }
//
//        vb.llFromLanguage.doClick {
//            speechDelegate.showLanguageSelector(IntelligentSpeechDelegate.type_langSelectSource)
//        }
//        vb.llToLanguage.doClick {
//            speechDelegate.showLanguageSelector(IntelligentSpeechDelegate.type_langSelectTarget)
//        }
//        //收藏记录
//        vb.ivAction.doClick {
//            goto(Rt.FavoriteRecordAct)
//        }
//        //删除输入内容
//        vb.ivClear.doClick {
//            vb.etInput.clear()
//            vb.tvOutput.clear()
//            isCollected = false
//            goneViews()
//        }
//
//        vb.ivSpeakTargetContent.doClick {
////            this.playAnimation()
////            mAliSynthesisHelper.stopTrack()
////            mAliSynthesisHelper.start(vb.tvOutput.textString(), voicer, targetLanguage.key)
//            speechDelegate.playTextSpeechSynthesis(vb.tvOutput.textString())
//        }
//        //复制翻译内容
//        vb.ivCopy.doClick {
//            vb.tvOutput.textString().takeIf { it.isNotBlank() }?.run {
//                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
//                val clip = ClipData.newPlainText("text_translation_content", this)
//                clipboard.setPrimaryClip(clip)
//                toast("复制翻译内容成功")
//            }
//        }
//        //收藏
//        vb.ivCollect.doClick {
//            isCollected = if (isCollected) {
//                vb.ivCollect.setImageResource(R.drawable.trantext_result_icon_clooect_def)
//                vb.tvOutput.setTextColor(ContextCompat.getColor(this@TextTranslationAct, R.color.black))
//                FavoriteRecordDBManager.deleteLatest()
//                toast("取消收藏")
//                false
//            } else {
//                vb.ivCollect.setImageResource(R.drawable.trantext_result_icon_clooect_sel)
//                vb.tvOutput.setTextColor(ContextCompat.getColor(this@TextTranslationAct, cn.chawloo.base.R.color.theme_color))
//                //插入到收藏中
//                speechDelegate.insertTextTranslateFav(vb.etInput.textString(), vb.tvOutput.textString())
//                toast("收藏成功")
//                true
//            }
//        }
//    }
//
//    /**
//     * 清空页面状态
//     */
//    private fun goneViews() {
//        //隐藏输入框删除图标
//        vb.ivClear.gone()
//        //隐藏输入文本播报图标
//        vb.ivSpeakSourceContent.gone()
//        //隐藏分割线
//        vb.vSplitDivider.gone()
//        //隐藏翻译后文本播报图标
//        vb.ivSpeakTargetContent.gone()
//        //隐藏翻译后的文本框
//        vb.tvOutput.gone()
//        //隐藏底部控制视图
//        vb.llResultControl.gone()
//    }
//
//    /**
//     * 显示页面控件
//     */
//    private fun visibleOutputViews() {
//        //翻译后文本显示
//        vb.tvOutput.visible()
//        //显示分割线
//        vb.vSplitDivider.visible()
//        //显示翻译后的输入框
//        vb.ivSpeakTargetContent.visible()
//        //显示底部控制视图
//        vb.llResultControl.visible()
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        speechDelegate.close()
//    }
}