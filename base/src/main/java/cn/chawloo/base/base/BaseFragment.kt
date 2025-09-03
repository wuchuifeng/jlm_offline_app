package cn.chawloo.base.base

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment


/**
 * TODO
 * @author Create by 鲁超 on 2021/3/15 0015 11:09
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
abstract class BaseFragment(@LayoutRes layoutId: Int) : Fragment(layoutId), IBaseView {
    private lateinit var mActivity: BaseAct
    protected lateinit var mContext: Context

    override fun onAttach(context: Context) {
        mActivity = context as BaseAct
        mContext = context
        super.onAttach(mContext)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialize()
    }

    protected abstract fun initialize()

    override fun loginOk() {}
}