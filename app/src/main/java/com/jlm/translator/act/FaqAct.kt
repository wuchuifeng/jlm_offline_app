package com.jlm.translator.act

import android.text.Editable
import android.text.TextWatcher
import androidx.core.content.ContextCompat
import cn.chawloo.base.ext.clear
import cn.chawloo.base.ext.doClick
import cn.chawloo.base.ext.dp
import cn.chawloo.base.ext.if2Gone
import cn.chawloo.base.ext.if2Visible
import cn.chawloo.base.ext.toJson
import cn.chawloo.base.ext.toast
import cn.chawloo.base.model.BaseResult
import com.drake.brv.annotaion.DividerOrientation
import com.drake.brv.utils.divider
import com.drake.brv.utils.dividerSpace
import com.drake.brv.utils.grid
import com.drake.brv.utils.linear
import com.drake.brv.utils.setup
import com.drake.net.Post
import com.drake.net.utils.scopeDialog
import com.dylanc.viewbinding.binding
import com.gyf.immersionbar.ktx.immersionBar
import com.jlm.common.compat.BaseActCompat
import com.jlm.common.router.Rt
import com.jlm.translator.R
import com.jlm.translator.databinding.ActFaqBinding
import com.jlm.translator.databinding.ItemFaqBinding
import com.jlm.translator.databinding.ItemProblemImageBinding
import com.jlm.translator.entity.FaqType
import com.jlm.translator.entity.FeedbackDto
import com.jlm.translator.entity.ProblemImage
import com.safframework.log.L
import com.therouter.router.Route

@Route(path = Rt.FaqAct)
class FaqAct : BaseActCompat() {
    private val vb by binding<ActFaqBinding>()
    private val problemImages = ArrayList<ProblemImage>()

    private var typeList = mutableListOf<FaqType>()
    private var curFaqTypeIndex = 4
    override fun initialize() {
        super.initialize()
        getFaqTypeList()
        immersionBar {
            statusBarColor(android.R.color.white)
        }
        problemImages.add(ProblemImage(isAdd = true))
        vb.etProblemContent.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                s?.apply {
                    if (length >= 300) {
                        toast("内容最多只能输入300字")
                    }
                    vb.tvLastCount.text = "${(300 - length)}/300"
                }
            }

            override fun afterTextChanged(s: Editable?) {}

        })

        vb.rvFaq.linear()
            .divider {
                setDivider(1.dp)
                setColor(ContextCompat.getColor(this@FaqAct, cn.chawloo.base.R.color.bg_color))
            }
            .setup {
                addType<FaqType>(R.layout.item_faq)
                onBind {
                    with(getBinding<ItemFaqBinding>()) {
                        with(getModel<FaqType>()) {
                            tvFaqTypeName.text = typeName
                            rbChecked.isChecked = isChecked
                        }
                    }
                }
                singleMode = true
                onChecked { position, checked, _ ->
                    val model = getModel<FaqType>(position)
                    model.isChecked = checked
                    notifyItemChanged(position) // 通知UI跟随数据变化
                }
                onClick(R.id.item_view) {
//                    setChecked(curFaqTypeIndex, false)
//                    setChecked(layoutPosition, true)
                    typeList.get(curFaqTypeIndex).isChecked = false
                    typeList.get(layoutPosition).isChecked = true
                    notifyDataSetChanged()
                    // 更新当前check的position
                    curFaqTypeIndex = layoutPosition
                }
            }.models = typeList //getFaqTypeList()
//        vb.rvFaq.bindingAdapter.setChecked(0, true)

        vb.rvImg.grid(3, scrollEnabled = false)
            .dividerSpace(6.dp, DividerOrientation.VERTICAL)
            .setup {
                addType<ProblemImage>(R.layout.item_problem_image)
                onBind {
                    val binding = ItemProblemImageBinding.bind(itemView)
                    val img = getModel<ProblemImage>()
                    binding.ivDelete.if2Gone(img.isAdd)
                    binding.tvAddImg.if2Visible(img.isAdd)
                    if (!img.isAdd) {
//                        binding.ivImg.load(img.path) {
//                            transformations(RoundedCornersTransformation(6F.dp))
//                        }
                    }
                }
            }.models = problemImages
    }

    override fun onClick() {
        super.onClick()
        vb.btnSubmit.doClick {
            val phone = vb.etPhone.text?.toString() ?: ""
            val content = vb.etProblemContent.text?.toString() ?: ""
            
            // 验证必填字段
            if (phone.length != 11) {
                toast("请输入正确的手机号")
                return@doClick
            }
            
            // 问题描述是可选的，但如果用户填写了就传递
            val pictures = mutableListOf<String>()
            // TODO: 实现实际的图片上传功能
            // 暂时传递空列表，因为服务端的 feedImg 是可选的
            
            sendFeedback(phone, curFaqTypeIndex + 1, content, pictures)
        }
    }

    //发送问题反馈请求
    private fun sendFeedback(tel: String, type: Int, content: String, pictures: List<String>) {
        scopeDialog {
            try {
                val result = Post<BaseResult<String>>("feedbacks/create") {
                    json(FeedbackDto(
                        tel = tel,
                        feedType = type,
                        feedDesc = content,
//                        feedImg = pictures
                    ).toJson())
                }.await()
                L.d("FaqAct", "反馈结果: $result")
                // 根据BaseResult的结构判断是否成功
                if (result.status == 200 || result.status == 0) {
                    // 确保在主线程更新UI
                    toast("反馈成功")
                    clearInputInfo()
                } else {
                    toast(result.message ?: "反馈失败，请稍后重试")
                }
            } catch (e: Exception) {
                toast("网络异常，请检查网络连接")
                e.printStackTrace()
            }
        }
    }

    private fun getFaqTypeList(): List<FaqType> {
        typeList.clear();
        typeList = mutableListOf(
            FaqType("识别/翻译/播报问题", false),      // 对应服务端 feedType: 1
            FaqType("软件体验问题", false),           // 对应服务端 feedType: 2  
            FaqType("耳机连接问题", false),           // 对应服务端 feedType: 3
            FaqType("翻译记录问题", false),           // 对应服务端 feedType: 4
            FaqType("其他问题", true),               // 对应服务端 feedType: 5
        )
        return typeList
    }

    private fun clearInputInfo() {
        vb.etPhone.clear()
        vb.etProblemContent.clear()
        // 手动重置字符计数器
        vb.tvLastCount.text = "300/300"
        
        typeList.get(curFaqTypeIndex).isChecked = false
        typeList.apply {
            get(curFaqTypeIndex).isChecked = false
            get(4).isChecked = true
            curFaqTypeIndex = 4
            vb.rvFaq.adapter?.notifyDataSetChanged()
        }
    }
}