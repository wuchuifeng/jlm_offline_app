package com.jlm.translator.pop

import android.content.Context
import android.view.Gravity
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat
import cn.chawloo.base.ext.if2Visible
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.divider
import com.drake.brv.utils.linear
import com.drake.brv.utils.setup
import com.jlm.translator.R
import com.jlm.translator.databinding.ItemTranslationModeBinding
import com.jlm.translator.databinding.PopTranslationModeBinding
import com.jlm.translator.entity.TranslationModeModel
import razerdp.basepopup.BasePopupFlag
import razerdp.basepopup.BasePopupWindow

/**
 * 选择翻译模式弹窗
 * 0 全部
 * 1 同声模式
 * 2 同声模式 双语输出
 * 3 自由对话-双语输出 //中英对话互译
 * 4 双语对话 //短语对话
 */
class TranslationModePop(context: Context, position: Int = 0, choose: (Int) -> Unit) : BasePopupWindow(context) {

    companion object{
        const val type_all: Int = 0 //全部
        const val type_listen: Int = 1 //同声
        const val type_listen_bilingual: Int = 2 //同声 双语
        const val type_listen_freetomulti: Int = 3 //
        const val type_listen_freetotwo: Int = 4
//        const val type_zh_en_dialogue: Int = 3 //中英对话
//        const val type_short_dialogue: Int = 4 //短语对话
    }

    init {
        setContentView(R.layout.pop_translation_mode)
        showAnimation = AnimationUtils.loadAnimation(getContext(), cn.chawloo.base.R.anim.pop_top_show)
        dismissAnimation = AnimationUtils.loadAnimation(getContext(), cn.chawloo.base.R.anim.pop_top_dismiss)
        setOverlayNavigationBarMode(BasePopupFlag.OVERLAY_MASK)
        setOverlayMask(true)
        setAlignBackground(true)
        setAlignBackgroundGravity(Gravity.TOP)
        popupGravity = Gravity.BOTTOM
        val vb = PopTranslationModeBinding.bind(contentView)
        vb.rvTranslationMode.linear()
            .divider {
                setDivider(20)
            }
            .setup {
                addType<TranslationModeModel>(R.layout.item_translation_mode)
                onBind {
                    with(getBinding<ItemTranslationModeBinding>()) {
                        with(getModel<TranslationModeModel>()) {
                            //是否显示选中图标
                            ivSelected.if2Visible(isChecked)
                            tvModeName.text = modelName
                            if (isChecked) {

                                tvModeName.setTextColor(ContextCompat.getColor(context, cn.chawloo.base.R.color.theme_color))
                            } else {
                                tvModeName.setTextColor(ContextCompat.getColor(context, R.color.color_262626))
                            }
                        }
                    }
                }
                singleMode = true
                onChecked { position, checked, _ ->
                    val model = getModel<TranslationModeModel>(position)
                    model.isChecked = checked
                    notifyItemChanged(position) // 通知UI跟随数据变化
                }

                onClick(R.id.ll_root_view) {
                    setChecked(layoutPosition, true)
                    choose(layoutPosition)
                    dismiss()
                }

            }
            .models = getModeList()
        //默认选中项
        vb.rvTranslationMode.bindingAdapter.setChecked(position, true)
    }

    private fun getModeList(): List<TranslationModeModel> {
        return mutableListOf(
            TranslationModeModel(type_all, false, "全部"),
            TranslationModeModel(type_listen, false, "同声翻译"),
            TranslationModeModel(type_listen_bilingual, false, "同声翻译(双语输出)"),
            TranslationModeModel(type_listen_freetomulti, false, "自由对话(双语输出)"),
            TranslationModeModel(type_listen_freetotwo, false, "双语对话"),
        )

    }
}