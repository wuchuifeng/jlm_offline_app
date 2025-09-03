package com.jlm.translator.pop

import android.content.Context
import android.view.Gravity
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.core.widget.addTextChangedListener
import cn.chawloo.base.ext.doClick
import cn.chawloo.base.ext.fromJson
import cn.chawloo.base.ext.gone
import cn.chawloo.base.ext.toJson
import cn.chawloo.base.ext.toast
import com.drake.brv.utils.divider
import com.drake.brv.utils.linear
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.jlm.translator.R
import com.jlm.translator.databinding.ItemLanguageBinding
import com.jlm.translator.databinding.ItemLanguageGroupBinding
import com.jlm.translator.databinding.PopLanguageSelectorBinding
import com.jlm.translator.entity.Language
import com.jlm.translator.entity.LanguageGroup
import com.jlm.translator.intelligent.provider.LanguageProvider
import com.jlm.translator.manager.LanguageModeEnum
import com.safframework.log.L
import razerdp.basepopup.BasePopupFlag
import razerdp.basepopup.BasePopupWindow

sealed class DoubleSelectorParams {
    class InputParam(val leftLanguage:  Language, val rightLanguage: Language, val leftLangGroupList: List<LanguageGroup>, val rightLangGroupList: List<LanguageGroup>)
    class callbackData(val langMode: LanguageModeEnum, val language: Language)
}

