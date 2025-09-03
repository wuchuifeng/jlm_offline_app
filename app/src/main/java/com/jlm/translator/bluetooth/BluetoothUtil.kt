@file:SuppressLint("MissingPermission")

package com.jlm.translator.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothA2dp
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHeadset
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.os.Build
import cn.chawloo.base.ext.application
import cn.chawloo.base.ext.intExtra
import cn.chawloo.base.ext.parcelableExtra
import cn.chawloo.base.ext.toast
import cn.chawloo.base.model.Event
import cn.chawloo.base.model.PushMessage
import cn.chawloo.base.utils.FlowChannelUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


/**
 * TODO
 * @author Create by 鲁超 on 2024/4/10 17:00
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
class BluetoothUtil {
    companion object {
        internal const val JLM_DEVICE_FLAG: String = "Earbuds"
        private val INSTANCE by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { BluetoothUtil() }
        fun get(): BluetoothUtil {
            return INSTANCE
        }
    }

    private var isConnected = false

    internal lateinit var mAudioManager: AudioManager
    private val bluetoothAdapter: BluetoothAdapter? = getBluetoothManager()?.adapter

    internal var bluetoothHeadset: BluetoothHeadset? = null
    internal var bluetoothA2dp: BluetoothA2dp? = null

    private val headsetProfileListener: BluetoothProfile.ServiceListener = object : BluetoothProfile.ServiceListener {

        override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
            if (profile == BluetoothProfile.HEADSET) {
                bluetoothHeadset = proxy as BluetoothHeadset
                println("创建HeadSet服务")
            }
        }

        override fun onServiceDisconnected(profile: Int) {
            if (profile == BluetoothProfile.HEADSET) {
                bluetoothHeadset = null
                FlowChannelUtil.send(message = PushMessage.DisconnectDevice)
            }
        }
    }
    private val a2dpProfileListener: BluetoothProfile.ServiceListener = object : BluetoothProfile.ServiceListener {

        override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
            if (profile == BluetoothProfile.A2DP) {
                bluetoothA2dp = proxy as BluetoothA2dp
                println("创建A2DP服务")
            }
        }

        override fun onServiceDisconnected(profile: Int) {
            if (profile == BluetoothProfile.A2DP) {
                bluetoothA2dp = null
            }
        }
    }

    /**
     * 注册相关，因为安卓12以上权限有区别，所有单独拎出来
     * 包含注册广播接收器、音频服务和音频连接相关
     */
    @SuppressLint("WrongConstant")
    fun register() {
        registerBluetoothReceiver()
        mAudioManager = application.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        mAudioManager.mode = AudioManager.MODE_IN_COMMUNICATION
        bluetoothAdapter?.getProfileProxy(application, headsetProfileListener, BluetoothProfile.HEADSET)
        bluetoothAdapter?.getProfileProxy(application, a2dpProfileListener, BluetoothProfile.A2DP)
    }

    private var mBluetoothSocket: BluetoothSocket? = null

    /**
     * 扫描
     * 1.获取已配对设备 遍历
     * 2.已配对设备不存在需要的设备或没有配对设备清单则进行经典蓝牙搜索
     */
    internal fun scan() {
        getBondDevices().takeIf { it.isNotEmpty() }?.run {
            var hasMatcher = false
            forEach {
                if (it.name.isNullOrBlank()) {
                    return
                }
                //找到配对过的相应的设备 进行连接 并停止遍历
                if (it.name.contains(JLM_DEVICE_FLAG)) {
                    hasMatcher = true
                    if (isConnect(it)) {
                        connectSco()
                    } else {
                        hasMatcher = connect(it)
                        return@forEach
                    }
                }
            }
            //配对清单里不存在已配对的对应设备 则进行搜索
            if (!hasMatcher) {
                scanClassicBluetooth()
            }
        } ?: run {
            scanClassicBluetooth()
        }
    }

    private fun isConnect(device: BluetoothDevice): Boolean {
        val method = BluetoothDevice::class.java.getDeclaredMethod("isConnected")
        method.isAccessible = true
        return method.invoke(device) as Boolean
    }

    internal fun connect(device: BluetoothDevice): Boolean {
        try {
            val connect = BluetoothHeadset::class.java.getDeclaredMethod("connect", BluetoothDevice::class.java)
            connect.isAccessible = true
            return connect.invoke(bluetoothHeadset, device) as Boolean
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    internal fun disConnect(device: BluetoothDevice): Boolean {
        try {
            val connect = BluetoothHeadset::class.java.getDeclaredMethod("disconnect", BluetoothDevice::class.java)
            connect.isAccessible = true
            return connect.invoke(bluetoothHeadset, device) as Boolean
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    /**
     * 扫描搜索经典蓝牙设备
     * 1.如果已经开始搜索了，则先停止
     * 2.开始搜索
     */
    private fun scanClassicBluetooth() {
        if (bluetoothAdapter?.isDiscovering == true) {
            stopDiscovery()
        }
        startDiscovery()
    }

    fun hasJlmDevice(): Boolean {
        // TODO: 手持设备上，默认取消蓝牙判断
        return true //bluetoothHeadset?.connectedDevices?.any { isScoConnected(it) } ?: false
    }

    @SuppressLint("WrongConstant")
    private fun getBluetoothManager(): BluetoothManager? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                application.getSystemService(BluetoothManager::class.java)
            } else {
                application.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 开始发现
     * 1.先清空本地内存保存的列表
     * 2.开始搜索 搜索过程中如果发现新设备 会发送广播 走  ACTION_FOUND
     */
    private fun startDiscovery() {
        bluetoothAdapter?.startDiscovery()
    }

    /**
     * 停止发现
     */
    fun stopDiscovery() {
        if (bluetoothAdapter?.isDiscovering == true) {
            bluetoothAdapter.cancelDiscovery()
        }
    }

    /**
     * 注册广播
     */
    private fun registerBluetoothReceiver() {
        println("注册广播")
        application.registerReceiver(bluetoothReceiver, IntentFilter().apply {
            addAction(BluetoothAdapter.ACTION_STATE_CHANGED) //蓝牙开关状态
            addAction(BluetoothDevice.ACTION_FOUND) //蓝牙发现新设备(未配对的设备)
            addAction(BluetoothDevice.ACTION_PAIRING_REQUEST)//在系统弹出配对框之前(确认/输入配对码)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED) //蓝牙扫描完成
            addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED) //设备配对状态改变
            addAction(BluetoothDevice.ACTION_ACL_CONNECTED) //最底层连接建立
            addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED) //最底层连接断开
            addAction(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED)//BluetoothHeadset连接状态
            addAction(BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED)//BluetoothA2dp连接状态
            addAction(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED) //SCO连接状态更新
        })
    }

    /**
     * 匹配设备
     */
    fun bond(device: BluetoothDevice) {
        val remoteDevice = bluetoothAdapter?.getRemoteDevice(device.address)
        remoteDevice?.run {
            if (remoteDevice.bondState == BluetoothDevice.BOND_BONDED) {
                connectSco()
            } else {
                createBond()
            }
        }
    }

    /**
     * 常规蓝牙连接
     */
    fun connectSco() {
//        bluetoothAdapter?.getProfileProxy(application, headsetProfileListener, BluetoothProfile.HEADSET)
//        bluetoothAdapter?.getProfileProxy(application, headsetProfileListener, BluetoothProfile.A2DP)
        mAudioManager.stopBluetoothSco()
        mAudioManager.startBluetoothSco()
    }

    /**
     * 获取已匹配设备
     */
    internal fun getBondDevices(): List<BluetoothDevice> {
        return bluetoothAdapter?.bondedDevices?.toList() ?: emptyList()
    }

    /**
     * 判断某个设备是否已连接
     */
    internal fun isConnected(device: BluetoothDevice): Boolean {
        return bluetoothHeadset?.isAudioConnected(device) ?: false || bluetoothA2dp?.connectedDevices?.any { it.name == device.name && it.address == device.address } ?: false
    }

    /**
     * 判断某个设备是否已连接，连接SCO为标准
     */
    internal fun isScoConnected(device: BluetoothDevice): Boolean {
        return bluetoothHeadset?.isAudioConnected(device) ?: false
    }

    /**
     * 蓝牙广播接收器
     */
    private val bluetoothReceiver = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            when (action) {
                BluetoothAdapter.ACTION_STATE_CHANGED -> {
                    val state = intent.intExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
                    when (state) {
                        BluetoothAdapter.STATE_OFF -> {
                            println("蓝牙关闭了，唤起开启")
                            FlowChannelUtil.send(message = PushMessage.OpenBluetooth)
                        }

                        BluetoothAdapter.STATE_TURNING_OFF -> {
                            println("蓝牙正在关闭")
                        }

                        BluetoothAdapter.STATE_ON -> {
                            println("蓝牙重新开启了，开始扫描")
                            scan()
                        }

                        BluetoothAdapter.STATE_TURNING_ON -> {
                            println("蓝牙正在开启")
                        }
                    }
                }

                BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED -> {
                    println("设备连接状态变更")
                    val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
                    when (state) {
                        BluetoothAdapter.STATE_CONNECTED -> {}
                        BluetoothAdapter.STATE_DISCONNECTED -> {}
                        BluetoothAdapter.STATE_ON -> {}
                        BluetoothAdapter.STATE_TURNING_ON -> {}
                    }
                }

                BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
                    println("设备绑定状态变更")
                    val bondState = intent.intExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.BOND_NONE)
                    val device = intent.parcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                    if (bondState == BluetoothDevice.BOND_BONDED && device != null && device.name.contains(
                            JLM_DEVICE_FLAG)) {
                        if (device.bondState == BluetoothDevice.BOND_BONDED) {
                            println("蓝牙匹配成功，进行SCO连接")
                            CoroutineScope(Dispatchers.IO).launch {
                                delay(1000)
                                connectSco()
                            }
                        }
                    }
                }

                BluetoothDevice.ACTION_ACL_CONNECTED -> {
                    val device = intent.parcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                    device?.run {
                        connectSco()
                    }
                }

                BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                    FlowChannelUtil.send(message = PushMessage(Event.DisconnectDevice))
                }

                //发现新设备都会进来，需要进一步判断
                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice? = intent.parcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    device?.let { d ->
                        val deviceName = device.name
                        if (deviceName.isNullOrBlank()) {
                            return@let
                        }
                        println("发现蓝牙${deviceName}::${device.address}::${device.uuids}")
                        if (deviceName.contains(JLM_DEVICE_FLAG)) {
                            println("发现所需要的特定设备【${JLM_DEVICE_FLAG}】，开始连接")
                            //停止搜索
                            stopDiscovery()
                            //开始连接
                            bond(d)
                        }
                    }
                }

                BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED -> {
                    val state = intent.intExtra(BluetoothHeadset.EXTRA_STATE, BluetoothHeadset.STATE_DISCONNECTED)
                    val device: BluetoothDevice? = intent.parcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    when (state) {
                        BluetoothHeadset.STATE_CONNECTED -> {
                            println("Headset:::已连接")
                            device?.let { d ->
                                val deviceName = d.name
                                if (deviceName.isNullOrBlank()) {
                                    return@let
                                }
                                if (deviceName.contains(JLM_DEVICE_FLAG)) {
                                    connectSco()
                                }
                            }

                        }

                        BluetoothHeadset.STATE_DISCONNECTED -> {
                            println("Headset:::断开连接")
                        }

                        BluetoothHeadset.STATE_AUDIO_CONNECTED -> {
                            println("Headset:::STATE_AUDIO_CONNECTED")
                        }

                        BluetoothHeadset.STATE_AUDIO_DISCONNECTED -> {
                            println("Headset:::STATE_AUDIO_DISCONNECTED")
                        }
                    }
                }

                BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED -> {
                    val state = intent.intExtra(BluetoothHeadset.EXTRA_STATE, BluetoothHeadset.STATE_DISCONNECTED)
                    when (state) {
                        BluetoothA2dp.STATE_CONNECTED -> {
                            println("A2DP:::已连接")
                        }

                        BluetoothA2dp.STATE_DISCONNECTED -> {
                            println("A2DP:::断开连接")
                        }

                        BluetoothA2dp.STATE_PLAYING -> {
                            println("A2DP:::PLAYING")
                        }

                        BluetoothA2dp.STATE_NOT_PLAYING -> {
                            println("A2DP:::STATE_NOT_PLAYING")
                        }
                    }
                }

                AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED -> {
                    val state = intent.intExtra(AudioManager.EXTRA_SCO_AUDIO_STATE, AudioManager.SCO_AUDIO_STATE_DISCONNECTED)
                    when (state) {
                        AudioManager.SCO_AUDIO_STATE_CONNECTED -> {
                            toast("SCO连接成功")
                            isConnected = true
                            mAudioManager.isBluetoothScoOn = true
                            FlowChannelUtil.send(message = PushMessage.ScoConnected)
                        }

                        AudioManager.SCO_AUDIO_STATE_DISCONNECTED -> {
                            if (isConnected) {
                                isConnected = false
                                toast("SCO断开连接，请使用手机讲话")
                            }
                        }

                        AudioManager.SCO_AUDIO_STATE_CONNECTING -> {
                            toast("正在进行sco连接")
                        }

                        AudioManager.SCO_AUDIO_STATE_ERROR -> {
                            toast("sco连接失败，请使用手机讲话")
                        }
                    }
                }
            }
        }
    }
}