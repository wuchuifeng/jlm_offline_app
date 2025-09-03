package com.jlm.translator.pop

import android.content.Context
import android.view.Gravity
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.core.widget.addTextChangedListener
import cn.chawloo.base.ext.doClick
import cn.chawloo.base.ext.gone
import cn.chawloo.base.ext.toast
import com.drake.brv.utils.divider
import com.drake.brv.utils.linear
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import razerdp.basepopup.BasePopupWindow
import com.jlm.translator.R
import com.jlm.translator.databinding.ItemLanguageBinding
import com.jlm.translator.databinding.ItemLanguageGroupBinding
import com.jlm.translator.databinding.PopLanguageMultiSelectorBinding
import com.jlm.translator.entity.Language
import com.jlm.translator.entity.LanguageGroup
import com.jlm.translator.intelligent.provider.LanguageProvider
import com.jlm.translator.manager.LanguageModeEnum
import razerdp.basepopup.BasePopupFlag

sealed class MultiSelectorParams {
    class InputParam(
        val sourceLangList:  List<Language>,
        val targetLeftLanguage: Language,
        val targetRightLanguage: Language,
        val sourceLangGroupList: List<LanguageGroup>,
        val targetLeftLangGroupList: List<LanguageGroup>,
        val targetRightLangGroupList: List<LanguageGroup>
    )
    class callbackData(val langMode: LanguageModeEnum, val language: Language, val sourceLangList: List<Language>)
}

