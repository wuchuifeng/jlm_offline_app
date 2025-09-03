package cn.chawloo.base.compose.ui.widget.state

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

sealed class PageData {
    data object Success : PageData()
    data class Error(
        val throwable: Throwable? = null,
        val value: Any? = null
    ) : PageData()

    data object Loading : PageData()
    data class Empty(val value: Any? = null) : PageData()
}

class PageState(state: PageData) {
    /** 内部交互的状态 */
    internal var interactionState by mutableStateOf(state)

    /** 供外部获取当前状态 */
    val state: PageData get() = interactionState

    /** 供外部修改当前状态 */
    fun changeState(pageData: PageData) {
        interactionState = pageData
    }

    val isLoading: Boolean
        get() = interactionState is PageData.Loading

    companion object {
        fun loading() = PageData.Loading

        fun <T> success(t: T) = PageData.Success

        fun empty(value: Any? = null) = PageData.Empty(value)

        fun error(throwable: Throwable? = null, exceptionMessage: Any? = null) = PageData.Error(throwable, exceptionMessage)
    }
}

@Composable
fun rememberPageState(state: PageData = PageData.Loading): PageState {
    return rememberSaveable(saver = PageStateSaver) {
        PageState(state)
    }
}

val PageStateSaver = Saver<PageState, String>(
    save = { pageState ->
        when (pageState.state) {
            is PageData.Success -> {
                "Success:${(pageState.state as? PageData.Success)}"
            }
            is PageData.Error -> "Error:${(pageState.state as PageData.Error).throwable?.message ?: ""}"
            is PageData.Empty -> "Empty"
            PageData.Loading -> "Loading"
        }},
    restore = { savedValue ->
        val parts = savedValue.split(":")
        when (parts[0]) {
            "Success" -> {
                PageState(PageData.Success)
            }
            "Error" -> PageState(PageData.Error(Throwable(parts[1])))
            "Empty" -> PageState(PageData.Empty())
            "Loading" -> PageState(PageData.Loading)
            else -> PageState(PageData.Loading)
        }
    }
)

@Composable
fun <T> StateCompose(
    modifier: Modifier = Modifier,
    pageState: PageState = rememberPageState(),
    loading: () -> Unit,
    loadingComponentBlock: @Composable (BoxScope.() -> Unit) = StateComposeConfig.loadingComponent,
    emptyComponentBlock: @Composable (BoxScope.(PageData.Empty) -> Unit) = StateComposeConfig.emptyComponent,
    errorComponentBlock: @Composable (BoxScope.(PageData.Error) -> Unit) = StateComposeConfig.errorComponent,
    contentComponentBlock: @Composable (BoxScope.(PageData.Success) -> Unit)
) {
    Box(modifier = modifier) {
        when (pageState.interactionState) {
            is PageData.Success -> contentComponentBlock(pageState.interactionState as PageData.Success)
            is PageData.Loading -> {
                loadingComponentBlock.invoke(this)
                loading.invoke()
            }

            is PageData.Error -> StateBoxCompose({ pageState.interactionState = PageData.Loading }) {
                errorComponentBlock(this, pageState.interactionState as PageData.Error)
            }

            is PageData.Empty -> emptyComponentBlock(this, pageState.interactionState as PageData.Empty)
        }
    }
}

@Composable
private fun StateBoxCompose(block: () -> Unit, content: @Composable BoxScope.() -> Unit) {
    Box(
        Modifier.clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
            block.invoke()
        },
        content = content
    )
}
