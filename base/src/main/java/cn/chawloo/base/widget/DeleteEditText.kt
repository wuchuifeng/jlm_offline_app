package cn.chawloo.base.widget

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.res.ResourcesCompat
import cn.chawloo.base.R

open class DeleteEditText : AppCompatEditText {
    private lateinit var clearIcon: Drawable

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attributeSet: AttributeSet?) : super(context, attributeSet) {
        init(context)
    }

    constructor(context: Context, attributeSet: AttributeSet?, defStyleAttrs: Int) : super(context, attributeSet, defStyleAttrs) {
        init(context)
    }

    fun init(context: Context) {
        clearIcon = ResourcesCompat.getDrawable(resources, R.drawable.trantext_input_icon_clear, context.theme)!!
        clearIcon.setBounds(0, 0, clearIcon.intrinsicWidth, clearIcon.intrinsicHeight)
        setOnTouchListener(object : OnTouchListener {
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                if (compoundDrawables[2] == null) {
                    return false
                }
                if (event.action != MotionEvent.ACTION_UP) {
                    return false
                }
                if (event.x > width - paddingRight - clearIcon.intrinsicWidth) {
                    setText("")
                    removeClearButton()
                }
                v.performClick()
                return false
            }
        })
        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                manageClearButton()
            }

            override fun afterTextChanged(s: Editable?) {}
        })
        this.setOnFocusChangeListener { _, _ ->
            manageClearButton()
        }
    }

    fun manageClearButton() {
        if (text?.toString().isNullOrBlank() || !isFocused) {
            removeClearButton()
        } else {
            addClearButton()
        }
    }

    private fun addClearButton() {
        setCompoundDrawables(compoundDrawables[0], compoundDrawables[1], clearIcon, compoundDrawables[3])
    }

    fun removeClearButton() {
        setCompoundDrawables(compoundDrawables[0], compoundDrawables[1], null, compoundDrawables[3])
    }

    /**
     * 设置回车事件  自带清除焦点
     * @param actionId 动作ID，例如IME_ACTION_SEARCH
     * @param block 具体执行内容 如果仅传入这个，则响应所有回车事件
     */
    fun setEnterAction(actionId: Int? = null, block: (() -> Unit)? = null) {
        this.setOnEditorActionListener { _, i, _ ->
            actionId?.run {
                if (i == actionId) {
                    block?.invoke()
                }
            } ?: run {
                block?.invoke()
            }
            clearFocus()
            true
        }
    }
}