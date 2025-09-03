package com.jlm.translator.act

import android.content.Intent
import android.os.Bundle
import androidx.core.widget.addTextChangedListener
import cn.chawloo.base.base.BUNDLE_NAME
import cn.chawloo.base.ext.bundleExtra
import cn.chawloo.base.ext.doClick
import cn.chawloo.base.ext.string
import cn.chawloo.base.ext.textString
import com.dylanc.viewbinding.binding
import com.jlm.common.compat.BaseActCompat
import com.jlm.common.router.Rt
import com.jlm.translator.databinding.ActUpdateNicknameBinding
import com.therouter.router.Route

@Route(path = Rt.UpdateNicknameAct)
class UpdateNicknameAct : BaseActCompat() {
    private val vb by binding<ActUpdateNicknameBinding>()

    companion object {
        const val KEY_NICKNAME = "key_nickname"
    }

    override fun initialize() {
        super.initialize()
        val oldNickname = intent.bundleExtra(BUNDLE_NAME).string(KEY_NICKNAME)
        vb.etNickname.setText(oldNickname)
        vb.btnDone.isEnabled = oldNickname.isNotBlank() && oldNickname != vb.etNickname.textString()
        vb.etNickname.addTextChangedListener {
            vb.btnDone.isEnabled = !it.isNullOrBlank() && oldNickname != vb.etNickname.textString()
        }
    }

    override fun onClick() {
        super.onClick()
        vb.btnDone.doClick {
            setResult(RESULT_OK, Intent().apply {
                putExtra(BUNDLE_NAME, Bundle().apply {
                    putString(KEY_NICKNAME, vb.etNickname.textString())
                })
            })
            finish()
        }
    }
}