class DoubleLangSelectorPop(
    val context: Context,
    var selectLangMode: LanguageModeEnum,
    val inputParams: DoubleSelectorParams.InputParam,
    val callback: DoubleSelectorParams.callbackData.() -> Unit
): BasePopupWindow(context) {

    val TAG: String = javaClass.name

    var leftLanguage: Language = inputParams.leftLanguage
    var rightLanguage: Language = inputParams.rightLanguage
    val leftLanguageGroupList: List<LanguageGroup> = inputParams.leftLangGroupList
    val rightLanguageGroupList: List<LanguageGroup> = inputParams.rightLangGroupList

    private val vb: PopLanguageSelectorBinding

    init {
        setContentView(R.layout.pop_language_selector)
        showAnimation = AnimationUtils.loadAnimation(context, cn.chawloo.base.R.anim.pop_bottom_show)
        dismissAnimation = AnimationUtils.loadAnimation(context, cn.chawloo.base.R.anim.pop_bottom_dismiss)
        popupGravity = Gravity.BOTTOM
        setOverlayStatusbar(true)
        setOverlayStatusbarMode(BasePopupFlag.OVERLAY_MASK)
        setOverlayNavigationBar(true)
        setOverlayNavigationBarMode(BasePopupFlag.OVERLAY_STATUS_BAR)
        vb = PopLanguageSelectorBinding.bind(contentView)
        //初始化
        initLangData()
        setHeaderUi()
        initList()
        setLanguageList()
        doClick()
    }

    private fun initLangData() {
//        leftGroupList = languageLeftGroupList.toJson().fromJson<List<LanguageGroup>>() //languageLeftGroupList //源
//        rightGroupList = languageRightGroupList.toJson().fromJson<List<LanguageGroup>>() //languageRightGroupList // 目标

        vb.etLanguageSearch.addTextChangedListener {
            //TODO: 暂时隐藏
            setLanguageList(it?.toString() ?: "")
        }
    }

    private fun initList() {
        vb.rvLanguage.linear()
            .divider {
                setDivider(1)
                setColor(ContextCompat.getColor(context, cn.chawloo.base.R.color.divider_color))
            }
            .setup {
                addType<LanguageGroup>(R.layout.item_language_group)
                addType<Language>(R.layout.item_language)
                onBind {
                    when (itemViewType) {
                        R.layout.item_language_group -> {
                            with(getBinding<ItemLanguageGroupBinding>()) {
                                with(getModel<LanguageGroup>()) {
                                    tvGroupName.text = getCategoryName()
                                }
                            }
                        }

                        R.layout.item_language -> {
                            //获取position
                            with(getBinding<ItemLanguageBinding>()) {
                                with(getModel<Language>()) {
                                    ivExpand.gone()
                                    val isCheck = isChecked(this)
//                                    ivChecked.if2Visible(isCheck) //是否被勾选
                                    tvLanguageName.text = buildSpannedString {
                                        var primaryColorId = R.color.color_text_primary
                                        var secondColorId = R.color.color_text_describ
                                        if (isCheck) {
                                            primaryColorId = R.color.color_text_selected
                                            secondColorId = R.color.color_text_selected_describ
                                        }
                                        color(ContextCompat.getColor(context, primaryColorId)) {
                                            append(getLangName())
                                        }
                                        color(ContextCompat.getColor(context, secondColorId)) {
                                            append("  ${getCountryName()}")
                                        }
                                    }
                                    //字体粗
                                    if (isCheck) {
                                        tvLanguageName.paint.isFakeBoldText = true
                                    } else {
                                        tvLanguageName.paint.isFakeBoldText = false
                                    }
                                }
                            }
                        }
                    }
                }
                onClick(R.id.tv_group_name) {
                    expandOrCollapse()
//                    onExpand { g ->
//                        withBinding<ItemLanguageGroupBinding> {
//                            tvGroupName.isSelected = !g
//                        }
//                    }
                }
                onClick(R.id.item_view) {
                    getModelOrNull<Language>()?.run {
                        when (selectLangMode) {
                            LanguageModeEnum.LANG_SOURCE -> { //左边
                                leftLanguage = this
                                vb.tvFromLanguage.text = this.getLangName()
                                // 发送callback更新
                                DoubleSelectorParams.callbackData(selectLangMode, leftLanguage).callback()
                            }

                            LanguageModeEnum.LANG_TARGET -> { //右边
                                rightLanguage = this
                                vb.tvToLanguage.text = this.getLangName()
                                //更新voicer描述
                                vb.tvLangDescrib.text = "${getLangName()}说的是："
                                vb.tvVoicer.text = rightLanguage?.getCurVoicerName()
                                //发送callback更新
                                DoubleSelectorParams.callbackData(selectLangMode, rightLanguage).callback()
                            }

                            else -> {
                                toast("点击异常")
                            }
                        }
                        notifyDataSetChanged()
                    }
                }
            }
    }

    private fun setHeaderUi() {
        when(selectLangMode) {
            LanguageModeEnum.LANG_SOURCE -> { // 左边的语音选择
                vb.tvFromLanguage.isSelected = true
                vb.llVoicerSelect.gone()
            }
            LanguageModeEnum.LANG_TARGET -> { // 右
                vb.tvToLanguage.isSelected = true

            }
            else -> {}
        }
        vb.tvToLanguage.text = rightLanguage.getLangName() //右
        vb.tvFromLanguage.text = leftLanguage.getLangName() //左
    }

    private fun doClick() {
        //左边的点击
        vb.tvFromLanguage.doClick {
            selectLangMode = LanguageModeEnum.LANG_SOURCE
            isSelected = true
            vb.tvToLanguage.isSelected = false
            setLanguageList()
            vb.llVoicerSelect.gone()
        }
        //右边的点击
        vb.tvToLanguage.doClick {
            selectLangMode = LanguageModeEnum.LANG_TARGET
            isSelected = true
            vb.tvFromLanguage.isSelected = false
            setLanguageList()
        }

        vb.btnConfirm.doClick {
            dismiss()
        }
    }

    /**
     * 设置语言列表
     * */
    private fun setLanguageList(filter: String = "") {
        vb.rvLanguage.models = getGroupLangList(filter)
        //滚动指定位置
        scrollToSelectedPosition()
    }

    private fun scrollToSelectedPosition() {
        //TODO: 使用recycler models的方式 会将列表给拉平，暂时用这样处理
        when(selectLangMode) {
            LanguageModeEnum.LANG_SOURCE -> {
                var index = 0
                for (item in vb.rvLanguage.models!!) {
                    if (item is Language) {
                        if (item.getItemKey() == leftLanguage?.getItemKey()) {
                            vb.rvLanguage.scrollToPosition(index)
                            return
                        }
                    }
                    index++
                }
            }
            LanguageModeEnum.LANG_TARGET -> {
                var index = 0
                for (item in vb.rvLanguage.models!!) {
                    if (item is Language) {
                        if (item.getItemKey() == rightLanguage?.getItemKey()) {
                            vb.rvLanguage.scrollToPosition(index)
                            return
                        }
                    }
                    index++
                }
            }
            else -> {}
        }
    }



    private fun getGroupLangList(filter: String = ""): List<LanguageGroup> {
        return when (selectLangMode) {
            LanguageModeEnum.LANG_SOURCE -> {
                if (filter.isNotBlank()) {
                    LanguageProvider.getSearchLanguageGroupObj(leftLanguageGroupList, filter)
                } else {
                    leftLanguageGroupList
                }
            }

            LanguageModeEnum.LANG_TARGET -> {
                if (filter.isNotBlank()) {
                    LanguageProvider.getSearchLanguageGroupObj(rightLanguageGroupList, filter)
                } else {
                    rightLanguageGroupList
                }
            }

            else -> emptyList()
        }
    }

    private fun isChecked(language: Language): Boolean {
        var check = false
        when(selectLangMode){
            LanguageModeEnum.LANG_SOURCE -> if(language.getItemKey() == leftLanguage?.getItemKey()) check = true
            LanguageModeEnum.LANG_TARGET -> if(language.getItemKey() == rightLanguage?.getItemKey()) check = true
            else -> check = false
        }
        return check
    }

}