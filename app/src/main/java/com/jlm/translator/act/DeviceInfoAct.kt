package com.jlm.translator.act

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.chawloo.base.compose.ui.theme.AppTheme
import cn.chawloo.base.compose.ui.theme.BgColor
import cn.chawloo.base.compose.ui.theme.Color99
import cn.chawloo.base.compose.ui.theme.ThemeColor
import cn.chawloo.base.compose.ui.widget.TopBar
import cn.chawloo.base.ext.toast
import com.jlm.common.compat.BaseActCompat
import com.jlm.common.router.Rt
import com.therouter.router.Route
import com.jlm.translator.R

@Route(path = Rt.DeviceInfoAct)
class DeviceInfoAct : BaseActCompat() {
    @SuppressLint("WrongConstant")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.White.toArgb(), Color.White.toArgb()),
            navigationBarStyle = SystemBarStyle.light(BgColor.toArgb(), BgColor.toArgb())
        )
        setContent {
            AppTheme {
                Scaffold(modifier = Modifier.systemBarsPadding(), topBar = { TopBar(title = "耳机中心", onBackClick = this::backPressed) }) { paddingValues ->
                    Column(
                        modifier = Modifier
                            .padding(paddingValues)
                            .background(BgColor)
                            .fillMaxSize()
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(top = 50.dp)
                                .align(Alignment.CenterHorizontally)
                        ) {
                            Image(
                                painterResource(R.drawable.mine_head_def02),
                                contentDescription = ""
                            )
                            Image(
                                painterResource(R.drawable.mine_userid_icon_edit),
                                contentDescription = "",
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                            )
                        }
                        Column(
                            modifier = Modifier
                                .padding(top = 30.dp)
                                .background(Color.White, RoundedCornerShape(20.dp, 20.dp))
                                .fillMaxHeight()
                        ) {
                            Row(modifier = Modifier.padding(horizontal = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "设备ID", //stringResource(R.string.mac_address),
                                    modifier = Modifier
                                        .padding(vertical = 20.dp),
                                    fontSize = 16.sp,
                                    color = Color99
                                )
//                                val macAddress by remember { mutableStateOf(IntelligentManager.getInstance().curSelectEarphoneModel.macAddress) }
                                Text(
                                    text = "xxxxxxxxxxx",//macAddress,
                                    modifier = Modifier
                                        .weight(1F)
                                        .padding(end = 6.dp),
                                    fontSize = 16.sp,
                                    color = ThemeColor,
                                    textAlign = TextAlign.End
                                )
                                Image(
                                    painterResource(R.drawable.mine_userid_icon_copy),
                                    contentDescription = "",
                                    modifier = Modifier
                                        .clickable {
                                            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            val clip = ClipData.newPlainText("jlm_user_id", "xxxxxxxx")
                                            clipboard.setPrimaryClip(clip)
                                            toast("复制设备ID成功")
                                        }
                                )
                            }
                            HorizontalDivider(thickness = 0.5.dp)
                            Row(modifier = Modifier.padding(horizontal = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "设备名称",//stringResource(R.string.nickname),
                                    modifier = Modifier
                                        .padding(vertical = 20.dp),
                                    fontSize = 16.sp,
                                    color = Color99
                                )
//                                val nickname by remember { mutableStateOf(IntelligentManager.getInstance().curSelectEarphoneModel.name) }
                                Text(
                                    text = "xxxx",//nickname,
                                    modifier = Modifier
                                        .weight(1F)
                                        .padding(end = 6.dp),
                                    fontSize = 16.sp,
                                    color = Color.Black,
                                    textAlign = TextAlign.End
                                )
                                Image(painterResource(R.drawable.list_right_icon_next), contentDescription = "")
                            }
                            HorizontalDivider(thickness = 0.5.dp)
                            Row(modifier = Modifier.padding(horizontal = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "型号",//stringResource(R.string.model),
                                    modifier = Modifier
                                        .padding(vertical = 20.dp),
                                    fontSize = 16.sp,
                                    color = Color99
                                )
//                                val model by remember { mutableStateOf(IntelligentManager.getInstance().curSelectEarphoneModel.model?.name ?: "--") }
                                Text(
                                    text = "xxxxx",//model,
                                    modifier = Modifier
                                        .weight(1F)
                                        .padding(end = 6.dp),
                                    fontSize = 16.sp,
                                    color = Color.Black,
                                    textAlign = TextAlign.End
                                )
                            }
                            HorizontalDivider(thickness = 0.5.dp)
                            Row(modifier = Modifier.padding(horizontal = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "等级", //stringResource(R.string.level),
                                    modifier = Modifier
                                        .padding(vertical = 20.dp),
                                    fontSize = 16.sp,
                                    color = Color99
                                )
//                                val version by remember { mutableStateOf(IntelligentManager.getInstance().curSelectEarphoneModel.getVersionName()) }
                                Text(
                                    text = "VIP",//version,
                                    modifier = Modifier
                                        .weight(1F)
                                        .padding(end = 6.dp),
                                    fontSize = 16.sp,
                                    color = Color.Black,
                                    textAlign = TextAlign.End
                                )
                            }
                            HorizontalDivider(thickness = 0.5.dp)
                            Row(modifier = Modifier.padding(horizontal = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "免费时长",//stringResource(R.string.free_time),
                                    modifier = Modifier
                                        .padding(vertical = 20.dp),
                                    fontSize = 16.sp,
                                    color = Color99
                                )
//                                val freeTime by remember { mutableStateOf(IntelligentManager.getInstance().curSelectEarphoneModel.getClearFreeTime()) }
                                Text(
                                    text = "10080",//freeTime,
                                    modifier = Modifier
                                        .weight(1F)
                                        .padding(end = 6.dp),
                                    fontSize = 16.sp,
                                    color = Color.Black,
                                    textAlign = TextAlign.End
                                )
                            }
                            HorizontalDivider(thickness = 0.5.dp)
                            Row(modifier = Modifier.padding(horizontal = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "SIN码", //stringResource(R.string.sin),
                                    modifier = Modifier
                                        .padding(vertical = 20.dp),
                                    fontSize = 16.sp,
                                    color = Color99
                                )
//                                val sin by remember { mutableStateOf(IntelligentManager.getInstance().curSelectEarphoneModel.sin.toString()) }
                                Text(
                                    text = "678657",//sin,
                                    modifier = Modifier
                                        .weight(1F)
                                        .padding(end = 6.dp),
                                    fontSize = 16.sp,
                                    color = Color.Black,
                                    textAlign = TextAlign.End
                                )
                            }
                            HorizontalDivider(thickness = 0.5.dp)
                            Row(modifier = Modifier.padding(horizontal = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "供应商", //stringResource(R.string.supplier),
                                    modifier = Modifier
                                        .padding(vertical = 20.dp),
                                    fontSize = 16.sp,
                                    color = Color99
                                )
//                                val supplier by remember { mutableStateOf(IntelligentManager.getInstance().curSelectEarphoneModel.agency?.name ?: "--") }
                                Text(
                                    text = "极力米", //supplier,
                                    modifier = Modifier
                                        .weight(1F)
                                        .padding(end = 6.dp),
                                    fontSize = 16.sp,
                                    color = Color.Black,
                                    textAlign = TextAlign.End
                                )
                            }
                            HorizontalDivider(thickness = 0.5.dp)
                            Row(modifier = Modifier.padding(horizontal = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "供应商", //stringResource(R.string.supplier),
                                    modifier = Modifier
                                        .padding(vertical = 20.dp),
                                    fontSize = 16.sp,
                                    color = Color99
                                )
//                                val supplier by remember { mutableStateOf(IntelligentManager.getInstance().curSelectEarphoneModel.agency?.name ?: "--") }
                                Text(
                                    text = "极力米", //supplier,
                                    modifier = Modifier
                                        .weight(1F)
                                        .padding(end = 6.dp),
                                    fontSize = 16.sp,
                                    color = Color.Black,
                                    textAlign = TextAlign.End
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}