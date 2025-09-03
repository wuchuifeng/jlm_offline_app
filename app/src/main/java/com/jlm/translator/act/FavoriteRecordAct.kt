package com.jlm.translator.act

import android.annotation.SuppressLint
import androidx.lifecycle.lifecycleScope
import cn.chawloo.base.ext.doClick
import cn.chawloo.base.ext.if2Gone
import cn.chawloo.base.ext.if2Visible
import cn.chawloo.base.pop.showConfirmWindow
import com.drake.brv.BindingAdapter
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.dividerSpace
import com.drake.brv.utils.linear
import com.drake.brv.utils.setup
import com.dylanc.viewbinding.binding
import com.gyf.immersionbar.ktx.immersionBar
import com.jlm.common.compat.BaseActCompat
import com.jlm.common.router.Rt
import com.jlm.translator.R
import com.jlm.translator.database.manager.FavoriteRecordDBManager
import com.jlm.translator.database.table.FavoriteRecordModel
import com.jlm.translator.databinding.ActFavoriteRecordsBinding
import com.jlm.translator.databinding.ItemFavoriteRecordBinding
import com.safframework.log.L
import com.therouter.router.Route
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/***收藏记录*/
@Route(path = Rt.FavoriteRecordAct)
class FavoriteRecordAct : BaseActCompat() {
    private val vb by binding<ActFavoriteRecordsBinding>()
//    private lateinit var mAliSynthesisHelper: AliSpeechSynthesisHelper
    private var pageIndex = 0


    override fun initialize() {
        super.initialize()
        initStatusBar()
        initView()
//        mAliSynthesisHelper = AliSpeechSynthesisHelper(this, listener)
        vb.refresh.onRefresh {
            lifecycleScope.launch {
                delay(300)
                loadData()
            }
        }.refreshing()
        vb.refresh.onLoadMore {
            pageIndex++
            loadData()
        }
    }

    /***初始化状态栏颜色*/
    private fun initStatusBar() {
        immersionBar {
            statusBarColor(android.R.color.white)
        }
    }

    /***初始化视图*/
    @SuppressLint("SetTextI18n")
    private fun initView() {
        //隐藏标记按钮
        vb.includeTranslationRecordsBottomOperation.tvIncludeMark.if2Gone(true)
        //隐藏导出按钮
        vb.includeTranslationRecordsBottomOperation.tvIncludeOutput.if2Gone(true)
        //初始化列表
        vb.rvFavoriteRecord.linear()
            .dividerSpace(20)
            .setup {
                addType<FavoriteRecordModel>(R.layout.item_favorite_record)
                onBind {
                    with(getBinding<ItemFavoriteRecordBinding>()) {
                        with(getModel<FavoriteRecordModel>()) {
                            tvTranslationType.text = "$sourceName - $targetName"
                            tvContent.text = content
                            tvTranslationContent.text = translationContent
                            lottieSource.if2Gone(toggleMode)
                            imgSelector.if2Visible(toggleMode)
                            if (toggleMode) {
                                imgSelector.isSelected = isChecked
                            }
                        }
                    }
                }

                onLongClick(R.id.root_view) {
                    if (!toggleMode) {
                        toggle()
                        setChecked(layoutPosition, true)
                    }
                }
                //选择图标和item被点击时只有在选择模式下才可以触发事件操作
                onFastClick(R.id.img_selector, R.id.root_view) {
                    //如果当前未处于选择模式下则不处理点击
                    if (!toggleMode && it == R.id.root_view) {
                        return@onFastClick
                    }
                    var checked = getModel<FavoriteRecordModel>().isChecked
                    checked = !checked
                    setChecked(layoutPosition, checked)
                }
                onClick(R.id.lottie_source) {
                    val model = getModel<FavoriteRecordModel>()
//                    mAliSynthesisHelper.stopTrack()
//                    mAliSynthesisHelper.start(model.translationContent, "becca", model.targetKey)
                }
                //item被点击
                onClick(R.id.root_view) {
                    //如果在选择模式下，则更改选中状态，反之则进入详情页
                    if (toggleMode) {
                        var checked = getModel<FavoriteRecordModel>().isChecked
                        checked = !checked
                        setChecked(layoutPosition, checked)
                    }
                }

                //监听列表选中
                onChecked { position, isChecked, allChecked ->
                    val model = getModel<FavoriteRecordModel>(position)
                    model.isChecked = isChecked
                    notifyItemChanged(position)
                    val icon =
                        if (allChecked) R.drawable.uikit_control_picker_sel else R.drawable.uikit_control_picker_selction
                    vb.ivAction.setImageResource(icon)
                }

                //监听切换模式
                onToggle { _, _, _ ->
                    // 刷新列表显示选择按钮
                    notifyDataSetChanged()
                    changeListEditable(this)
                }
            }
        //重置顶部标题栏Icon
        resetPageTitleIcon()
    }

