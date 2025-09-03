package com.jlm.translator.widget

import android.content.Context
import android.content.SharedPreferences
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.ViewConfiguration
import androidx.appcompat.widget.AppCompatTextView
import kotlin.math.abs

/**
 * 悬浮可拖动按钮 - 简化版本，固定在右边，只能垂直拖动
 */
class DraggableFloatingButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    private var isDragging = false
    private var lastTouchY = 0f
    private var initialTouchY = 0f
    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop
    
    private var onClickListener: (() -> Unit)? = null
    
    // 拖拽边界
    private var topBoundary: Float = 0f
    private var bottomBoundary: Float = Float.MAX_VALUE
    
    // SharedPreferences用于保存位置
    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences("floating_button_prefs", Context.MODE_PRIVATE)
    }
    
    // 实时保存位置
    private var currentTranslationY: Float = 0f
        set(value) {
            field = value
            // 实时保存到SharedPreferences
            prefs.edit().putFloat(KEY_TRANSLATION_Y, value).apply()
        }
    
    companion object {
        private const val KEY_TRANSLATION_Y = "translation_y"
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        // 恢复保存的Y位置
        restorePosition()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                isDragging = false
                lastTouchY = event.rawY
                initialTouchY = event.rawY
                return true
            }
            
            MotionEvent.ACTION_MOVE -> {
                val deltaY = event.rawY - lastTouchY
                
                // 判断是否开始拖拽
                if (!isDragging) {
                    val totalDistance = abs(event.rawY - initialTouchY)
                    if (totalDistance > touchSlop) {
                        isDragging = true
                    }
                }
                
                if (isDragging) {
                    // 只允许垂直移动
                    val newTranslationY = translationY + deltaY
                    
                    // 限制在边界内
                    val constrainedY = constrainVerticalBounds(newTranslationY)
                    translationY = constrainedY
                    currentTranslationY = constrainedY // 实时保存位置
                }
                
                lastTouchY = event.rawY
                return true
            }
            
            MotionEvent.ACTION_UP -> {
                if (!isDragging) {
                    // 如果没有拖拽，则触发点击事件
                    onClickListener?.invoke()
                } else {
                    // 确保最终位置被保存
                    currentTranslationY = translationY
                }
                isDragging = false
                return true
            }
        }
        return super.onTouchEvent(event)
    }
    
    /**
     * 设置拖拽边界
     */
    fun setDragBounds(topBoundary: Float, bottomBoundary: Float) {
        this.topBoundary = topBoundary
        this.bottomBoundary = bottomBoundary
        
        // 重新约束当前位置
        post {
            val constrainedY = constrainVerticalBounds(translationY)
            if (constrainedY != translationY) {
                translationY = constrainedY
                currentTranslationY = constrainedY
            }
        }
    }
    
    /**
     * 设置初始位置（仅在首次使用时）
     */
    fun setInitialPosition(translationY: Float) {
        post {
            // 检查是否已经有保存的位置
            val savedTranslationY = prefs.getFloat(KEY_TRANSLATION_Y, Float.MIN_VALUE)
            
            if (savedTranslationY == Float.MIN_VALUE) {
                // 如果没有保存的位置，使用初始位置
                val constrainedY = constrainVerticalBounds(translationY)
                this.translationY = constrainedY
                this.currentTranslationY = constrainedY
            }
            // 如果已有保存位置，不覆盖用户设置
        }
    }
    
    /**
     * 限制按钮在垂直边界内
     */
    private fun constrainVerticalBounds(newTranslationY: Float): Float {
        // 如果边界还没有设置，不做限制
        if (topBoundary == 0f && bottomBoundary == Float.MAX_VALUE) {
            return newTranslationY
        }
        
        // 计算按钮当前在父容器中的绝对位置
        val currentY = y + newTranslationY
        val buttonHeight = height.toFloat()
        
        // 临时调试信息
        Log.d("ConstraintDebug", "=== Constraint Check ===")
        Log.d("ConstraintDebug", "y: $y, newTranslationY: $newTranslationY")
        Log.d("ConstraintDebug", "currentY: $currentY, buttonHeight: $buttonHeight")
        Log.d("ConstraintDebug", "topBoundary: $topBoundary, bottomBoundary: $bottomBoundary")
        Log.d("ConstraintDebug", "Bottom check: currentY + buttonHeight = ${currentY + buttonHeight} vs bottomBoundary = $bottomBoundary")
        
        return when {
            currentY < topBoundary -> {
                val result = topBoundary - y
                Log.d("ConstraintDebug", "Hit top boundary, returning: $result")
                result
            }
            currentY + buttonHeight > bottomBoundary -> {
                val result = bottomBoundary - buttonHeight - y
                Log.d("ConstraintDebug", "Hit bottom boundary, returning: $result")
                result
            }
            else -> {
                Log.d("ConstraintDebug", "Within bounds, returning original: $newTranslationY")
                newTranslationY
            }
        }
    }
    
    /**
     * 恢复保存的Y位置
     */
    private fun restorePosition() {
        post {
            val savedTranslationY = prefs.getFloat(KEY_TRANSLATION_Y, Float.MIN_VALUE)
            if (savedTranslationY != Float.MIN_VALUE) {
                // 只有在有保存位置时才恢复
                val constrainedY = constrainVerticalBounds(savedTranslationY)
                translationY = constrainedY
                currentTranslationY = constrainedY
            }
        }
    }
    
    /**
     * 恢复到指定Y位置（用于MotionLayout状态切换后恢复位置）
     */
    fun restoreToPosition(translationY: Float) {
        post {
            val constrainedY = constrainVerticalBounds(translationY)
            this.translationY = constrainedY
            this.currentTranslationY = constrainedY
        }
    }
    
    /**
     * 获取当前Y位置
     */
    fun getCurrentTranslationY(): Float {
        return currentTranslationY
    }
    
    /**
     * 强制保存当前位置
     */
    fun saveCurrentPosition() {
        currentTranslationY = translationY
    }
    
    /**
     * 设置点击监听器
     */
    fun setOnClickListener(listener: () -> Unit) {
        this.onClickListener = listener
    }
} 