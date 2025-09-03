package com.jlm.translator.pop

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.view.Gravity
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat
import cn.chawloo.base.ext.doClick
import cn.chawloo.base.ext.toast
import cn.chawloo.base.model.Event
import cn.chawloo.base.utils.FlowChannelUtil
import com.drake.brv.listener.ItemDifferCallback
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.divider
import com.drake.brv.utils.linear
import com.drake.brv.utils.setDifferModels
import com.drake.brv.utils.setup
import com.jlm.translator.R
import com.jlm.translator.bluetooth.BluetoothUtil
import com.jlm.translator.databinding.ItemDeviceConnectBinding
import com.jlm.translator.databinding.PopDeviceConnectionBinding
import razerdp.basepopup.BasePopupFlag
import razerdp.basepopup.BasePopupWindow

@SuppressLint("MissingPermission")
class DeviceConnectionPop(context: Context) : BasePopupWindow(context) {
    private val vb: PopDeviceConnectionBinding

    init {
        setContentView(R.layout.pop_device_connection)
        showAnimation = AnimationUtils.loadAnimation(context, cn.chawloo.base.R.anim.pop_bottom_show)
        dismissAnimation = AnimationUtils.loadAnimation(context, cn.chawloo.base.R.anim.pop_bottom_dismiss)
        popupGravity = Gravity.BOTTOM
        setOverlayNavigationBar(true)
        setOverlayNavigationBarMode(BasePopupFlag.OVERLAY_CONTENT)
        vb = PopDeviceConnectionBinding.bind(contentView)
        //设备列表
        vb.rvDeviceList.linear()
            .divider { setDivider(40) }
            .setup {
                addType<BluetoothDevice>(R.layout.item_device_connect)
                itemDifferCallback = object : ItemDifferCallback {
                    override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
                        return if (oldItem is BluetoothDevice && newItem is BluetoothDevice) {
                            oldItem.name == newItem.name && oldItem.address == newItem.address
                        } else {
                            super.areItemsTheSame(oldItem, newItem)
                        }
                    }

                    override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
                        return if (oldItem is BluetoothDevice && newItem is BluetoothDevice) {
                            oldItem.bondState == newItem.bondState && BluetoothUtil.get().isScoConnected(oldItem) == BluetoothUtil.get().isScoConnected(newItem)
                        } else {
                            super.areContentsTheSame(oldItem, newItem)
                        }
                    }

                    override fun getChangePayload(oldItem: Any, newItem: Any) = true
                }
                onBind {
                    with(getBinding<ItemDeviceConnectBinding>()) {
                        with(getModel<BluetoothDevice>()) {
                            //设备名称
                            tvDeviceName.text = name
                            //连接状态样式
                            if (BluetoothUtil.get().isScoConnected(this)) {//已连接
                                tvDeviceOperate.shapeDrawableBuilder.setStrokeColor(ContextCompat.getColor(context, R.color.color_8c8c8c))
                                    .setStrokeSize(1)
                                    .setSolidColor(ContextCompat.getColor(context, cn.chawloo.base.R.color.white))
                                    .intoBackground()
                                tvDeviceOperate.setTextColor(ContextCompat.getColor(context, R.color.color_8c8c8c))
                                tvDeviceOperate.text = "已连接"
                            } else {//未连接
                                tvDeviceOperate.shapeDrawableBuilder
                                    .setStrokeSize(0)
                                    .setSolidColor(ContextCompat.getColor(context, cn.chawloo.base.R.color.theme_color))
                                    .intoBackground()
                                tvDeviceOperate.setTextColor(ContextCompat.getColor(context, cn.chawloo.base.R.color.white))
                                tvDeviceOperate.text = "连接"
                            }
                        }
                    }
                }

                onClick(R.id.tv_device_operate) {
                    val device = getModel<BluetoothDevice>()
                    if (BluetoothUtil.get().isScoConnected(device)) {
//                        BluetoothUtil.get().disConnected(device)
                    } else {
                        BluetoothUtil.get().connect(device)
                    }
                }
            }
        //遇到问题
        vb.tvProblem.doClick { toast("遇到问题") }

        //确定
        vb.tvConfirm.doClick { dismiss() }

        FlowChannelUtil.receive {
            when (it) {
                is Event.ScoConnected -> {
                    vb.rvDeviceList.bindingAdapter.notifyDataSetChanged()
                }

                else -> {
                }
            }
        }
    }

    override fun showPopupWindow() {
        scan()
        super.showPopupWindow()
    }

    private fun scan() {
        val deviceList = mutableListOf<BluetoothDevice>()
        vb.rvDeviceList.setDifferModels(deviceList)
        BluetoothUtil.get().getBondDevices().takeIf { it.isNotEmpty() }?.forEach {
            if (it.name.contains(BluetoothUtil.JLM_DEVICE_FLAG)) {
                deviceList.add(it)
            }
        }
//        deviceList.addAll(BluetoothUtil.get().getDevices())
    }
}