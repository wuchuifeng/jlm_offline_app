package com.jlm.translator

import android.os.Build
import cn.chawloo.base.base.BaseApplication
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.gif.AnimatedImageDecoder
import coil3.gif.GifDecoder
import coil3.request.allowHardware
import coil3.request.crossfade
import com.jlm.common.net.NetConstants
import com.umeng.analytics.MobclickAgent
import com.umeng.commonsdk.UMConfigure


class App : BaseApplication(), SingletonImageLoader.Factory {
    override fun onCreate() {
        super.onCreate()
        //初始化友盟
        UMConfigure.init(this, "673750078f232a05f1b30700", "Umeng", UMConfigure.DEVICE_TYPE_PHONE, "")
        MobclickAgent.setPageCollectionMode(MobclickAgent.PageMode.AUTO) // 自动采集

        NetConstants.initApi(app = this, debug = false, showLog = true)
    }

    override fun newImageLoader(context: PlatformContext): ImageLoader {
        return ImageLoader.Builder(this)
            .components {
                if (Build.VERSION.SDK_INT >= 28) {
                    add(AnimatedImageDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }
            .crossfade(true)
            .allowHardware(false)
            .build()
    }
}