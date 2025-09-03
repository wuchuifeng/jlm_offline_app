package cn.chawloo.base.ext

import android.view.View
import cn.chawloo.base.R

internal const val NO_GETTER: String = "Property does not have a getter"

internal fun noGetter(): Nothing = throw NotImplementedError(NO_GETTER)

internal var View.lastClickTime: Long? by viewTags(R.id.tag_last_click_time)
