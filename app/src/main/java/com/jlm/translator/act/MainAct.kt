package com.jlm.translator.act

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import cn.chawloo.base.ext.doClick
import cn.chawloo.base.ext.drawableTopToText
import cn.chawloo.base.ext.toJson
import cn.chawloo.base.ext.toast
import cn.chawloo.base.model.BaseResult
import cn.chawloo.base.utils.MK
import cn.chawloo.base.utils.MKKeys
import com.drake.net.Post
import com.drake.net.utils.scopeNet
import com.drake.net.utils.scopeNetLife
import com.dylanc.viewbinding.binding
import com.dylanc.viewbinding.setCustomView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.gyf.immersionbar.ktx.immersionBar
import com.jlm.common.cache.GlobalManager.getAppConfig
import com.jlm.common.compat.BaseActCompat
import com.jlm.common.entity.UserModel
import com.jlm.common.router.Rt
import com.jlm.common.router.goto
import com.jlm.common.util.ApiUtil
import com.jlm.translator.R
//import com.jlm.translator.ali.AliUtils
import com.jlm.translator.bluetooth.BluetoothUtil
import com.jlm.translator.databinding.ActMainBinding
import com.jlm.translator.databinding.TabMainItemBinding
import com.jlm.translator.entity.GetUserDto
import com.jlm.translator.entity.KeyModel
import com.jlm.translator.entity.Language
import com.jlm.translator.entity.responseModel.DeviceInfoModel
import com.jlm.translator.entity.responseModel.DeviceWhitelistModel
import com.jlm.translator.fragment.AIFragment
import com.jlm.translator.fragment.HomeFragment
import com.jlm.translator.fragment.MineFragment
import com.jlm.translator.intelligent.speech.offline.OfflineManager
import com.jlm.translator.manager.IntelliVersionManager
import com.jlm.translator.manager.TransDeviceManager
import com.jlm.translator.net.HttpRequest
import com.jlm.translator.utils.DeviceUtil
import com.jlm.translator.utils.IntelligentKeyUtil
import com.jlm.translator.utils.LogUtil
import com.jlm.translator.utils.VolumeEnhanceUtil
import com.safframework.log.L
import com.therouter.router.Route

@Route(path = Rt.MainAct)
class MainAct : BaseActCompat() {
    val TAG = javaClass.simpleName
    private val vb by binding<ActMainBinding>()
    private var fragments = ArrayList<Fragment>()
    
    // 音频管理相关
    private lateinit var audioManager: AudioManager
    private var audioFocusRequest: AudioFocusRequest? = null
    private var originalVolume: Int = 0


    companion object {
        val tabSelectIconList = listOf(
            R.mipmap.ic_tab_home,
            R.mipmap.ic_tab_info,
            R.mipmap.ic_tab_my,
//            R.drawable.selector_tab_icon_home,
//            R.drawable.selector_tab_icon_message,
//            R.drawable.selector_tab_icon_mine
        )
        val tabUnSelectIconList = listOf(
            R.mipmap.ic_tab_home1,
            R.mipmap.ic_tab_info1,
            R.mipmap.ic_tab_my1,
        )

        val tabTextList = listOf("首页", "AI", "我的")
    }

    override fun initialize() {
        super.initialize()
        immersionBar {
            statusBarDarkFont(false)
//            transparentStatusBar()
            navigationBarColor(android.R.color.white)
            navigationBarDarkIcon(true)
            statusBarColor(cn.chawloo.base.R.color.color_main)
        }
        
        // 初始化音频管理器
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        
        // 综合音量增强设置
        enhanceAudioVolume()
        
        // 初始化版本配置信息
        IntelliVersionManager.getInstance(this.applicationContext).initConfig()
        TransDeviceManager.getInstance().requestDeviceInfoModel { it ->
            if (it != null && !it.status) { //如果设备被禁用，则提示用户
                showDeviceDisabledDialog("该设备已被禁用，如有需要请联系管理员。15813703532")
            }
        }
        
        // app更新接口
//        HttpRequest.requestAppUpdate{
//            update(this, false)
//        }

        // 获取用户信息
//        getUserInfo()
        initFragments()
        initTabLayout()
        //初始化离线sdk
        OfflineManager.initSdk(this)
    }

    /**
     * 综合音量增强设置
     */
    private fun enhanceAudioVolume() {
        try {
            // 1. 设置系统音量到最大
            setMaxSystemVolume()
            
            // 2. 请求音频焦点
            requestAudioFocus()
            
            // 3. 启用扬声器模式
//            enableSpeakerMode()
            
            // 4. 设置音频会话增强
            configureAudioSession()
            
            // 5. 使用工具类进行额外的音量优化
//            VolumeEnhanceUtil.optimizeAudioRouting(this)

        } catch (e: Exception) {
            L.e(TAG, "音量增强设置失败: ${e.message}")
        }
    }

