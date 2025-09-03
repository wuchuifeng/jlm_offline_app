package com.jlm.translator.act

import cn.chawloo.base.ext.doClick
import cn.chawloo.base.ext.textString
import cn.chawloo.base.ext.toJson
import cn.chawloo.base.ext.toast
import cn.chawloo.base.model.BaseResult
import cn.chawloo.base.utils.MK
import cn.chawloo.base.utils.MKKeys
import com.drake.net.Post
import com.drake.net.utils.scopeDialog
import com.dylanc.viewbinding.binding
import com.jlm.common.compat.BaseActCompat
import com.jlm.common.entity.LoginModel
import com.jlm.common.router.Rt
import com.jlm.common.router.goHome
import com.jlm.common.util.ApiUtil
import com.jlm.translator.databinding.ActLoginBinding
import com.jlm.translator.entity.LoginDto
import com.therouter.router.Route

@Route(path = Rt.LoginAct)
class LoginAct : BaseActCompat() {
    private val vb by binding<ActLoginBinding>()
    override fun initialize() {
        super.initialize()
        vb.btnOk.doClick {
            if (vb.etAccount.textString().isBlank()) {
                toast("请输入账号")
                return@doClick
            }
            if (vb.etPwd.textString().isBlank()) {
                toast("请输入密码")
                return@doClick
            }
            scopeDialog {
                val dto = LoginDto(phone = vb.etAccount.textString(), password = vb.etPwd.textString())
                val result = Post<BaseResult<LoginModel>>(ApiUtil.loginUrl) { json(dto.toJson()) }.await()
                result.data?.run {
                    userInfo?.run {
                        MK.encode(MKKeys.KEY_USER, this)
                    }
                    MK.encode(MKKeys.KEY_TOKEN, token)
                    goHome()
                } ?: run {
                    toast("登录失败")
                }
            }
        }
    }
}