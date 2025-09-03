package com.jlm.translator.act

import androidx.lifecycle.lifecycleScope
import cn.chawloo.base.base.BUNDLE_NAME
import cn.chawloo.base.ext.bundleExtra
import cn.chawloo.base.ext.doClick
import cn.chawloo.base.ext.if2Gone
import cn.chawloo.base.ext.if2Visible
import cn.chawloo.base.ext.insertClipboard
import cn.chawloo.base.ext.parcelable
import cn.chawloo.base.ext.toast
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
import com.jlm.translator.database.manager.TranslateHistoryDBManager
import com.jlm.translator.database.table.TranslateHistory
import com.jlm.translator.manager.IntelliVersionManager
import com.jlm.translator.manager.IntelligentVersionEnum
import com.jlm.translator.databinding.ActDialogRecordsDetailsBinding
import com.jlm.translator.databinding.ItemDialogueRecordsDetailsBinding
import com.jlm.translator.pop.ChooseOutputTypePop
import com.jlm.translator.utils.TxtUtils.exportTxt
import com.therouter.router.Route
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.joda.time.LocalDateTime

/**翻译记录*/
@Route(path = Rt.DialogueRecordsDetailsAct)
class DialogueRecordsDetailsAct : BaseActCompat() {

    private val vb by binding<ActDialogRecordsDetailsBinding>()

    private var pageIndex = 0
    private var time: String = ""
    private var uuid: String = ""
    override fun initialize() {
        super.initialize()
        initStatusBar()
        initTitle()
        initData()
        initView()
        initListener()
    }

    /***初始化状态栏颜色*/
    private fun initStatusBar() {
        immersionBar {
            statusBarColor(android.R.color.white)
        }
    }

    /***初始化标题*/
    private fun initTitle() {
        val currentVersion = IntelliVersionManager.getInstance(this).getCurVersion()
        vb.tvTitle.text = "详情(${currentVersion.getDisplayName()})"
    }

    private fun initData() {
        val translateHistory = intent.bundleExtra(BUNDLE_NAME).parcelable<TranslateHistory>(TranslateHistory.KEY_TRANSLATE)
        uuid = translateHistory?.UUID ?: ""
        time = translateHistory?.createTime?.toString("yyyy-MM-dd HH:mm:ss") ?: LocalDateTime.now().toString("yyyy-MM-dd HH:mm:ss")
        vb.tvTitle.text = translateHistory?.createTime?.toString("yyyy/MM/dd")
    }