    /**
     * 1. 设置系统音量到最大
     */
    private fun setMaxSystemVolume() {
        try {
            // 保存原始音量
            originalVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
            
            // 获取各种音频流的最大值并设置
            val streamTypes = listOf(
                AudioManager.STREAM_MUSIC,      // 媒体音量
                AudioManager.STREAM_VOICE_CALL, // 通话音量
                AudioManager.STREAM_SYSTEM,     // 系统音量
                AudioManager.STREAM_ALARM       // 闹钟音量
            )
            
            streamTypes.forEach { streamType ->
                val maxVolume = audioManager.getStreamMaxVolume(streamType)
                audioManager.setStreamVolume(streamType, maxVolume, 0)
                L.d(TAG, "音频流 $streamType 音量已设置到最大: $maxVolume")
            }
            
        } catch (e: Exception) {
            L.e(TAG, "设置系统音量失败: ${e.message}")
        }
    }

    /**
     * 2. 请求音频焦点 - 确保应用音频优先级最高
     */
    private fun requestAudioFocus() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Android 8.0+ 使用 AudioFocusRequest
                val audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()

                audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setAudioAttributes(audioAttributes)
                    .setAcceptsDelayedFocusGain(true)
                    .setOnAudioFocusChangeListener { focusChange ->
                        handleAudioFocusChange(focusChange)
                    }
                    .build()

