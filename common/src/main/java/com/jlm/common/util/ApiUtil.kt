package com.jlm.common.util

/**
 * TODO
 * @author Create by 鲁超 on 2024/4/12 09:42
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
object ApiUtil {
    val userInfoUrl: String = "client/customers/findOne"  // 用户信息api
    val loginUrl: String = "client/auth/login" // 登录api
    val updateUserInfoUrl: String = "client/customers/update" // 更新用户信息api
    val deviceTypeListUrl: String = "client/devices_classifies/list" //设备列表api
    val deviceQsListUrl: String = "client/devices_encyclopedias/list" //设备百科列表api  device_id="xxxxxxxxx"
    val deviceListUrl: String = "client/equipments/list" // 设备列表api  class_id="xxxxxxx"
    val feedbackUrl: String = "client/feedbacks/create" // 用户反馈api


}