    /***初始化视图*/
    private fun initView() {
        //隐藏标记按钮
        vb.includeTranslationRecordsBottomOperation.tvIncludeMark.if2Gone(true)
        //初始化列表数据
        vb.rvTranslationRecord.linear()
            .dividerSpace(20)
            .setup {
                addType<TranslateHistory>(R.layout.item_dialogue_records_details)
                onBind {
                    with(getBinding<ItemDialogueRecordsDetailsBinding>()) {
                        with(getModel<TranslateHistory>()) {
                            tvContent.text = sourceText
                            tvTranslationContent.text = targetText
                            imgSelector.if2Visible(toggleMode)
                            //如果当前是选择模式显示图标
                            if (toggleMode) {
                                imgSelector.isSelected = isChecked
                            }
                        }
                    }
                }

                onLongClick(R.id.item_view) {
                    if (!toggleMode) {
                        toggle()
                        setChecked(layoutPosition, true)
                    }
                }

                //选择图标和item被点击时只有在选择模式下才可以触发事件操作
                onFastClick(R.id.item_view) {
                    //如果当前未处于选择模式下则不处理点击
                    if (!toggleMode && it == R.id.item_view) {
                        return@onFastClick
                    }
                    setChecked(layoutPosition, !getModel<TranslateHistory>().isChecked)
                }

                //item被点击
                onClick(R.id.item_view) {
                    //如果在选择模式下，则更改选中状态，反之则进入详情页
                    if (toggleMode) {
                        setChecked(layoutPosition, !getModel<TranslateHistory>().isChecked)
                    }
                }

                //监听列表选中
                onChecked { position, isChecked, allChecked ->
                    val model = getModel<TranslateHistory>(position)
                    model.isChecked = isChecked
                    notifyItemChanged(position)
                    vb.ivAction.isSelected = allChecked
                }
                //监听切换模式
                onToggle { _, _, _ ->
                    // 刷新列表显示选择按钮
                    notifyDataSetChanged()
                    changeListEditable(this)
                }
            }
        vb.ivBack.setImageResource(R.drawable.tools_nav_icon_back_black_def)
        vb.ivAction.setImageResource(R.drawable.tools_nav_icon_menu)

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


    /***初始化数据*/
    private fun loadData() {
        val dataList = TranslateHistoryDBManager.getTranslateHistoryListByUUID(uuid, pageIndex = pageIndex)
        vb.refresh.addData(dataList) {
            dataList.size == 20
        }
    }

    /***初始化事件监听*/
    private fun initListener() {
        //返回或取消选择模式
        vb.ivBack.doClick {
            if (vb.rvTranslationRecord.bindingAdapter.toggleMode) {//选择模式下，取消选择
                vb.rvTranslationRecord.bindingAdapter.toggle()
            } else {
                finish()
            }
        }

        //右侧菜单或全选
        vb.ivAction.doClick {
            if (vb.rvTranslationRecord.bindingAdapter.toggleMode) {//选择模式下，全选与反选
                vb.rvTranslationRecord.bindingAdapter.checkedAll(!vb.rvTranslationRecord.bindingAdapter.isCheckedAll())
            } else {
                vb.rvTranslationRecord.bindingAdapter.toggle()
            }
        }
        //导出
        vb.includeTranslationRecordsBottomOperation.tvIncludeOutput.doClick {
            if (vb.rvTranslationRecord.bindingAdapter.checkedCount > 0) {
                ChooseOutputTypePop(this@DialogueRecordsDetailsAct) { type ->
                    if (type == 1) {
                        val sb = StringBuilder()
                        sb.append("Time:${time}")
                        sb.append("\n")
                        sb.append("========================")
                        vb.rvTranslationRecord.bindingAdapter.getCheckedModels<TranslateHistory>().forEach { history ->
                            sb.takeIf { it.isNotBlank() }?.append("\n")
                            sb.append(history.sourceText)
                            sb.append("\n")
                            sb.append(history.targetText)
                            sb.append("\n")
                        }
                        sb.toString().insertClipboard(this@DialogueRecordsDetailsAct)
                        toast("已复制到剪切板")
                    } else {
                        val sb = StringBuilder()
                        vb.rvTranslationRecord.bindingAdapter.getCheckedModels<TranslateHistory>().forEach { history ->
                            sb.takeIf { it.isNotBlank() }?.append("\n")
                            sb.append(history.sourceText)
                            sb.append("\n")
                            sb.append(history.targetText)
                            sb.append("\n")
                        }
                        val fileName = "jlm_translator_record_${time.trim()}.txt"
                        exportTxt(fileName, sb.toString())
                    }
                }.showPopupWindow()
            } else {
                toast("请选择导出条目")
            }
        }
        //删除
        vb.includeTranslationRecordsBottomOperation.tvIncludeDel.doClick {
            showConfirmWindow(
                this@DialogueRecordsDetailsAct,
                "提示",
                "您要删除这些记录吗？",
                leftStr = "取消"
            ) {
                val checkedList = vb.rvTranslationRecord.bindingAdapter.getCheckedModels<TranslateHistory>()
                TranslateHistoryDBManager.deleteByIDs(checkedList.map { it.ID })
                vb.refresh.refreshing()
            }
        }
    }

    /** 改变编辑状态 */
    private fun changeListEditable(bindingAdapter: BindingAdapter) {
        val toggleMode = bindingAdapter.toggleMode
        if (toggleMode) {
            vb.ivBack.setImageResource(R.drawable.tools_nav_icon_close_def)
            vb.ivAction.setImageResource(R.drawable.selector_uikit_control_picker)
            vb.ivAction.isSelected = vb.rvTranslationRecord.bindingAdapter.isCheckedAll()
        } else {
            vb.ivBack.setImageResource(R.drawable.tools_nav_icon_back_black_def)
            vb.ivAction.setImageResource(R.drawable.tools_nav_icon_menu)
        }
        // 如果取消管理模式则取消全部已选择
        if (!toggleMode) bindingAdapter.checkedAll(false)
        //是否显示底部操作视图
        vb.includeTranslationRecordsBottomOperation.rootView.if2Visible(toggleMode)
    }

    override fun backPressed() {
        if (vb.rvTranslationRecord.bindingAdapter.toggleMode) {
            vb.rvTranslationRecord.bindingAdapter.toggle()
        } else {
            super.backPressed()
        }
    }
}