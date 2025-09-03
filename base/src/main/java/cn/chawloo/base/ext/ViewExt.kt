package cn.chawloo.base.ext

import android.content.Context
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.GradientDrawable
import android.os.CountDownTimer
import android.view.MotionEvent
import android.view.TouchDelegate
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * TODO
 * @author Create by 鲁超 on 2021/3/9 0009 16:57
 *----------Dragon be here!----------/
 *       ┌─┐      ┌─┐
 *     ┌─┘─┴──────┘─┴─┐
 *     │              │
 *     │      ─       │
 *     │  ┬─┘   └─┬   │
 *     │              │
 *     │      ┴       │
 *     │              │
 *     └───┐      ┌───┘
 *         │      │神兽保佑
 *         │      │代码无BUG！
 *         │      └──────┐
 *         │             ├┐
 *         │             ┌┘
 *         └┐ ┐ ┌───┬─┐ ┌┘
 *          │ ┤ ┤   │ ┤ ┤
 *          └─┴─┘   └─┴─┘
 *─────────────神兽出没───────────────/
 */
fun <T : View> T.gone() {
    if (this.visibility != View.GONE) {
        this.visibility = View.GONE
    }
}

fun <T : View> T.visible(): T {
    if (this.visibility != View.VISIBLE) {
        this.visibility = View.VISIBLE
    }
    return this
}

fun <T : View> T.Invisible(): T {
    if (this.visibility != View.INVISIBLE) {
        this.visibility = View.INVISIBLE
    }
    return this
}

/**
 * 如果visible 则Visible  否则 Gone
 */
fun <T : View> T.if2Visible(isVisible: Boolean): T {
    if (isVisible) {
        this.visible()
    } else {
        this.gone()
    }
    return this
}

/**
 * 如果gone 则Gone  否则 Visible
 * @param isGone 是否隐藏
 */
fun <T : View> T.if2Gone(isGone: Boolean): T {
    if (isGone) {
        this.gone()
    } else {
        this.visible()
    }
    return this
}

/**
 * 把显示状态反转
 */
fun <T : View> T.reverseVisible() {
    if (this.isShown) {
        gone()
    } else {
        visible()
    }
}

/**
 * 把显示状态反转
 */
fun <T : View> T.reverseSelected() {
    this.isSelected = !this.isSelected
}

fun <T : TextView> T.clear() {
    this.text = ""
}

/**
 * 开启倒计时
 */
fun TextView.startCountDown(
    lifecycleOwner: LifecycleOwner,
    secondInFuture: Long = 60,
    step: Long = 1000,
    isDisableWhileDown: Boolean = true,
    onTick: TextView.(Long) -> Unit,
    onFinish: TextView.() -> Unit
): CountDownTimer {
    val time = object : CountDownTimer(secondInFuture * 1000, step) {
        override fun onTick(millisUntilFinished: Long) {
            this@startCountDown.apply {
                if (isDisableWhileDown) {
                    isEnabled = false
                }
                onTick(millisUntilFinished / 1000)
            }
        }

        override fun onFinish() {
            isEnabled = true
            this@startCountDown.onFinish()
        }
    }
    time.start()
    lifecycleOwner.lifecycle.addObserver(object : LifecycleEventObserver {
        override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
            if (event == Lifecycle.Event.ON_DESTROY) {
                time.cancel()
            }
        }
    })
    return time
}

fun <T : View> T.doClick(clickIntervals: Int = 300, isSharingIntervals: Boolean = false, block: T.() -> Unit) = setOnClickListener {
    this.context.asActivity()?.hideSoftKeyboard()
    val view = if (isSharingIntervals) context.asActivity()?.window?.decorView ?: this else this
    val currentTime = System.currentTimeMillis()
    val lastTime = view.lastClickTime ?: 0L
    if (currentTime - lastTime > clickIntervals) {
        view.lastClickTime = currentTime
        block()
    }
}

fun View.doOnDebouncingLongClick(clickIntervals: Int = 300, isSharingIntervals: Boolean = false, block: () -> Unit) =
    doOnLongClick {
        val view = if (isSharingIntervals) context.asActivity()?.window?.decorView ?: this else this
        val currentTime = System.currentTimeMillis()
        if (currentTime - (view.lastClickTime ?: 0L) > clickIntervals) {
            view.lastClickTime = currentTime
            block()
        }
    }

inline fun List<View>.doOnLongClick(crossinline block: () -> Unit) = forEach { it.doOnLongClick(block) }

inline fun View.doOnLongClick(crossinline block: () -> Unit) =
    setOnLongClickListener {
        block()
        true
    }

/**
 * 不改变View大小 扩大点击区域
 */
fun View.expandClickArea(expandSize: Float) = expandClickArea(expandSize.toInt())

/**
 * 不改变View大小 扩大点击区域
 */
fun View.expandClickArea(expandSize: Int) =
    expandClickArea(expandSize, expandSize, expandSize, expandSize)

