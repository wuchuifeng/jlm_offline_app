package com.jlm.translator.act

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import cn.chawloo.base.R
import cn.chawloo.base.ext.backgroundShape
import cn.chawloo.base.ext.doClick
import cn.chawloo.base.ext.gone
import cn.chawloo.base.ext.size
import cn.chawloo.base.ext.sp
import cn.chawloo.base.ext.textString
import cn.chawloo.base.ext.toast
import cn.chawloo.base.ext.visible
import cn.chawloo.base.pop.showSingleWheelViewPopupWindow
import cn.chawloo.base.utils.AlbumTool
import com.drake.brv.utils.linear
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.dylanc.viewbinding.binding
import com.jlm.common.compat.BaseActCompat
import com.jlm.common.pop.ChooseAlbumPop
import com.jlm.common.router.Rt
import com.jlm.common.router.goto
import com.jlm.common.util.PermissionUtils.requestCameraPermission
import com.jlm.common.util.PermissionUtils.requestStoragePermission
import com.jlm.translator.contract.UpdateNicknameContract
import com.jlm.translator.databinding.ActTransdeviceInfoBinding
import com.jlm.translator.databinding.ItemDeviceinfoBinding
import com.jlm.translator.entity.DeviceItemModel
import com.jlm.translator.entity.responseModel.DeviceInfoModel
import com.jlm.translator.manager.TransDeviceManager
import com.luck.picture.lib.config.SelectMimeType
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.manager.PictureCacheManager
import com.therouter.router.Route

@Route(path = Rt.TransDeviceInfoAct)
class TransDeviceInfoAct : BaseActCompat() {
    private val vb by binding<ActTransdeviceInfoBinding>()
    private var deviceInfoModel: DeviceInfoModel? = null
    private var modelList = emptyList<DeviceItemModel>()

    override fun initialize() {
        super.initialize()
        initData()
        vb.ivAvatarEdit.backgroundShape(android.R.color.white, radius = 99F)
        vb.ivAvatar.backgroundShape(android.R.color.white, radius = 99F)
        vb.tvBalance.text = buildSpannedString {
            color(ContextCompat.getColor(this@TransDeviceInfoAct, android.R.color.black)) {
                size(18.sp) {
                    append("${userModel?.balance ?: 0} ")
                }
            }
            color(ContextCompat.getColor(this@TransDeviceInfoAct, R.color.color_99)) {
                size(12.sp) {
                    append("Fish")
                }
            }
        }
//        vb.tvId.text = userModel?._id
//        vb.tvGender.text = gender
//        vb.tvNickname.text = userModel?.nickname
//        vb.tvCompany.text = userModel?.company
//        vb.tvUserLevel.text = userModel?.showLevel

        vb.recycler.linear()
            .setup {
                addType<DeviceItemModel>(com.jlm.translator.R.layout.item_deviceinfo)
                onBind {
                    val binding = getBinding<ItemDeviceinfoBinding>()
                    with(getModel<DeviceItemModel>()){
                        binding.tvTitle.text = title
                        binding.tvContent.text = content
                        if (editAble) {
                            binding.tvContent.setCompoundDrawablesWithIntrinsicBounds(
                                null,
                                null,
                                ContextCompat.getDrawable(
                                    this@TransDeviceInfoAct,
                                    com.jlm.translator.R.drawable.list_right_icon_next
                                ),
                                null
                            )
                        } else {
                            binding.tvContent.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
                        }
                        // 更新内容
//                        updateSettingValue(this, binding)
                    }
                }
                onClick(com.jlm.translator.R.id.tv_content) {
                    with(getModel<DeviceItemModel>()) {
//                        itemClick(this, position)
                    }
                }
            }
        vb.recycler.models = modelList

    }