    /***初始化数据*/
    private fun loadData() {
        val dataList = FavoriteRecordDBManager.getFavoriteRecordList(pageIndex = pageIndex)
        vb.refresh.addData(dataList) {
            dataList.size == 20
        }
    }

    override fun onClick() {
        super.onClick()
        //返回或取消选择模式
        vb.ivBack.doClick {
            if (vb.rvFavoriteRecord.bindingAdapter.toggleMode) {//选择模式下，取消选择
                vb.rvFavoriteRecord.bindingAdapter.toggle()
            } else {
                finish()
            }
        }
        //右侧菜单或全选
        vb.ivAction.doClick {
            if (vb.rvFavoriteRecord.bindingAdapter.toggleMode) {//选择模式下，全选与反选
                vb.rvFavoriteRecord.bindingAdapter.checkedAll(!vb.rvFavoriteRecord.bindingAdapter.isCheckedAll())
            } else {
                vb.rvFavoriteRecord.bindingAdapter.toggle()
            }
        }
        //删除
        vb.includeTranslationRecordsBottomOperation.tvIncludeDel.doClick {
            showConfirmWindow(
                this@FavoriteRecordAct,
                "提示",
                "您要删除这些记录吗？",
                leftStr = "取消"
            ) {
                val checkList = vb.rvFavoriteRecord.bindingAdapter.getCheckedModels<FavoriteRecordModel>()
                FavoriteRecordDBManager.deleteFavoriteRecord(checkList)
                vb.refresh.refreshing()
            }
        }
    }

    /** 改变编辑状态 */
    private fun changeListEditable(bindingAdapter: BindingAdapter) {
        val toggleMode = bindingAdapter.toggleMode
        resetPageTitleIcon()
        // 如果取消管理模式则取消全部已选择
        if (!toggleMode) bindingAdapter.checkedAll(false)
        //是否显示底部操作视图
        vb.includeTranslationRecordsBottomOperation.rootView.if2Visible(toggleMode)
    }

    /** 重置顶部标题栏Icon */
    private fun resetPageTitleIcon() {
        val toggleMode = vb.rvFavoriteRecord.bindingAdapter.toggleMode
        if (toggleMode) {//如果在编辑模式下，顶部左侧Icon为X（取消），顶部右侧Icon为选择
            vb.ivBack.setImageResource(R.drawable.tools_nav_icon_close_def)
            val checkAllIcon =
                if (vb.rvFavoriteRecord.bindingAdapter.isCheckedAll()) R.drawable.uikit_control_picker_sel else R.drawable.uikit_control_picker_selction
            vb.ivAction.setImageResource(checkAllIcon)
        } else {
            vb.ivBack.setImageResource(R.drawable.tools_nav_icon_back_black_def)
            vb.ivAction.setImageResource(R.drawable.tools_nav_icon_menu)
        }
    }

    override fun backPressed() {
        if (vb.rvFavoriteRecord.bindingAdapter.toggleMode) {
            vb.rvFavoriteRecord.bindingAdapter.toggle()
        } else {
            super.backPressed()
        }
    }
}