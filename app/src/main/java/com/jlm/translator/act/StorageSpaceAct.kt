package com.jlm.translator.act

import androidx.core.text.buildSpannedString
import androidx.lifecycle.lifecycleScope
import cn.chawloo.base.ext.size
import cn.chawloo.base.ext.sp
import com.dylanc.viewbinding.binding
import com.jlm.common.compat.BaseActCompat
import com.jlm.common.router.Rt
import com.jlm.translator.databinding.ActStorageSpaceBinding
import com.therouter.router.Route
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Route(path = Rt.StorageSpaceAct)
class StorageSpaceAct : BaseActCompat() {
    private val vb by binding<ActStorageSpaceBinding>()
    override fun initialize() {
        super.initialize()
        vb.animationView.playAnimation()
        lifecycleScope.launch {
            delay(2000)
            vb.animationView.pauseAnimation()
            vb.tvStorageSpace.text = buildSpannedString {
                size(20.sp) {
                    append("10.2")
                }
                size(12.sp) {
                    append("MB")
                }
            }
            vb.tvOfflineSize.text = buildSpannedString {
                size(20.sp) {
                    append("0")
                }
                size(12.sp) {
                    append("KB")
                }
            }
            vb.tvHistorySize.text = buildSpannedString {
                size(20.sp) {
                    append("3.4")
                }
                size(12.sp) {
                    append("MB")
                }
            }
            vb.tvChatSize.text = buildSpannedString {
                size(20.sp) {
                    append("0.0")
                }
                size(12.sp) {
                    append("KB")
                }
            }
            vb.tvClearCacheStorage.isEnabled = true
            vb.tvClearHistoryStorage.isEnabled = true
            vb.tvCacheSize.text = buildSpannedString {
                size(20.sp) {
                    append("6.8")
                }
                size(12.sp) {
                    append("KB")
                }
            }
        }
    }
}