    fun initData() {
        deviceInfoModel = TransDeviceManager.getInstance().getDeviceInfoModel()
        if (deviceInfoModel != null) {
            val list = mutableListOf<DeviceItemModel>()
            list.add(DeviceItemModel("设备ID", deviceInfoModel?.deviceId, false, 1))
            list.add(DeviceItemModel("SIN码", "${deviceInfoModel?.sin}", false, 2))
            list.add(DeviceItemModel("VIP时长", "${deviceInfoModel?.getFreeTime()}", false, 3))
            list.add(DeviceItemModel("搭配耳机数量", "${deviceInfoModel?.getEarphoneCountInfo()}", false, 12))
            list.add(DeviceItemModel("设备类型", deviceInfoModel?.getDeviceTypeName(), false, 11))
            list.add(DeviceItemModel("设备状态", deviceInfoModel?.getStatusName(), false, 4))
//            list.add(DeviceItemModel("版本", deviceInfoModel?.getVersionName(), false, 5))
            list.add(DeviceItemModel("设备型号", deviceInfoModel?.modelInfo?.name, false, 8))
            list.add(DeviceItemModel("机主信息", deviceInfoModel?.buyer_info, false, 6))
            list.add(DeviceItemModel("机主电话", deviceInfoModel?.buyer_phone, false, 7))
            list.add(DeviceItemModel("服务商", deviceInfoModel?.agencyInfo?.name, false, 9))
            list.add(DeviceItemModel("备注", deviceInfoModel?.remark, false, 10))

            modelList = list
        }
    }



    private val updateNicknameLauncher = registerForActivityResult(UpdateNicknameContract()) {
        it?.run {
//            vb.tvNickname.text = getString(UpdateNicknameAct.KEY_NICKNAME)
        }
    }

    private fun setAvatar() {
        ChooseAlbumPop(this) { requestPermission(it) }.showPopupWindow()
    }

    private fun requestPermission(type: Int) {
        if (type == 1) {
            if (packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
                requestCameraPermission { AlbumTool.takePhoto(this, this::getPhoto) }
            } else {
                toast("未检测到相机")
            }
        }
        if (type == 2) {
            requestStoragePermission { AlbumTool.choosePhoto(this, 1, this::getPhoto) }
        }
    }

    private fun getPhoto(list: List<LocalMedia>) {
        list.forEach { uploadFile(if (it.isCompressed) it.compressPath else it.realPath) }
    }

    private fun uploadFile(path: String) {
        uploadSuccess(path)
    }

    private fun uploadSuccess(uploadPath: String) {
        clearCache()
//        vb.ivAvatar.load(uploadPath) {
//            transformations(RoundedCornersTransformation(99F))
//            crossfade(false)
//            placeholder(com.jlm.translator.R.drawable.mine_head_def02)
//            error(com.jlm.translator.R.drawable.mine_head_def02)
//            fallback(com.jlm.translator.R.drawable.mine_head_def02)
//        }
    }

    private fun clearCache() {
        PictureCacheManager.deleteCacheDirFile(this, SelectMimeType.ofImage())
        PictureCacheManager.deleteAllCacheDirFile(this)
    }

    @SuppressLint("WrongConstant")
    override fun onClick() {
        super.onClick()
        vb.ivAvatar.doClick {
            setAvatar()
        }
        vb.ivAvatarEdit.doClick {
            setAvatar()
        }
//        vb.tvId.doClick {
//            val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
//            val clip = ClipData.newPlainText("jlm_user_id", vb.tvId.textString())
//            clipboard.setPrimaryClip(clip)
//            toast("复制ID成功")
//        }
//        vb.tvNickname.doClick {
//            updateNicknameLauncher.launch(Bundle().apply {
//                putString(UpdateNicknameAct.KEY_NICKNAME, userModel?.nickname)
//            })
//        }
//        vb.tvGender.doClick {
//            val list = listOf("女", "男", "其他")
//            showSingleWheelViewPopupWindow(this@TransDeviceInfoAct, "请选择性别", list, list.indexOf(gender)) { _, result, _ ->
//                gender = result
//                vb.tvGender.text = result
//            }
//        }
        vb.tvToRecharge.doClick {
            goto(Rt.RechargeAct)
        }
    }
}