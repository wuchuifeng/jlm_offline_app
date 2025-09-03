package com.jlm.translator.entity

import androidx.annotation.DrawableRes
import androidx.annotation.Keep
import com.drake.brv.annotaion.ItemOrientation
import com.drake.brv.item.ItemDrag
import com.jlm.translator.R

@Keep
data class ProblemImage(
    var path: String = "",
    var isAdd: Boolean = false,
    var uploadPath: String = "",
    var name: String = "",
    @DrawableRes val addImg: Int = R.drawable.question_icon_addimg,
    override var itemOrientationDrag: Int = ItemOrientation.ALL
) : ItemDrag