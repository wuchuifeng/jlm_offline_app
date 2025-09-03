package com.jlm.translator.contract

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContract
import cn.chawloo.base.base.BUNDLE_NAME
import cn.chawloo.base.ext.bundleExtra
import com.jlm.translator.act.UpdateNicknameAct

class UpdateNicknameContract : ActivityResultContract<Bundle, Bundle?>() {
    override fun createIntent(context: Context, input: Bundle): Intent {
        return Intent(context, UpdateNicknameAct::class.java).apply {
            putExtra(BUNDLE_NAME, input)
        }
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Bundle? {
        return if (resultCode == RESULT_OK && intent != null) {
            intent.bundleExtra(BUNDLE_NAME)
        } else {
            null
        }
    }

}