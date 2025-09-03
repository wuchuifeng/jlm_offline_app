package cn.chawloo.base.pop

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.os.Environment
import android.text.method.ScrollingMovementMethod
import androidx.core.content.FileProvider
import cn.chawloo.base.R
import cn.chawloo.base.databinding.PopUpdateAppBinding
import cn.chawloo.base.ext.browse
import cn.chawloo.base.ext.doClick
import cn.chawloo.base.ext.gone
import cn.chawloo.base.ext.installAPK
import cn.chawloo.base.ext.toast
import cn.chawloo.base.ext.visible
import cn.chawloo.base.model.IUpdateContract
import cn.chawloo.base.utils.DownloadUtils
import com.drake.net.Get
import com.drake.net.component.Progress
import com.drake.net.interfaces.ProgressListener
import com.drake.net.scope.AndroidScope
import com.drake.net.utils.scopeNet
import java.io.File


/**
 * TODO
 * @author Create by 鲁超 on 2022/4/19 0019 11:37:39
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
class UpdateAppPop<T : IUpdateContract>(
    private val context: Context,
    private val info: T,
    private val onFailed: () -> Boolean = { false }
) : Dialog(context, R.style.UpdateDialog) {
    private lateinit var vb: PopUpdateAppBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vb = PopUpdateAppBinding.inflate(layoutInflater)
        setContentView(vb.root)
        window?.setBackgroundDrawableResource(android.R.color.transparent)//去掉白色背景
        if (info.isForceUpdate) {
            setCancelable(false)
            vb.ivUpdatePopClose.gone()
        } else {
            vb.ivUpdatePopClose.visible().doClick {
                downloadJob?.cancel()
                dismiss()
            }
        }
        val versionTitle = String.format("是否升级到%s版本？", info.ver)
        vb.tvUpdateTitle.text = versionTitle
        vb.tvUpdateInfo.movementMethod = ScrollingMovementMethod()
        vb.tvUpdateInfo.text = info.verInfo
        vb.btnManualUpgrade.doClick {
            info.url?.takeIf { it.isNotBlank() }?.run {
                browse(this)
            }
        }
        vb.btnUpgrade.doClick {
            info.url?.takeIf { it.isNotBlank() }?.run {
                try {
                    download(this)
                } catch (e: Exception) {
                    if (!onFailed()) {
                        downloadBySystem(e)
                    }
                }
            } ?: run {
                toast("下载地址为空")
            }
        }
    }

    override fun dismiss() {
        downloadJob?.cancel()
        super.dismiss()
    }

    private var systemDownloadId = -1L
    private fun downloadBySystem(e: Throwable) {
        try {
            e.printStackTrace()
            toast("下载失败，正在尝试使用系统下载器，请稍等")
            info.url?.takeIf { it.isNotBlank() }?.run {
                DownloadUtils.clearCurrentTask(context, systemDownloadId)
                DownloadUtils.downloadBySystem(context, this, "temp.apk", info.verInfo)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            toast("更新失败，请重试")
        }
    }

    private var downloadJob: AndroidScope? = null
    private fun download(downloadUrl: String) {
        val path: String
        if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED || !Environment.isExternalStorageRemovable()) {
            path = try {
                context.externalCacheDir?.absolutePath ?: ""
            } catch (e: Exception) {
                e.printStackTrace()
                ""
            }
            path.ifBlank { Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath }
        } else {
            path = context.cacheDir.absolutePath
        }
        val dir = path + File.separator + "apk"

        downloadJob = scopeNet {
            toast("开始下载...")
            vb.progress.setProgress(0)
            vb.progress.visible()
            vb.llBtnContainer.gone()
            val file = Get<File>(downloadUrl) {
                addDownloadListener(object : ProgressListener() {
                    override fun onProgress(p: Progress) {
                        vb.progress.setProgress(p.progress())
                    }
                })
                setDownloadDir(dir)
            }.await()
            toast("下载完成，开始安装...")
            vb.progress.setProgress(100)
            file.takeIf { it.exists() }?.run {
                vb.progress.gone()
                vb.btnUpgrade.text = "安装"
                vb.btnUpgrade.doClick {
                    install(this@run)
                }
                vb.llBtnContainer.visible()
                install(this)
            }
        }.catch {
            toast(it.message)
            vb.progress.setProgress(0)
            vb.progress.gone()
            vb.btnUpgrade.text = "重试"
            vb.llBtnContainer.visible()
        }
    }

    private fun install(appFile: File) {
        try {
            val fileUri = FileProvider.getUriForFile(context, context.applicationContext.packageName.toString() + ".fileProvider", appFile)
            installAPK(fileUri)
        } catch (e: Exception) {
            if (!onFailed()) {
                downloadBySystem(e)
            }
        }
    }
}