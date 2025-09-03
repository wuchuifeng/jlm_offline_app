package cn.chawloo.base.pop

import android.content.Context

/**
 * 显示通用确认弹窗
 */
fun showConfirmWindow(
    c: Context,
    title: String? = "温馨提示",
    content: String? = null,
    leftStr: String? = null,
    rightStr: String? = "确定",
    cancel: (() -> Unit)? = null,
    confirm: () -> Unit
): CommonConfirmPopupWindow {
    return CommonConfirmPopupWindow(c, title, content, leftStr, rightStr, cancel, confirm).apply {
        showPopupWindow()
    }
}

/**
 * 显示通用的长文本弹窗
 */
fun showLongContentWindow(c: Context?, title: String, content: String): CommonLongContentPopupWindow {
    return CommonLongContentPopupWindow(c, title, content).apply { showPopupWindow() }
}

/**
 * 显示通用单滚轮选择器
 */
fun <M> showSingleWheelViewPopupWindow(c: Context?, title: String, dataList: List<M>, lastPosition: Int, picker: (List<M>, M, Int) -> Unit): SingleWheelPicker<M> {
    return SingleWheelPicker(c, title, dataList, lastPosition, picker).apply {
        showPopupWindow()
    }
}


fun showBottomListPopup(c: Context, dataList: List<String>, picker: (String) -> Unit) {
    BottomListPickerPopupWindow(c, dataList, picker).showPopupWindow()
}

fun showMiddleListPopup(c: Context, dataList: List<String>, picker: (String) -> Unit): MiddleListPickerPopupWindow {
    return MiddleListPickerPopupWindow(c, dataList, picker).also {
        it.showPopupWindow()
    }
}

fun showListPopup(c: Context, title: String, dataList: List<String>) {
    CommonListPopupWindow(c, title, dataList).showPopupWindow()
}