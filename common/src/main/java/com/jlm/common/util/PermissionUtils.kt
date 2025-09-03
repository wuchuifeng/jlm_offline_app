package com.jlm.common.util

import android.Manifest
import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import cn.chawloo.base.ext.toast
import cn.chawloo.base.pop.showConfirmWindow
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.OnPermissionInterceptor
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.jlm.common.pop.PermissionDescriptionPop
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * TODO
 * @author Create by 鲁超 on 2023/1/3 15:29
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
object PermissionUtils {
    fun AppCompatActivity.requestRecordAudioPermission(success: () -> Unit) {
        var mRequestFlag: Boolean
        val permissionPop = PermissionDescriptionPop(this, PermissionDescriptionPop.PermissionEnum.RECORD_AUDIO)
        XXPermissions
            .with(this)
            .permission(Permission.RECORD_AUDIO)
            .interceptor(object : OnPermissionInterceptor {
                override fun launchPermissionRequest(activity: Activity, allPermissions: MutableList<String>, callback: OnPermissionCallback?) {
                    mRequestFlag = true
                    super.launchPermissionRequest(activity, allPermissions, callback)
                    lifecycleScope.launch {
                        delay(300)
                        if (mRequestFlag) {
                            permissionPop.showPopupWindow()
                        }
                    }
                }

                override fun grantedPermissionRequest(
                    activity: Activity,
                    allPermissions: MutableList<String>,
                    grantedPermissions: MutableList<String>,
                    all: Boolean,
                    callback: OnPermissionCallback?
                ) {
                    super.grantedPermissionRequest(activity, allPermissions, grantedPermissions, all, callback)
                }

                override fun deniedPermissionRequest(
                    activity: Activity,
                    allPermissions: MutableList<String>,
                    deniedPermissions: MutableList<String>,
                    never: Boolean,
                    callback: OnPermissionCallback?
                ) {
                    super.deniedPermissionRequest(activity, allPermissions, deniedPermissions, never, callback)
                    if (never) {
                        showConfirmWindow(
                            this@requestRecordAudioPermission,
                            content = "您已禁止授予极力米 麦克风 权限，可能会造成功能不可用，如需使用请到设置授予相应权限",
                            leftStr = "取消",
                            rightStr = "前往设置"
                        ) {
                            XXPermissions.startPermissionActivity(this@requestRecordAudioPermission, Manifest.permission.RECORD_AUDIO)
                        }
                    } else {
                        toast("您已禁止授予极力米 麦克风 权限")
                    }
                }

                override fun finishPermissionRequest(activity: Activity, allPermissions: MutableList<String>, skipRequest: Boolean, callback: OnPermissionCallback?) {
                    super.finishPermissionRequest(activity, allPermissions, skipRequest, callback)
                    mRequestFlag = false
                    lifecycleScope.launch {
                        delay(300)
                        permissionPop.dismiss()
                    }
                }
            })
            .request { _, all -> if (all) success() }
    }