                val result = audioManager.requestAudioFocus(audioFocusRequest!!)
                L.d(TAG, "音频焦点请求结果: ${if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) "成功" else "失败"}")
                
            } else {
                // Android 8.0以下使用旧API
                val result = audioManager.requestAudioFocus(
                    { focusChange -> handleAudioFocusChange(focusChange) },
                    AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN
                )
                L.d(TAG, "音频焦点请求结果: ${if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) "成功" else "失败"}")
            }
            
        } catch (e: Exception) {
            L.e(TAG, "请求音频焦点失败: ${e.message}")
        }
    }

    /**
     * 处理音频焦点变化
     */
    private fun handleAudioFocusChange(focusChange: Int) {
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN -> {
                L.d(TAG, "获得音频焦点")
                // 重新设置最大音量
                setMaxSystemVolume()
            }
            AudioManager.AUDIOFOCUS_LOSS -> {
                L.d(TAG, "永久失去音频焦点")
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                L.d(TAG, "暂时失去音频焦点")
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                L.d(TAG, "失去音频焦点，但可以降低音量继续播放")
            }
        }
    }

    /**
     * 3. 启用扬声器模式 - 强制使用扬声器而不是听筒
     */
    private fun enableSpeakerMode() {
        try {
            // 启用扬声器
            audioManager.isSpeakerphoneOn = true
            L.d(TAG, "扬声器模式已启用")
            
            // 设置音频模式为正常模式（非通话模式）
            audioManager.mode = AudioManager.MODE_NORMAL
            L.d(TAG, "音频模式设置为正常模式")
            
        } catch (e: Exception) {
            L.e(TAG, "启用扬声器模式失败: ${e.message}")
        }
    }

    /**
     * 4. 配置音频会话增强
     */
    private fun configureAudioSession() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                // 设置音频属性
                val audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)           // 媒体用途
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH) // 语音内容
                    .setFlags(AudioAttributes.FLAG_AUDIBILITY_ENFORCED)  // 强制可听性
                    .build()
                
                L.d(TAG, "音频会话配置完成")
            }
            
            // 启用音量键控制媒体音量
            volumeControlStream = AudioManager.STREAM_MUSIC
            
        } catch (e: Exception) {
            L.e(TAG, "配置音频会话失败: ${e.message}")
        }
    }

    /**
     * 处理硬件音量键 - 确保音量键调节媒体音量
     */
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP -> {
                val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                if (currentVolume < maxVolume) {
                    audioManager.setStreamVolume(
                        AudioManager.STREAM_MUSIC,
                        currentVolume + 1,
                        AudioManager.FLAG_SHOW_UI
                    )
                }
                return true
            }
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                if (currentVolume > 0) {
                    audioManager.setStreamVolume(
                        AudioManager.STREAM_MUSIC,
                        currentVolume - 1,
                        AudioManager.FLAG_SHOW_UI
                    )
                }
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    /**
     * 获取当前音量信息（用于调试）
     */
    private fun logVolumeInfo() {
        try {
            L.d(TAG, VolumeEnhanceUtil.getVolumeInfoString(this))
        } catch (e: Exception) {
            L.e(TAG, "获取音量信息失败: ${e.message}")
        }
    }

    private fun initFragments() {
        fragments.add(HomeFragment())
        fragments.add(AIFragment())
        fragments.add(MineFragment())
    }

    private fun initTabLayout() {
        vb.vpMain.adapter = object : FragmentStateAdapter(this) {

            override fun getItemCount(): Int {
                return fragments.size
            }

            override fun createFragment(position: Int): Fragment {
                return fragments[position]
            }
        }
        vb.vpMain.offscreenPageLimit = 2
        vb.vpMain.isUserInputEnabled = false
        vb.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                tab.setCustomView<TabMainItemBinding> {
                    LogUtil.d("tablayout>>>select", "${tab.position}")
                    tvTab.text = tabTextList[tab.position]
                    ivTab.setBackgroundResource(getTabIcon(tab.position, true))
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                tab.setCustomView<TabMainItemBinding> {
                    LogUtil.d("tablayout>>>unselect", "${tab.position}")
                    tvTab.text = tabTextList[tab.position]
                    ivTab.setBackgroundResource(getTabIcon(tab.position, false))
                }
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {}

        })
        val title = listOf("首页", "信息", "我的")
        val iconList = listOf(
            R.mipmap.ic_tab_home,
            R.mipmap.ic_tab_info,
            R.mipmap.ic_tab_my,
//            R.drawable.selector_tab_icon_home,
//            R.drawable.selector_tab_icon_message,
//            R.drawable.selector_tab_icon_mine
        )
        TabLayoutMediator(vb.tabLayout, vb.vpMain) { tab, position ->
            LogUtil.d("tablayout>>>", "${position}")
            tab.setCustomView<TabMainItemBinding> {
                tvTab.text = tabTextList[position]
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    tvTab.tooltipText = ""
                }
//                tvTab.drawableTopToText(this@MainAct, iconList[position])
                ivTab.setImageResource(getTabIcon(position, position == 0))

            }
        }.attach()
    }

    private fun getTabIcon(position: Int, isSelect: Boolean): Int {
        if (isSelect) {
            return tabSelectIconList[position]
        } else {
          return tabUnSelectIconList[position]
        }
    }

    /**
     * 获取用户信息
     * */
    private fun getUserInfo() {
        scopeNetLife {
            val token = MK.decodeString(MKKeys.KEY_TOKEN)
            L.d("loginMess", token)
            Post<BaseResult<UserModel?>>(ApiUtil.userInfoUrl) {
                json(GetUserDto("${userModel?._id}").toJson())
            }.await().data?.run {
                MK.encode(MKKeys.KEY_USER, this)
            } ?: run {
//                toast("请重新登录")
//                // 清空user信息,跳转到登录页面
//                MK.removeKeys(MKKeys.KEY_USER, MKKeys.KEY_TOKEN)
//                goto(Rt.LoginAct)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // 检查设备白名单
        checkDeviceWhitelist()
        
        // 重新请求音频焦点和设置音量
//        try {
//            requestAudioFocus()
//            setMaxSystemVolume()
//            logVolumeInfo() // 打印当前音量信息
//        } catch (e: Exception) {
//            L.e(TAG, "onResume音频设置失败: ${e.message}")
//        }
    }

    override fun onClick() {
        super.onClick()
        vb.ivFaq.doClick {
            goto(Rt.FaqAct)
        }
    }

    /**
     * 检查设备白名单状态
     */
    private fun checkDeviceWhitelist() {
        val deviceModel = DeviceUtil.getDeviceName() // 获取设备型号
        L.d(TAG, "设备型号: $deviceModel")
        HttpRequest.requestDeviceWhitelist(
            model = deviceModel,
            success = { whitelistModel ->
                if (whitelistModel.isWhitelisted()) {
                    L.d(TAG, "设备型号 ${deviceModel} 在白名单中且已启用")
                    // 设备在白名单中且启用，可以正常使用
                } else {
                    showNotInWhitelistDialog("该设备型号被禁止使用翻译功能，如有需要请联系管理员。15813703532")
                }
            },
            failure = { errorMsg ->
                // 未找到该型号的设备白名单，可以根据业务需求决定是否允许使用
//                toast("设备型号验证失败: $errorMsg")
            },
            notInWhitelist = { errorMsg ->
                L.w(TAG, "设备型号 ${deviceModel} 不在白名单内")
                // 弹出不可取消的dialog
                showNotInWhitelistDialog(errorMsg)
            }
        )
    }
    
    /**
     * 显示设备不在白名单的不可取消dialog
     */
    private fun showNotInWhitelistDialog(message: String) {
        android.app.AlertDialog.Builder(this)
            .setTitle("设备验证失败")
            .setMessage(message)
            .setCancelable(false) // 不可取消
            .setPositiveButton("确定") { dialog, _ ->
                dialog.dismiss()
                // 退出应用
                finishAffinity()
            }
            .show()
    }

    private fun showDeviceDisabledDialog(message: String) {
        android.app.AlertDialog.Builder(this)
            .setTitle("设备被禁用")
            .setMessage(message)
            .setCancelable(false) // 不可取消
            .setPositiveButton("确定") { dialog, _ ->
                dialog.dismiss()
                // 退出应用
                finishAffinity()
            }
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        
        // 释放音频焦点
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                audioFocusRequest?.let { request ->
                    audioManager.abandonAudioFocusRequest(request)
                    L.d(TAG, "音频焦点已释放")
                }
            } else {
                audioManager.abandonAudioFocus { focusChange ->
                    handleAudioFocusChange(focusChange)
                }
                L.d(TAG, "音频焦点已释放")
            }
            
            // 恢复原始音量设置（可选）
            // audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, originalVolume, 0)
            
        } catch (e: Exception) {
            L.e(TAG, "释放音频资源失败: ${e.message}")
        }
        
//        BluetoothUtil.get().stopDiscovery()
    }
}