package cn.chawloo.base.model

/**
 * TODO
 * @author Create by 鲁超 on 2022/7/31 17:04
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
data class PushMessage(val event: Event = Event.EmptyMessage) {
    companion object {
        val emptyMessage = PushMessage(event = Event.EmptyMessage)
        val ConnectDevice: PushMessage get() = PushMessage(event = Event.ConnectDevice)
        val ScoConnected: PushMessage get() = PushMessage(event = Event.ScoConnected)
        val OpenBluetooth: PushMessage get() = PushMessage(event = Event.OpenBluetooth)
        val DisconnectDevice: PushMessage get() = PushMessage(event = Event.DisconnectDevice)
    }
}

sealed class Event {
    data object EmptyMessage : Event()
    data object OpenBluetooth : Event()
    data object ConnectDevice : Event()
    data object ScoConnected : Event()
    data object DisconnectDevice : Event()
}
