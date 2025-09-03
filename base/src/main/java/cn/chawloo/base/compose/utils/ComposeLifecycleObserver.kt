package net.cbi360.jst.base.compose.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner

/**
 * TODO
 * @author Create by 鲁超 on 2023/3/8 16:02
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
class ComposeLifecycleObserver : DefaultLifecycleObserver {
    var create: (() -> Unit)? = null
    var start: (() -> Unit)? = null
    private var resume: (() -> Unit)? = null
    var pause: (() -> Unit)? = null
    var stop: (() -> Unit)? = null
    var destroy: (() -> Unit)? = null
    fun onResume(block: () -> Unit) {
        this.resume = block
    }

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        create?.invoke()
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        resume?.invoke()
    }
}

@Composable
fun rememberLifecycle(): ComposeLifecycleObserver {
    val observer = ComposeLifecycleObserver()
    val owner = LocalLifecycleOwner.current
    DisposableEffect(key1 = "lifecycle", effect = {
        owner.lifecycle.addObserver(observer)
        onDispose {
            owner.lifecycle.removeObserver(observer)
        }
    })
    val ctx = LocalContext.current
    return remember(ctx) { observer }
}