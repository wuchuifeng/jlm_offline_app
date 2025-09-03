package cn.chawloo.base.utils

import android.app.Activity
import android.content.Context
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.os.Build
import android.provider.Settings
import android.view.View
import androidx.annotation.ChecksSdkIntAtLeast
import java.math.BigInteger
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.*
import javax.crypto.Mac
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

/**
 * TODO
 * @author Create by 鲁超 on 2021/7/15 0015 8:54:34
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
object DeviceUtils {
    fun Context.deviceID(): String {
        val sb = StringBuilder()
        val androidId = this.androidId()
        val serial = getSERIAL()
        val uuid = getDeviceUUID().replace("-", "")
        if (androidId.isNotBlank()) {
            sb.append(androidId)
        }
        if (serial.isNotBlank()) {
            sb.takeIf { it.isNotBlank() }?.append("|")
            sb.append(serial)
        }
        if (uuid.isNotBlank()) {
            sb.takeIf { it.isNotBlank() }?.append("|")
            sb.append(uuid)
        }
        if (sb.isNotBlank()) {
            try {
                val hash = sb.toString().toHash()
                val sha1 = hash.toHex()
                if (sha1.isNotBlank()) {
                    return sha1
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return UUID.randomUUID().toString().replace("-", "")
    }

    /**
     * 获得设备的AndroidId
     *
     * @return 设备的AndroidId
     */
    private fun Context.androidId(): String {
        return try {
            Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        } catch (ex: Exception) {
            ex.printStackTrace()
            ""
        }
    }

    /**
     * 获得设备序列号（如：WTK7N16923005607）, 个别设备无法获取
     *
     * @return 设备序列号
     */
    public fun getSERIAL(): String {
        return try {
            Build.SERIAL
        } catch (ex: Exception) {
            ex.printStackTrace()
            ""
        }
    }

    /**
     * 获得设备硬件uuid
     * 使用硬件信息，计算出一个随机数
     *
     * @return 设备硬件uuid
     */
    private fun getDeviceUUID(): String {
        return try {
            val dev = "3883756" +
                    Build.BOARD.length % 10 +
                    Build.BRAND.length % 10 +
                    Build.DEVICE.length % 10 +
                    Build.HARDWARE.length % 10 +
                    Build.ID.length % 10 +
                    Build.MODEL.length % 10 +
                    Build.PRODUCT.length % 10 +
                    Build.SERIAL.length % 10
            UUID(dev.hashCode().toLong(), Build.SERIAL.hashCode().toLong()).toString()
        } catch (ex: Exception) {
            ex.printStackTrace()
            ""
        }
    }

    /**
     * 取SHA1
     *
     * @return 对应的hash值
     */
    private fun String.toHash(): ByteArray {
        return try {
            val messageDigest = MessageDigest.getInstance("SHA1")
            messageDigest.reset()
            messageDigest.update(toByteArray(StandardCharsets.UTF_8))
            messageDigest.digest()
        } catch (e: Exception) {
            "".toByteArray()
        }
    }

    /**
     * 转16进制字符串
     *
     * @return 16进制字符串
     */
    private fun ByteArray.toHex(): String {
        return BigInteger(1, this).toString(16)
    }

    private const val MAC_NAME = "HmacSHA1"

    /**
     * 使用 HMAC-SHA1 签名方法对encryptText进行签名
     *
     * @param encryptText 被签名的字符串
     * @param encryptKey  密钥
     * @return
     * @throws Exception
     */
    @Throws(Exception::class)
    private fun HmacSHA1Encrypt(encryptText: String, encryptKey: String): ByteArray {
        val data = encryptKey.toByteArray(StandardCharsets.UTF_8)
        //根据给定的字节数组构造一个密钥,第二参数指定一个密钥算法的名称
        val secretKey: SecretKey = SecretKeySpec(data, MAC_NAME)
        //生成一个指定 Mac 算法 的 Mac 对象
        val mac = Mac.getInstance(MAC_NAME)
        //用给定密钥初始化 Mac 对象
        mac.init(secretKey)
        val text = encryptText.toByteArray(StandardCharsets.UTF_8)
        //完成 Mac 操作
        return mac.doFinal(text)
    }

    fun Activity.oneKeyMourn() {
        val paint = Paint()
        val cm = ColorMatrix()
        cm.setSaturation(0F)
        paint.colorFilter = ColorMatrixColorFilter(cm)
        window.decorView.setLayerType(View.LAYER_TYPE_HARDWARE, paint)
    }

    @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.TIRAMISU)
    fun isLatestT(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

    @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    fun isLatest34(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE
}