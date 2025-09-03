package cn.chawloo.base.utils

import android.content.Context
import android.net.Uri
import com.luck.picture.lib.basic.PictureSelector
import com.luck.picture.lib.config.PictureMimeType
import com.luck.picture.lib.config.SelectMimeType
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.OnResultCallbackListener
import com.luck.picture.lib.utils.DateUtils
import com.safframework.log.L
import top.zibin.luban.Luban
import top.zibin.luban.OnNewCompressListener
import java.io.File

/**
 * 相册工具
 * @author Create by 鲁超 on 2022/2/7 0007 14:55:59
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
object AlbumTool {
    fun takePhoto(c: Context, getPhoto: (ArrayList<LocalMedia>) -> Unit) {
        PictureSelector.create(c)
            .openCamera(SelectMimeType.ofImage())
            .setCompressEngine { context, source: ArrayList<Uri>, call ->
                Luban.with(context)
                    .load(source)
                    .ignoreBy(100)
                    .filter {
                        PictureMimeType.isUrlHasImage(it) && !PictureMimeType.isHasHttp(it)
                    }
                    .setRenameListener {
                        val indexOf = it.lastIndexOf(".")
                        val postFix = if (indexOf != -1) {
                            it.substring(indexOf)
                        } else {
                            ".jpg"
                        }
                        DateUtils.getCreateFileName("CMP_") + postFix
                    }
                    .setCompressListener(object : OnNewCompressListener {
                        override fun onStart() {

                        }

                        override fun onSuccess(source: String?, compressFile: File?) {
                            call?.onCallback(source, compressFile?.absolutePath)
                        }

                        override fun onError(source: String?, e: Throwable?) {
                            call?.onCallback(source, null)
                        }
                    }).launch()

            }.forResult(object : OnResultCallbackListener<LocalMedia> {
                override fun onResult(result: ArrayList<LocalMedia>?) {
                    result?.run {
                        forEach {
                            it.apply {
                                L.e("压缩::$compressPath")
                                L.e("原图::$path")
                                L.e("裁剪::$cutPath")
                                L.e("是否开启原图::$isOriginal")
                                L.e("原图路径::$originalPath")
                            }
                        }
                        getPhoto(this)
                    }
                }

                override fun onCancel() {}
            })
    }

    fun choosePhoto(c: Context, sum: Int, getPhoto: (ArrayList<LocalMedia>) -> Unit) {
        PictureSelector.create(c)
            .openGallery(SelectMimeType.ofImage())
            .setMaxSelectNum(sum)
            .isDisplayCamera(false)
            .isPreviewImage(true)
            .setImageEngine(CoilEngine.instance)
            .setCompressEngine { context, source: ArrayList<Uri>, call ->
                Luban.with(context)
                    .load(source)
                    .ignoreBy(100)
                    .filter {
                        PictureMimeType.isUrlHasImage(it) && !PictureMimeType.isHasHttp(it)
                    }
                    .setRenameListener {
                        val indexOf: Int = it.lastIndexOf(".")
                        val postfix = if (indexOf != -1) it.substring(indexOf) else ".jpg"
                        DateUtils.getCreateFileName("CMP_") + postfix
                    }
                    .setCompressListener(object : OnNewCompressListener {
                        override fun onStart() {}
                        override fun onSuccess(source: String?, compressFile: File?) {
                            call?.onCallback(source, compressFile?.path)
                        }

                        override fun onError(source: String?, e: Throwable?) {
                            call?.onCallback(source, null)
                        }
                    }).launch()
            }.forResult(object : OnResultCallbackListener<LocalMedia> {
                override fun onResult(result: ArrayList<LocalMedia>?) {
                    result?.run {
                        forEach {
                            it.apply {
                                L.e("压缩::$compressPath")
                                L.e("原图::$path")
                                L.e("裁剪::$cutPath")
                                L.e("是否开启原图::$isOriginal")
                                L.e("原图路径::$originalPath")
                            }
                        }
                        getPhoto(this)
                    }
                }

                override fun onCancel() {}

            })
    }
}