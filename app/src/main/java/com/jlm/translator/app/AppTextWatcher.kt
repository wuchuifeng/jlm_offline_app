package com.jlm.translator.app

import android.text.Editable
import android.text.TextWatcher

/***输入框内容变化监听*/
interface AppTextWatcher : TextWatcher {
    override fun onTextChanged(charSequence: CharSequence, p1: Int, p2: Int, p3: Int) {

    }

    override fun afterTextChanged(editable: Editable) {

    }

    override fun beforeTextChanged(editable: CharSequence, p1: Int, p2: Int, p3: Int) {

    }
}