class MultiLangSelectorPop(
    val context: Context,
    var selectLangMode: LanguageModeEnum,
    val inputParams: MultiSelectorParams.InputParam,
    val callback: MultiSelectorParams.callbackData.() -> Unit,
) : BasePopupWindow(context) {
    val TAG: String = javaClass.name
    private val vb: PopLanguageMultiSelectorBinding

//    var sourceLangStrList: MutableList<String> = inputParams.sourceLangStrs
    var sourceLangMutableList: MutableList<Language> = inputParams.sourceLangList.toMutableList()
    var targetLeftLanguage: Language = inputParams.targetLeftLanguage
    var targetRightLanguage: Language = inputParams.targetRightLanguage
    var sourceLangGroupList: List<LanguageGroup> = inputParams.sourceLangGroupList
    var targetLeftGroupList: List<LanguageGroup> = inputParams.targetLeftLangGroupList
    var targetRightGroupList: List<LanguageGroup> = inputParams.targetRightLangGroupList

    init {
        setContentView(R.layout.pop_language_multi_selector)
        showAnimation = AnimationUtils.loadAnimation(context, cn.chawloo.base.R.anim.pop_bottom_show)
        dismissAnimation = AnimationUtils.loadAnimation(context, cn.chawloo.base.R.anim.pop_bottom_dismiss)
        popupGravity = Gravity.BOTTOM
        setOverlayStatusbar(true)
        setOverlayStatusbarMode(BasePopupFlag.OVERLAY_MASK)
        setOverlayNavigationBar(true)
        setOverlayNavigationBarMode(BasePopupFlag.OVERLAY_NAVIGATION_BAR)
        //绑定视图
        vb = PopLanguageMultiSelectorBinding.bind(contentView)

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
//                                    tvGroupName.text = name
                                }
                            }
                        }

                        R.layout.item_language -> {
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
                            LanguageModeEnum.LANG_SOURCE -> { //源语言
//                                this.getItemKey().apply {
                                val itemkey = this.getItemKey()
                                //根据键值判断是否被选中
                                if (sourceLangMutableList.any { it.getItemKey() == itemkey }) {
                                    //删除对应的选择
                                    sourceLangMutableList.remove(this)
                                } else {
                                    if (sourceLangMutableList.size < 10) {
                                        // 添加选择的语种
                                        sourceLangMutableList.add(this)
                                    } else {
                                        toast("最多只能添加10个语种")
                                    }
                                }
                                //回调
                                if (sourceLangMutableList.isNotEmpty()) {
//                                        sourceLangStrList.callbackSourceList()
                                    MultiSelectorParams.callbackData(selectLangMode, Language(), sourceLangMutableList).callback()
                                }
//                                }
                            }

                            LanguageModeEnum.LANG_TARGET_LEFT -> { //目标-左
                                targetLeftLanguage = this
                                vb.tvToLeftLanguage.text = getLangName()
                                //回调
                                MultiSelectorParams.callbackData(selectLangMode, this, mutableListOf()).callback()
                            }

                            LanguageModeEnum.LANG_TARGET_RIGHT -> { //目标右
                                targetRightLanguage = this
                                vb.tvToRightLanguage.text = getLangName()
                                //回调
                                MultiSelectorParams.callbackData(selectLangMode, this, mutableListOf()).callback()
                            }

                            else->{}
                        }
                        notifyDataSetChanged()
                    }
                }
            }
    }

    private fun setHeaderUi() {
        when (selectLangMode) {
            LanguageModeEnum.LANG_SOURCE -> { // 输入语音
                vb.tvFromLanguage.isSelected = true
            }

            LanguageModeEnum.LANG_TARGET_LEFT -> { // 左声道
                vb.tvToLeftLanguage.isSelected = true
            }

            LanguageModeEnum.LANG_TARGET_RIGHT -> { // 右声道
                vb.tvToRightLanguage.isSelected = true
            }

            else -> {
            }
        }
//        vb.tvFromLanguage.text = lastSourceLanguage.getLangName()
        vb.tvToLeftLanguage.text = targetLeftLanguage.getLangName()
        vb.tvToRightLanguage.text = targetRightLanguage.getLangName()
    }

    private fun doClick() {
        //左边的点击
        vb.tvFromLanguage.doClick {
            selectLangMode = LanguageModeEnum.LANG_SOURCE
            isSelected = true
            vb.tvToLeftLanguage.isSelected = false
            vb.tvToRightLanguage.isSelected = false
            setLanguageList()
        }

        // 输出的左声道语言
        vb.tvToLeftLanguage.doClick {
            selectLangMode = LanguageModeEnum.LANG_TARGET_LEFT
            isSelected = true
            vb.tvFromLanguage.isSelected = false
            vb.tvToRightLanguage.isSelected = false
            setLanguageList()
        }

        //输出的右声道语言
        vb.tvToRightLanguage.doClick {
            selectLangMode = LanguageModeEnum.LANG_TARGET_RIGHT
            isSelected = true
            vb.tvFromLanguage.isSelected = false
            vb.tvToLeftLanguage.isSelected = false
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
        // 滚动到选中位置
        scrollToSelectedPosition()
    }

    private fun scrollToSelectedPosition() {
        //TODO: 使用recycler models的方式 会将列表给拉平，暂时用这样处理
        when(selectLangMode) {
            LanguageModeEnum.LANG_SOURCE -> { //暂不处理
                vb.rvLanguage.scrollToPosition(0)
            }
            LanguageModeEnum.LANG_TARGET_LEFT -> {
                var index = 0
                for (item in vb.rvLanguage.models!!) {
                    if (item is Language) {
                        if (item.getItemKey() == targetLeftLanguage?.getItemKey()) {
                            vb.rvLanguage.scrollToPosition(index)
                            return
                        }
                    }
                    index++
                }
            }
            LanguageModeEnum.LANG_TARGET_RIGHT -> {
                var index = 0
                for (item in vb.rvLanguage.models!!) {
                    if (item is Language) {
                        if (item.getItemKey() == targetRightLanguage?.getItemKey()) {
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
                    LanguageProvider.getSearchLanguageGroupObj(sourceLangGroupList, filter)
                } else {
                    sourceLangGroupList
                }
            }

            LanguageModeEnum.LANG_TARGET_LEFT -> {
                if (filter.isNotBlank()) {
                    LanguageProvider.getSearchLanguageGroupObj(targetLeftGroupList, filter)
                } else {
                    targetLeftGroupList
                }
            }

            LanguageModeEnum.LANG_TARGET_RIGHT -> {
                if (filter.isNotBlank()) {
                    LanguageProvider.getSearchLanguageGroupObj(targetRightGroupList, filter)
                } else {
                    targetRightGroupList
                }
            }

            else -> emptyList()
        }
    }

    private fun isChecked(language: Language): Boolean {
        var check = false
        when (selectLangMode) {
            LanguageModeEnum.LANG_SOURCE -> if (sourceLangMutableList.any { it.getItemKey() == language.getItemKey() }) check = true
            LanguageModeEnum.LANG_TARGET_LEFT -> if (language.getItemKey() == targetLeftLanguage.getItemKey()) check = true
            LanguageModeEnum.LANG_TARGET_RIGHT -> if (language.getItemKey() == targetRightLanguage.getItemKey()) check = true
            else -> {}
        }
        return check
    }
}