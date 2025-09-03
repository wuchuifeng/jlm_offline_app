package com.jlm.translator.utils

import android.app.ProgressDialog
import android.content.Context
import android.view.Gravity

/**
 * Loading对话框工具类
 */
class LoadingHelper(private val context: Context) {
    
    private var loadingDialog: ProgressDialog? = null
    
    /**
     * 显示loading
     */
    fun showLoading() {
        try {
            if (loadingDialog == null) {
                loadingDialog = ProgressDialog(context).apply {
                    setMessage("连接中")
                    setCancelable(false)
                }
            }
            loadingDialog?.show()
            
            // 设置固定宽度并居中显示
            loadingDialog?.window?.let { window ->
                val layoutParams = window.attributes
                // 设置固定宽度200dp
                val fixedWidth = (200 * context.resources.displayMetrics.density).toInt()
                layoutParams.width = fixedWidth
                layoutParams.gravity = Gravity.CENTER
                window.attributes = layoutParams
            }
        } catch (e: Exception) {
            // 如果显示Dialog失败，不崩溃
            e.printStackTrace()
        }
    }
    
    /**
     * 隐藏loading
     */
    fun hideLoading() {
        try {
            loadingDialog?.let {
                if (it.isShowing) {
                    it.dismiss()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * 释放资源
     */
    fun release() {
        hideLoading()
        loadingDialog = null
    }
} 