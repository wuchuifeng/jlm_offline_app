package com.jlm.translator.act

import android.content.Context
import androidx.lifecycle.lifecycleScope
import cn.chawloo.base.ext.appVersionName
import cn.chawloo.base.ext.doClick
import cn.chawloo.base.ext.toJson
import cn.chawloo.base.ext.toast
import cn.chawloo.base.model.BaseResult
import cn.chawloo.base.pop.showConfirmWindow
import cn.chawloo.base.utils.MK
import com.drake.net.Post
import com.drake.net.utils.scopeDialog
import com.dylanc.viewbinding.binding
import com.gyf.immersionbar.ktx.immersionBar
import com.jlm.common.compat.BaseActCompat
import com.jlm.common.router.Rt
import com.jlm.common.router.goto
import com.jlm.common.router.logout
import com.jlm.translator.database.manager.TranslateHistoryDBManager
import com.jlm.translator.databinding.ActSettingBinding
import com.jlm.translator.entity.LogoutDto
import com.luck.picture.lib.config.SelectMimeType
import com.luck.picture.lib.manager.PictureCacheManager
import com.therouter.router.Route
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@Route(path = Rt.SettingAct)
class SettingAct : BaseActCompat() {
    private val vb by binding<ActSettingBinding>()
    override fun initialize() {
        super.initialize()
        immersionBar {
            statusBarColor(android.R.color.white)
            navigationBarColor(android.R.color.transparent)
        }
//        vb.tvVersion.text = "v${appVersionName}"
    }

    override fun onClick() {
        super.onClick()
        vb.stAbout.doClick {
            goto(Rt.AboutUsAct)
        }
//        vb.tvStorageSpace.doClick {
//            goto(Rt.StorageSpaceAct)
//        }
        vb.stFaq.doClick {
            goto(Rt.FaqAct)
        }

        //清空缓存
        vb.stClearcache.doClick {
            showConfirmWindow(this@SettingAct, "清空缓存", "确定要清空翻译记录、语言信息和各种缓存文件吗？", leftStr = "取消") {
                clearAllCache()
            }
        }

        vb.tvLogout.doClick {
            showConfirmWindow(this@SettingAct, "退出登录", "您确定要退出当前账号吗？", leftStr = "取消") {
                scopeDialog {
                    Post<BaseResult<String?>>("client/auth/logout") {
                        json(LogoutDto(userModel?._id).toJson())
                    }.await()
                    logout()
                }
            }
        }
    }

    /**
     * 清空所有缓存
     * 包括：翻译记录、MMKV存储、语言信息、图片缓存、应用程序目录缓存文件
     */
    private fun clearAllCache() {
        lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    // 1. 清空翻译记录数据库
                    clearTranslateHistory()
                    
                    // 2. 清空MMKV存储（保留用户登录信息）
                    clearMMKVCache()
                    
                    // 3. 清空图片缓存
                    clearImageCache()
                    
                    // 4. 清空应用程序目录缓存文件
                    clearAppDirectoryCache()
                }
                
                withContext(Dispatchers.Main) {
                    showCacheCleanCompleteDialog()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    toast("缓存清理失败：${e.message}")
                }
            }
        }
    }

    /**
     * 显示缓存清理完成对话框并强制退出应用
     */
    private fun showCacheCleanCompleteDialog() {
        android.app.AlertDialog.Builder(this)
            .setTitle("缓存清理完成")
            .setMessage("所有缓存数据已清理完成！\n\n为确保应用正常运行，需要重新启动应用。\n\n点击确定将退出应用，请手动重新打开。")
            .setCancelable(false) // 不允许取消
            .setPositiveButton("确定") { dialog, _ ->
                dialog.dismiss()
                // 强制退出应用
                finishAffinity() // 关闭所有Activity
                System.exit(0) // 强制退出进程
            }
            .show()
    }

    /**
     * 清空翻译记录数据库
     */
    private fun clearTranslateHistory() {
        try {
            // 通过数据库实例获取DAO并清空所有记录
            com.jlm.translator.database.JLMDatabase.get().translateHistoryDao().deleteAll()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 清空MMKV缓存（保留重要的用户信息）
     */
    private fun clearMMKVCache() {
        try {
            // 保存重要信息
            val token = MK.decodeString("key_token")
            val user = MK.decodeString("key_user")
            val deviceId = MK.decodeString("key_device_id")
            val isFirstRun = MK.decodeBool("key_is_first_run", true)
            
            // 清空所有MMKV缓存
            MK.clearAll()
            
            // 恢复重要信息
            if (token.isNotEmpty()) {
                MK.encode("key_token", token)
            }
            if (user.isNotEmpty()) {
                MK.encode("key_user", user)
            }
            if (deviceId.isNotEmpty()) {
                MK.encode("key_device_id", deviceId)
            }
            MK.encode("key_is_first_run", isFirstRun)
            
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 清空图片缓存
     */
    private fun clearImageCache() {
        try {
            // 清空图片选择库的缓存
            PictureCacheManager.deleteCacheDirFile(this@SettingAct, SelectMimeType.ofImage())
            PictureCacheManager.deleteAllCacheDirFile(this@SettingAct)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 清空应用程序目录缓存文件
     */
    private fun clearAppDirectoryCache() {
        try {
            // 清理应用内部缓存目录
            val cacheDir = cacheDir
            if (cacheDir.exists()) {
                deleteDirectory(cacheDir)
            }
            
            // 清理应用外部缓存目录
            val externalCacheDir = externalCacheDir
            if (externalCacheDir?.exists() == true) {
                deleteDirectory(externalCacheDir)
            }
            
            // 清理应用数据目录下的临时文件
            val filesDir = filesDir
            val tempFiles = filesDir.listFiles { file -> 
                file.name.contains("temp") || file.name.contains("cache") || file.extension == "tmp"
            }
            tempFiles?.forEach { file ->
                try {
                    if (file.isFile) {
                        file.delete()
                    } else if (file.isDirectory) {
                        deleteDirectory(file)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 递归删除目录及其内容
     */
    private fun deleteDirectory(dir: File): Boolean {
        return try {
            if (dir.exists()) {
                dir.listFiles()?.forEach { file ->
                    if (file.isDirectory) {
                        deleteDirectory(file)
                    } else {
                        file.delete()
                    }
                }
                dir.delete()
            } else {
                true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}