/**
 * 不改变View大小 扩大点击区域
 */
fun View.expandClickArea(top: Float, left: Float, right: Float, bottom: Float) =
    expandClickArea(top.toInt(), left.toInt(), right.toInt(), bottom.toInt())

/**
 * 不改变View大小 扩大点击区域
 */
fun View.expandClickArea(top: Int, left: Int, right: Int, bottom: Int) {
    val parent = parent as? ViewGroup ?: return
    parent.post {
        val rect = Rect()
        getHitRect(rect)
        rect.top -= top
        rect.left -= left
        rect.right += right
        rect.bottom += bottom
        val touchDelegate = parent.touchDelegate
        if (touchDelegate == null || touchDelegate !is MultiTouchDelegate) {
            parent.touchDelegate = MultiTouchDelegate(rect, this)
        } else {
            touchDelegate.put(rect, this)
        }
    }
}

class MultiTouchDelegate(bound: Rect, delegateView: View) : TouchDelegate(bound, delegateView) {
    private val map = mutableMapOf<View, Pair<Rect, TouchDelegate>>()
    private var targetDelegate: TouchDelegate? = null

    init {
        put(bound, delegateView)
    }

    fun put(bound: Rect, delegateView: View) {
        map[delegateView] = bound to TouchDelegate(bound, delegateView)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x.toInt()
        val y = event.y.toInt()
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                targetDelegate = map.entries.find { it.value.first.contains(x, y) }?.value?.second
            }

            MotionEvent.ACTION_CANCEL -> {
                targetDelegate = null
            }
        }
        return targetDelegate?.onTouchEvent(event) ?: false
    }
}

fun <T> viewTags(key: Int) = object : ReadWriteProperty<View, T?> {
    @Suppress("UNCHECKED_CAST")
    override fun getValue(thisRef: View, property: KProperty<*>): T? = thisRef.getTag(key) as? T
    override fun setValue(thisRef: View, property: KProperty<*>, value: T?) = thisRef.setTag(key, value)
}

/**
 * 获取TextView的值，如果返回空白
 * 减少判空逻辑
 */
fun TextView.textString() = text?.toString() ?: ""

/**
 * 判断是否有值
 * @param isTrim 判定条件是否删除空格  是  " " 为true   否   " " 为false
 */
fun TextView.hasText(isTrim: Boolean = true): Boolean {
    return if (isTrim) {
        textString().isNotBlank()
    } else {
        textString().isNotEmpty()
    }
}

/**
 * 富文本点击时间
 */
fun TextView.transparentHighlightColor() {
    highlightColor = Color.TRANSPARENT
}

/**
 * 调整ViewPager2的滑动因子
 * "6" was obtained experimentally
 */
fun ViewPager2.reduceDragSensitivity(touchSlopScale: Int = 6) {
    val recyclerViewField = ViewPager2::class.java.getDeclaredField("mRecyclerView")
    recyclerViewField.isAccessible = true
    val recyclerView = recyclerViewField.get(this) as RecyclerView
    val touchSlopField = RecyclerView::class.java.getDeclaredField("mTouchSlop")
    touchSlopField.isAccessible = true
    val touchSlop = touchSlopField.get(recyclerView) as Int
    touchSlopField.set(recyclerView, touchSlop * touchSlopScale)
}

/**
 * 给TextView设置Top图标
 * 注意这个方法会导致其他的图标删除
 */
fun TextView.drawableTopToText(mCtx: Context, resId: Int): TextView {
    val drawable = ContextCompat.getDrawable(mCtx, resId)
    drawable!!.setBounds(0, 0, drawable.minimumWidth, drawable.minimumHeight)
    setCompoundDrawables(null, drawable, null, null)
    return this
}

/**
 * 给TextView设置Top图标
 * 注意这个方法会导致其他的图标删除
 */
fun TextView.drawableLeftAndRightToText(leftRes: Int, rightRes: Int): TextView {
    val leftDrawable = ContextCompat.getDrawable(context, leftRes).apply {
        this!!.setBounds(0, 0, this.minimumWidth, this.minimumHeight)
    }
    val rightDrawable = ContextCompat.getDrawable(context, rightRes).apply {
        this!!.setBounds(0, 0, this.minimumWidth, this.minimumHeight)
    }
    setCompoundDrawables(null, leftDrawable, rightDrawable, null)
    return this
}

fun View.backgroundShape(bgColorId: Int = 0, borderWidth: Int = 0, borderColorId: Int = 0, radius: Float): View {
    val gd = GradientDrawable()
    gd.cornerRadius = radius.dp
    if (bgColorId > 0) {
        gd.setColor(ContextCompat.getColor(application, bgColorId))
    }
    if (borderColorId > 0) {
        gd.setStroke(borderWidth, ContextCompat.getColor(application, borderColorId))
    }
    background = gd
    return this
}