    fun AppCompatActivity.requestCameraPermission(success: () -> Unit) {
        var mRequestFlag: Boolean
        val permissionPop = PermissionDescriptionPop(this, PermissionDescriptionPop.PermissionEnum.CAMERA)
        XXPermissions
            .with(this)
            .permission(Permission.CAMERA)
            .interceptor(object : OnPermissionInterceptor {
                override fun launchPermissionRequest(activity: Activity, allPermissions: MutableList<String>, callback: OnPermissionCallback?) {
                    mRequestFlag = true
                    super.launchPermissionRequest(activity, allPermissions, callback)
                    lifecycleScope.launch {
                        delay(300)
                        if (mRequestFlag) {
                            permissionPop.showPopupWindow()
                        }
                    }
                }

                override fun grantedPermissionRequest(
                    activity: Activity,
                    allPermissions: MutableList<String>,
                    grantedPermissions: MutableList<String>,
                    all: Boolean,
                    callback: OnPermissionCallback?
                ) {
                    super.grantedPermissionRequest(activity, allPermissions, grantedPermissions, all, callback)
                }

                override fun deniedPermissionRequest(
                    activity: Activity,
                    allPermissions: MutableList<String>,
                    deniedPermissions: MutableList<String>,
                    never: Boolean,
                    callback: OnPermissionCallback?
                ) {
                    super.deniedPermissionRequest(activity, allPermissions, deniedPermissions, never, callback)
                    if (never) {
                        showConfirmWindow(
                            this@requestCameraPermission,
                            content = "您已禁止授予极力米 相机 权限，可能会造成功能不可用，如需使用请到设置授予相应权限",
                            leftStr = "取消",
                            rightStr = "前往设置"
                        ) {
                            XXPermissions.startPermissionActivity(this@requestCameraPermission, Manifest.permission.CAMERA)
                        }
                    } else {
                        toast("您已禁止授予极力米 相机 权限")
                    }
                }

                override fun finishPermissionRequest(activity: Activity, allPermissions: MutableList<String>, skipRequest: Boolean, callback: OnPermissionCallback?) {
                    super.finishPermissionRequest(activity, allPermissions, skipRequest, callback)
                    mRequestFlag = false
                    lifecycleScope.launch {
                        delay(300)
                        permissionPop.dismiss()
                    }
                }
            })
            .request { _, all -> if (all) success() }
    }

    fun AppCompatActivity.requestStoragePermission(success: () -> Unit) {
        var mRequestFlag: Boolean
        val permissionPop = PermissionDescriptionPop(this, PermissionDescriptionPop.PermissionEnum.STORAGE)
        XXPermissions
            .with(this)
            .permission(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_AUDIO,
                Manifest.permission.READ_MEDIA_VIDEO
            )
            .interceptor(object : OnPermissionInterceptor {
                override fun launchPermissionRequest(activity: Activity, allPermissions: MutableList<String>, callback: OnPermissionCallback?) {
                    mRequestFlag = true
                    super.launchPermissionRequest(activity, allPermissions, callback)
                    lifecycleScope.launch {
                        delay(300)
                        if (mRequestFlag) {
                            permissionPop.showPopupWindow()
                        }
                    }
                }

                override fun grantedPermissionRequest(
                    activity: Activity,
                    allPermissions: MutableList<String>,
                    grantedPermissions: MutableList<String>,
                    all: Boolean,
                    callback: OnPermissionCallback?
                ) {
                    super.grantedPermissionRequest(activity, allPermissions, grantedPermissions, all, callback)
                }

                override fun deniedPermissionRequest(
                    activity: Activity,
                    allPermissions: MutableList<String>,
                    deniedPermissions: MutableList<String>,
                    never: Boolean,
                    callback: OnPermissionCallback?
                ) {
                    super.deniedPermissionRequest(activity, allPermissions, deniedPermissions, never, callback)
                    if (never) {
                        showConfirmWindow(
                            this@requestStoragePermission,
                            content = "您已禁止授予极力米 存储 权限，可能会造成功能不可用，如需使用请到设置授予相应权限",
                            leftStr = "取消",
                            rightStr = "前往设置"
                        ) {
                            XXPermissions.startPermissionActivity(
                                this@requestStoragePermission,
                                Manifest.permission.READ_MEDIA_IMAGES,
                                Manifest.permission.READ_MEDIA_AUDIO,
                                Manifest.permission.READ_MEDIA_VIDEO
                            )
                        }
                    } else {
                        toast("您已禁止授予极力米 存储 权限")
                    }
                }

                override fun finishPermissionRequest(activity: Activity, allPermissions: MutableList<String>, skipRequest: Boolean, callback: OnPermissionCallback?) {
                    super.finishPermissionRequest(activity, allPermissions, skipRequest, callback)
                    mRequestFlag = false
                    lifecycleScope.launch {
                        delay(300)
                        permissionPop.dismiss()
                    }
                }
            })
            .request { _, all -> if (all) success() }
    }
}