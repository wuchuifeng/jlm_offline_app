package cn.chawloo.base.pop

import android.content.Context
import android.view.Gravity.BOTTOM
import android.view.View
import android.view.animation.AnimationUtils
import cn.chawloo.base.R
import cn.chawloo.base.databinding.SingleWheelPickerBinding
import cn.chawloo.base.ext.content
import cn.chawloo.base.ext.doClick
import cn.chawloo.base.ext.empty
import cn.chawloo.base.ext.loading
import cn.chawloo.base.ext.outOf
import cn.chawloo.base.listener.IWheelEntity
import com.zyyoona7.wheel.WheelView
import com.zyyoona7.wheel.adapter.ArrayWheelAdapter
import com.zyyoona7.wheel.listener.OnItemSelectedListener
import razerdp.basepopup.BasePopupFlag
import razerdp.basepopup.BasePopupWindow

/**
 * 通用单个滚轮选择器
 * @author Create by 鲁超 on 2020/11/20 0020 10:58
 */
class SingleWheelPicker<M>(context: Context?, title: String, dataList: List<M>, private var lastPosition: Int, val picker: (List<M>, M, Int) -> Unit) :
    BasePopupWindow(context) {
    private var result: M? = null
    private var vb: SingleWheelPickerBinding

    init {
        setContentView(R.layout.single_wheel_picker)
        popupGravity = BOTTOM
        showAnimation = AnimationUtils.loadAnimation(context, R.anim.pop_bottom_show)
        dismissAnimation = AnimationUtils.loadAnimation(context, R.anim.pop_bottom_dismiss)
        setOverlayNavigationBar(false)
        setOverlayNavigationBarMode(BasePopupFlag.OVERLAY_MASK)

        vb = SingleWheelPickerBinding.bind(contentView)
        vb.state.loading()
        vb.tvTitle.text = title
        vb.tvConfirm.doClick {
            dismiss()
            result?.run {
                picker.invoke(dataList, this, lastPosition)
            }
        }
        vb.tvCancel.doClick { dismiss() }
        dataList.takeIf { it.isNotEmpty() }?.run {
            vb.firstWheelView.setData(dataList)
            vb.firstWheelView.setTextFormatter {
                if (it is IWheelEntity) {
                    it.getWheelText()
                } else {
                    it.toString()
                }
            }
            //描述文字
            vb.tvDescrib.visibility = View.GONE
            val item = this.get(lastPosition)
            if (item is IWheelEntity) {
                if (!item.getWheelDescribtion().isNullOrEmpty()) {
                    vb.tvDescrib.text = item.getWheelDescribtion()
                    vb.tvDescrib.visibility = View.VISIBLE
                }
            }
            //选中事件
            vb.firstWheelView.setOnItemSelectedListener(object : OnItemSelectedListener {
                override fun onItemSelected(wheelView: WheelView, adapter: ArrayWheelAdapter<*>, position: Int) {
                    result = dataList[position]
                    lastPosition = position

                    vb.tvDescrib.text = if (result is IWheelEntity) {
                        (result as IWheelEntity).getWheelDescribtion()
                    } else {
                        ""
                    }
                }
            })
            lastPosition = lastPosition.outOf(0..dataList.lastIndex)
            result = dataList[lastPosition]
            vb.firstWheelView.setSelectedPosition(lastPosition)
            vb.state.content()
        } ?: run {
            vb.tvConfirm.isEnabled = false
            vb.state.empty()
        }
    }
}