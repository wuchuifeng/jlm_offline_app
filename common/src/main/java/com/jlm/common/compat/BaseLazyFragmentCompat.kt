package com.jlm.common.compat

import androidx.annotation.LayoutRes
import cn.chawloo.base.base.BaseLazyFragment
import cn.chawloo.base.utils.MK
import cn.chawloo.base.utils.MKKeys
import com.jlm.common.entity.UserModel

abstract class BaseLazyFragmentCompat(@LayoutRes layoutId: Int) : BaseLazyFragment(layoutId) {
    protected val userModel: UserModel?
        get() = MK.decodeParcelable(MKKeys.KEY_USER, UserModel::class.java)
}