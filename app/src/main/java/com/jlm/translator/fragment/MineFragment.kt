package com.jlm.translator.fragment

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.core.content.ContextCompat
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import cn.chawloo.base.ext.doClick
import cn.chawloo.base.ext.gone
import cn.chawloo.base.ext.size
import cn.chawloo.base.ext.sp
import cn.chawloo.base.ext.textString
import cn.chawloo.base.ext.toJson
import cn.chawloo.base.ext.toast
import cn.chawloo.base.ext.visible
import cn.chawloo.base.model.BaseResult
import cn.chawloo.base.pop.showConfirmWindow
import cn.chawloo.base.utils.MK
import cn.chawloo.base.utils.MKKeys
import coil3.load
import coil3.transform.RoundedCornersTransformation
import com.drake.net.Post
import com.drake.net.utils.scopeDialog
import com.drake.net.utils.scopeNetLife
import com.dylanc.viewbinding.binding
import com.jlm.common.compat.BaseLazyFragmentCompat
import com.jlm.common.entity.UserModel
import com.jlm.common.router.Rt
import com.jlm.common.router.goto
import com.jlm.common.router.logout
import com.jlm.common.util.ApiUtil
import com.jlm.translator.R
import com.jlm.translator.databinding.FragmentMineBinding
import com.jlm.translator.entity.GetUserDto
import com.jlm.translator.entity.LogoutDto
import com.jlm.translator.entity.responseModel.DeviceInfoModel
import com.jlm.translator.manager.IntelliVersionManager
import com.jlm.translator.manager.TransDeviceManager
import com.safframework.log.L

class MineFragment : BaseLazyFragmentCompat(R.layout.fragment_mine) {
    val TAG = javaClass.simpleName

    private val vb by binding<FragmentMineBinding>()

    var deviceInfoModel: DeviceInfoModel? = null

    companion object {
        const val unLoginTvName = "未识别到设备"
    }

    override fun onResume() {
        super.onResume()
        deviceInfoModel = TransDeviceManager.getInstance().getDeviceInfoModel()
        updateUi()
//        getUser()
    }

    override fun lazyLoad() {

    }

    override fun initialize() {
        super.initialize()

//        L.d("userModel:${userModel?.toJson()}")
    }

    fun updateUi() {
//        if (!userModel?._id.isNullOrEmpty()) {
//            vb.tvLogin.visible()
//            vb.tvLogin.text = "退出登录"
//            vb.tvName.text = userModel?.nickname?.ifBlank{ "游客" }
//            vb.tvId.visible()
//        } else {
//            vb.tvLogin.gone()
////            vb.tvLogin.text = "登录"
//            vb.tvName.text = unLoginTvName
//            vb.tvId.gone()
//        }

        if (deviceInfoModel != null) {
            vb.tvName.text = deviceInfoModel?.getDeviceFullName()
            vb.tvId.text = "VIP时长：${deviceInfoModel?.getFreeTime()}"
        }

    }

    private fun getUser() {
        scopeNetLife {
            Post<BaseResult<UserModel?>>(ApiUtil.userInfoUrl) {
                json(GetUserDto(userModel?._id).toJson())
            }.await().data?.run {
                MK.encode(MKKeys.KEY_USER, this)
                setUser()
                updateUi()
            }?. run {
                updateUi()
            }
        }.preview {
            userModel?.setUser()
        }
    }

    private fun UserModel.setUser() {
        vb.tvName.text = nickname.ifBlank { unLoginTvName }
//        vb.ivAvatar.load(avatar) {
//
//            transformations(RoundedCornersTransformation(99F))
//            placeholder(R.mipmap.ic_avatar_default)
//            error(R.mipmap.ic_avatar_default)
//        }
        vb.ivAvatar.doClick {
            goto(Rt.PersonInfoAct)
        }
        vb.tvId.visible()
        vb.tvId.text = "等级:${showLevel}"
        vb.tvBalance.text = buildSpannedString {
            color(ContextCompat.getColor(mContext, android.R.color.black)) {
                size(18.sp) {
                    append("${userModel?.balance ?: 0} ")
                }
            }
            color(ContextCompat.getColor(mContext, cn.chawloo.base.R.color.color_99)) {
                size(12.sp) {
//                    append("Fish")
                }
            }
        }
    }

    override fun onClick() {
        super.onClick()
//        vb.tvId.doClick {
//            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
//            val clip = ClipData.newPlainText("jlm_user_id", vb.tvId.textString())
//            clipboard.setPrimaryClip(clip)
//            toast("复制ID成功")
//        }
        // 对话翻译
        vb.tvDialogue.doClick {
            goto(Rt.SpeakerModeAct)
        }
        vb.tvRecharge.doClick {
            goto(Rt.RechargeAct)
        }
        vb.tvMyOrder.doClick {
            goto(Rt.MyOrderAct)
        }
        vb.tvSetting.doClick {
            goto(Rt.SettingAct)
        }

        //文本翻译
        vb.tvTextTranslation.doClick {
            goto(Rt.TextTranslationAct)
        }

        //联系我们
        vb.tvContactUs.doClick {
            goto(Rt.ContactUsAct)
        }
        // 产品解答
        vb.tvProductQues.doClick {
            //goto(Rt.SelectProductByAllEncyclopediaAct)
            goto(Rt.ProductInstructionsAct)
        }
        // 语音配置页面
        vb.tvSpeechConfig.doClick {
            goto(Rt.ListenerModeSettingAct)
        }

        vb.llInfo.doClick {
            goto(Rt.TransDeviceInfoAct)
//            if (userModel?._id.isNullOrEmpty()) {
//                goto(Rt.LoginAct)
//            }
        }

        // 判断是登录还是退出登录
        vb.tvLogin.doClick {
            if (userModel?._id.isNullOrEmpty()) {
                goto(Rt.LoginAct)
            } else {
                vb.tvLogin.doClick {
                    showConfirmWindow(mContext, "退出登录", "您确定要退出当前账号吗？", leftStr = "取消") {
                        scopeDialog {
                            Post<BaseResult<String?>>("client/auth/logout") {
                                json(LogoutDto(userModel?._id).toJson())
                            }.await()
                            logout()
                        }
                    }
                }
            }
        }
    }
}