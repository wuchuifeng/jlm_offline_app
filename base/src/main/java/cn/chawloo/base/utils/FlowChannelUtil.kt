package cn.chawloo.base.utils

import cn.chawloo.base.model.Event
import cn.chawloo.base.model.PushMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

object FlowChannelUtil {
    private val sharedFlow = MutableSharedFlow<PushMessage>()

    fun send(coroutineScope: CoroutineScope = MainScope(), message: PushMessage = PushMessage.emptyMessage) {
        coroutineScope.launch {
            sharedFlow.emit(message)
        }
    }

    fun receive(coroutineScope: CoroutineScope = MainScope(), block: (Event) -> Unit) {
        coroutineScope.launch {
            sharedFlow.collect {
                block(it.event)
            }
        }
    }
}