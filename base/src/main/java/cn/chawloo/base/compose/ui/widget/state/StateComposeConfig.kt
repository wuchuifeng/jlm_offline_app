package cn.chawloo.base.compose.ui.widget.state

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.chawloo.base.R
import cn.chawloo.base.compose.ui.theme.ThemeColor

/**
 * TODO
 * @author Create by 鲁超 on 2023/3/9 17:31
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
object StateComposeConfig {

    internal var loadingComponent: @Composable BoxScope.() -> Unit = {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            CircularProgressIndicator(
                color = ThemeColor,
                strokeWidth = 3.dp,
                modifier = Modifier.size(48.dp)
            )
            Text("加载中...", modifier = Modifier.padding(top = 3.dp))
        }
    }
    internal var emptyComponent: @Composable BoxScope.(PageData.Empty) -> Unit = {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(painter = painterResource(R.mipmap.ic_state_empty), contentDescription = "空白缺省页图标")
            Text(stringResource(R.string.empty_view_hint), color = Color(0xFFA9B7B7), fontSize = 16.sp)
        }
    }
    internal var errorComponent: @Composable BoxScope.(PageData.Error) -> Unit = {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(painter = painterResource(R.mipmap.ic_state_error), contentDescription = "错误缺省页图标")
            Text(stringResource(R.string.error_view_hint), color = Color(0xFFA9B7B7), fontSize = 16.sp)
            Text(
                stringResource(R.string.retry_button_hint),
                modifier = Modifier.padding(top = 10.dp),
                color = ThemeColor,
                fontSize = 16.sp
            )
        }
    }

    fun loadingComponent(component: @Composable BoxScope.() -> Unit = {}) {
        loadingComponent = component
    }

    fun errorComponent(component: @Composable BoxScope.(PageData.Error) -> Unit = {}) {
        errorComponent = component
    }

    fun emptyComponent(component: @Composable BoxScope.(PageData.Empty) -> Unit = { }) {
        emptyComponent = component
    }
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
fun Preview() {
    Box {
        CircularProgressIndicator(
            color = ThemeColor,
            strokeWidth = 2.dp,
            modifier = Modifier
                .size(100.dp)
                .align(Alignment.Center)
        )
    }
}