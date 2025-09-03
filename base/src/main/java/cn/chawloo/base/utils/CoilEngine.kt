package cn.chawloo.base.utils

import android.content.Context
import android.widget.ImageView
import cn.chawloo.base.ext.dp
import coil3.load
import coil3.request.placeholder
import coil3.request.transformations
import coil3.transform.RoundedCornersTransformation
import com.luck.picture.lib.engine.ImageEngine

class CoilEngine private constructor() : ImageEngine {
    override fun loadImage(context: Context, url: String, imageView: ImageView) {
        imageView.load(url)
    }

    override fun loadImage(
        context: Context?,
        imageView: ImageView?,
        url: String?,
        maxWidth: Int,
        maxHeight: Int
    ) {
        val width = maxWidth.takeIf { it > 0 } ?: 1
        val height = maxHeight.takeIf { it > 0 } ?: 1
        imageView?.load(url) {
            size(width, height)
            placeholder(com.luck.picture.lib.R.drawable.ps_ic_placeholder)
        }
    }

    override fun loadAlbumCover(context: Context, url: String, imageView: ImageView) {
        imageView.load(url) {
            size(180, 180)
            placeholder(com.luck.picture.lib.R.drawable.ps_ic_placeholder)
            transformations(RoundedCornersTransformation(8F.dp))
        }
    }

    override fun loadGridImage(context: Context, url: String, imageView: ImageView) {
        imageView.load(url) {
            size(200, 200)
            placeholder(com.luck.picture.lib.R.drawable.ps_ic_placeholder)
            transformations(RoundedCornersTransformation(8F.dp))
        }
    }

    override fun pauseRequests(context: Context?) {
    }

    override fun resumeRequests(context: Context?) {
    }

    companion object {
        val instance: CoilEngine by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { CoilEngine() }
    }
}