package com.jlm.translator.utils

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.appcompat.app.AppCompatActivity
import cn.chawloo.base.ext.toast
import cn.chawloo.base.pop.showConfirmWindow
import com.jlm.common.util.PermissionUtils.requestStoragePermission
import java.io.File
import java.io.FileOutputStream

object TxtUtils {
    fun AppCompatActivity.exportTxt(fileName: String, content: String) {
        requestStoragePermission {
            if (isExternalStorageWritable()) {
                try {
                    val dir = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "MyTranslatorExport")
                    } else {
                        //Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                        File(Environment.getExternalStorageDirectory(), "jlmCache")
                    }
                    if (!dir.exists()) {
                        dir.mkdirs()
                    }
                    val txtFile = File(dir, fileName)
                    val fos = FileOutputStream(txtFile)
                    fos.write(content.toByteArray())
                    fos.close()
                    showConfirmWindow(this, title = "导出成功", rightStr = "好的", content = "文件已导出至${txtFile.path}") {

                    }
                    toast("导出成功")
                } catch (e: Exception) {
                    e.printStackTrace()
                    toast("导出失败")
                }
            } else {
                try {
                    val dir = File(externalCacheDir, "MyTranslatorExport")
                    if (!dir.exists()) {
                        dir.mkdirs()
                    }
                    val fos = FileOutputStream(File(dir, fileName))
                    fos.write(content.toByteArray())
                    fos.close()
                    toast("导出成功")
                } catch (e: Exception) {
                    e.printStackTrace()
                    toast("导出失败")
                }
            }
        }
    }

    private fun AppCompatActivity.shareFile(file: File) {
        val shareIntent = Intent()
        shareIntent.setAction(Intent.ACTION_SEND)
        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file))
        shareIntent.setType("*/*") // 可以分享任何文件类型
        startActivity(Intent.createChooser(shareIntent, "分享到..."))
    }

    private fun isExternalStorageWritable(): Boolean {
        return Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()
    }
}