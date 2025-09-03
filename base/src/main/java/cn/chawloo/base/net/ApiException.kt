package cn.chawloo.base.net

import java.lang.RuntimeException

/**
 * TODO
 * @author Create by 鲁超 on 2020/10/9 0009 15:02
 */
data class ApiException(override var message: String) : RuntimeException(message)