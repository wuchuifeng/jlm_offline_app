package com.jlm.translator.pop

import android.content.Context
import android.view.LayoutInflater
import android.widget.PopupWindow
import androidx.core.content.ContextCompat
import com.drake.brv.utils.divider
import com.drake.brv.utils.linear
import com.drake.brv.utils.setup
import com.jlm.translator.R
import com.jlm.translator.databinding.ItemVersionBinding
import com.jlm.translator.databinding.PopVersionSelectBinding
import com.jlm.translator.manager.IntelligentVersionEnum

class VersionSelectPop(val context: Context, private var selectVersion: IntelligentVersionEnum, val callback: IntelligentVersionEnum.() -> Unit) : PopupWindow(context) {

    private val binding: PopVersionSelectBinding

    init {
        contentView = LayoutInflater.from(context).inflate(R.layout.pop_version_select, null)
        binding = PopVersionSelectBinding.bind(contentView)
        isFocusable = true
        isOutsideTouchable = true

        val data: List<IntelligentVersionEnum> = IntelligentVersionEnum.getVersionList()

        initView(data)


        // 设置默认选中项的字体颜色为蓝色
//        when (selectedIndex) {
//            0 -> binding.tvDomesticVersion.setTextColor(ContextCompat.getColor(context, R.color.blue))
//            1 -> binding.tvMainlandVersion.setTextColor(ContextCompat.getColor(context, R.color.blue))
//        }

//        binding.tvDomesticVersion.setOnClickListener {
//            // 处理国内版点击事件
//            dismiss()
//        }
//
//        binding.tvMainlandVersion.setOnClickListener {
//            // 处理大陆版点击事件
//            dismiss()
//        }
    }

    private fun initView(data: List<IntelligentVersionEnum>) {
        binding.recyclerView.linear()
            .divider {
                setDivider(1)
                setColor(ContextCompat.getColor(context, cn.chawloo.base.R.color.divider_color))
            }
            .setup {
                addType<IntelligentVersionEnum>(R.layout.item_version)
                onBind {
                    with(getBinding<ItemVersionBinding>()) {
                        with(getModel<IntelligentVersionEnum>()) {
                            val isCheck = isChecked(this)
                            tvVersion.text = this.title
                            //字体粗
                            if (isCheck) {
                                tvVersion.paint.isFakeBoldText = true
                                tvVersion.setTextColor(ContextCompat.getColor(context, R.color.color_text_selected))
                            } else {
                                tvVersion.paint.isFakeBoldText = false
                                tvVersion.setTextColor(ContextCompat.getColor(context, R.color.color_text_primary))
                            }
                        }
                    }
                }
                onClick(R.id.item_view) {
                    getModelOrNull<IntelligentVersionEnum>()?.run {
                        dismiss()
                        //返回数据
                        this.callback()
                        selectVersion = this
//                        notifyDataSetChanged()
                    }
                }
            }
            .models = data
    }

    private fun isChecked(version: IntelligentVersionEnum): Boolean {
        return selectVersion == version
    }
}