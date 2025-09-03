package com.jlm.translator.act

import android.graphics.Typeface
import android.os.Environment
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.Orientation
import cn.chawloo.base.ext.doClick
import cn.chawloo.base.ext.dp
import cn.chawloo.base.ext.gone
import cn.chawloo.base.ext.if2Gone
import cn.chawloo.base.ext.if2Visible
import cn.chawloo.base.ext.insertClipboard
import cn.chawloo.base.ext.textString
import cn.chawloo.base.ext.toast
import cn.chawloo.base.ext.visible
import cn.chawloo.base.pop.showConfirmWindow
import com.drake.brv.BindingAdapter
import com.drake.brv.PageRefreshLayout
import com.drake.brv.listener.ItemDifferCallback
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.divider
import com.drake.brv.utils.grid
import com.drake.brv.utils.linear
import com.drake.brv.utils.setup
import com.drake.net.utils.debounce
import com.drake.net.utils.launchIn
import com.dylanc.viewbinding.binding
import com.gyf.immersionbar.ktx.immersionBar
import com.jlm.common.compat.BaseActCompat
import com.jlm.common.router.Rt
import com.jlm.common.router.goto
import com.jlm.translator.R
//import com.jlm.translator.azure.IntelligentSpeechDelegate
//import com.jlm.translator.database.manager.TranslateHistoryDBManager
import com.jlm.translator.database.table.TranslateHistory
import com.jlm.translator.databinding.ActTranslationRecordsBinding
import com.jlm.translator.databinding.ItemListenerModeBinding
import com.jlm.translator.databinding.ItemRecordNavBinding
import com.jlm.translator.databinding.ItemTranslationRecordsBinding
import com.jlm.translator.databinding.ItemTranslationRecordsGroupBinding
import com.jlm.translator.entity.RecordModel
import com.jlm.translator.entity.TranslateHistoryGroup
import com.jlm.translator.entity.TranslateRecordNavItem
import com.jlm.translator.entity.TranslateRecordNavModel
import com.jlm.translator.manager.IntelliVersionManager
import com.jlm.translator.manager.IntelligentVersionEnum
import com.jlm.translator.manager.TranslateRecordDelegate
import com.jlm.translator.manager.TranslateRecordTypeEnum
import com.jlm.translator.pop.ChooseOutputTypePop
import com.jlm.translator.pop.TranslationModePop
import com.jlm.translator.utils.TxtUtils.exportTxt
import com.safframework.log.L
import com.therouter.router.Route
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import org.joda.time.LocalDateTime

/**翻译记录*/
@Route(path = Rt.TranslationRecordsAct)
class TranslationRecordsAct : BaseActCompat() {
    val TAG: String = javaClass.name

    private val vb by binding<ActTranslationRecordsBinding>()
    
    private lateinit var translateRecordDelegate: TranslateRecordDelegate

    override fun initialize() {
        super.initialize()
        // 初始化翻译记录代理
        translateRecordDelegate = IntelliVersionManager.getInstance(this).getTranslateRecordDelegate()
        initStatusBar()
        initTitle()
        initView()
        initNav()
    }

    /***初始化状态栏颜色*/
    private fun initStatusBar() {
        immersionBar {
            statusBarColor(cn.chawloo.base.R.color.bg_color)
        }
    }

    /***初始化标题*/
    private fun initTitle() {
        val currentVersion = IntelliVersionManager.getInstance(this).getCurVersion()
        vb.tvTitle.text = "翻译记录(${currentVersion.getDisplayName()})"
        
        // 开发功能：长按标题可以管理测试数据（正式发布时可移除）
//        vb.tvTitle.setOnLongClickListener {
//            showTestDataMenu()
//            true
//        }
    }

    /***初始化视图*/
    private fun initView() {
        //初始化列表数据
        vb.rvTranslationRecord.linear()
            .divider {
                setDivider(8.dp)
                includeVisible = false
                onEnabled {
                    itemViewType == R.layout.item_translation_records
                }
            }
            .setup {
                addType<TranslateHistoryGroup>(R.layout.item_translation_records_group)
                addType<TranslateHistory>(R.layout.item_translation_records)
                itemDifferCallback = object : ItemDifferCallback {
                    override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
                        return if (oldItem is TranslateHistory && newItem is TranslateHistory) {
                            oldItem.UUID == newItem.UUID
                        } else {
                            super.areItemsTheSame(oldItem, newItem)
                        }
                    }

                    override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
                        return if (oldItem is TranslateHistory && newItem is TranslateHistory) {
                            oldItem.sourceText == newItem.sourceText
                                    && oldItem.targetText == newItem.targetText
                                    && oldItem.isMarked == newItem.isMarked
                                    && oldItem.targetSecondText == newItem.targetSecondText
                                    && oldItem.createTime == newItem.createTime
                        } else {
                            super.areContentsTheSame(oldItem, newItem)
                        }
                    }

                    override fun getChangePayload(oldItem: Any, newItem: Any) = true
                }
                onBind {
                    getBindingOrNull<ItemTranslationRecordsGroupBinding>()?.run {
                        getModelOrNull<TranslateHistoryGroup>()?.run {
                            tvDateTitle.text = createTime
                        }
                    }
                    getBindingOrNull<ItemTranslationRecordsBinding>()?.run {
                        getModelOrNull<TranslateHistory>()?.run {
                            //根据类型显示不同的UI
//                            if (type > 0 && type <= navIcons.size) {
//                                ivAvatar.setBackgroundResource(navIcons[type-1])
//                                tvName.text = navTitles[type].name
//                            }
                            //显示标题和图标
                            ivAvatar.setBackgroundResource(this.getTypeIcon())
                            tvName.text = this.getTypeTitle()

                            tvContent.text = sourceText
                            tvTranslationContent.text = targetText
                            //判断是否有第二个输出语言
                            if (targetSecondText.isEmpty()) {
                                tvTranslationSecondContent.gone()
                            } else {
                                tvTranslationSecondContent.visible()
                                tvTranslationSecondContent.text = targetSecondText
                            }
                            tvTime.text = createTime.toString("HH:mm")
//                            tvDate.text = createTime.toString("yyyy/MM/dd")
                            //如果当前记录是标记状态，则展示标记且日期颜色为蓝色
                            viewTag.if2Visible(isMarked)
//                            tvDate.isSelected = isMarked
                            //如果当前是选择模式，则显示选择图标，隐藏时间
                            tvTime.if2Gone(toggleMode)
                            imgSelector.if2Visible(toggleMode)
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
                //item被点击
                onClick(R.id.item_view) {
                    //如果在选择模式下，则更改选中状态，反之则进入详情页
                    val positionData = getModel<TranslateHistory>()
                    if (toggleMode) {
                        setChecked(layoutPosition, !positionData.isChecked)
                    } else {
                        if (positionData.type == TranslateHistory.type_listen ||
                            positionData.type == TranslateHistory.type_listen_bilingual ||
                            positionData.type == TranslateHistory.type_listen_freeformulti
                        ) {//翻译详情
                            goto(Rt.TranslationRecordsDetailsAct) {
                                putParcelable(TranslateHistory.KEY_TRANSLATE, positionData)
                            }
                        } else {//短语对话
                            goto(Rt.DialogueRecordsDetailsAct) {
                                putParcelable(TranslateHistory.KEY_TRANSLATE, positionData)
                            }
                        }
                    }
                }
                //监听列表选中
                onChecked { position, isChecked, allChecked ->
                    val model = getModel<TranslateHistory>(position)
                    model.isChecked = isChecked
                    notifyItemChanged(position)
                    vb.ivAction.setImageResource(if (allChecked) R.drawable.uikit_control_picker_sel else R.drawable.uikit_control_picker_selction)
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
        
        // 防止搜索框自动获取焦点，避免页面进入时键盘弹出
        vb.etSearch.isFocusable = false
        vb.etSearch.isFocusableInTouchMode = false
        vb.etSearch.clearFocus()
        // 让根视图获取焦点，避免搜索框自动获取焦点
        vb.root.isFocusable = true
        vb.root.isFocusableInTouchMode = true
        vb.root.requestFocus()
        
        // 当用户点击搜索框时，允许获取焦点
        vb.etSearch.setOnTouchListener { _, _ ->
            vb.etSearch.isFocusable = true
            vb.etSearch.isFocusableInTouchMode = true
            vb.etSearch.requestFocus()
            false
        }
        
        vb.etSearch.debounce(300).distinctUntilChanged().launchIn(this) {
            vb.refresh.refreshing()
        }
        PageRefreshLayout.startIndex = 0
        vb.refresh.onRefresh {
            lifecycleScope.launch {
                delay(300)
                loadData()
            }
        }.refreshing()
        vb.refresh.onLoadMore {
            loadData()
        }
    }

    fun initNav() {
        vb.rvNav.linear(LinearLayoutManager.HORIZONTAL)
            .divider {
                setDivider(10.dp)
            }
            .setup {
                addType<TranslateRecordNavModel>(R.layout.item_record_nav)
                onBind {
                    getBindingOrNull<ItemRecordNavBinding>()?.run {
                        getModelOrNull<TranslateRecordNavModel>()?.run {
                            tvTitle.text = title
                            if (isSelected) {
                                tvTitle.setTextColor(ContextCompat.getColor(context, cn.chawloo.base.R.color.color_main))
                                tvTitle.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD))
                            } else {
                                tvTitle.setTextColor(ContextCompat.getColor(context, R.color.color_text_describ))
                                tvTitle.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL))
                            }
                        }
                    }
                }
                onClick(R.id.tv_title) {
                    with(getModel<TranslateRecordNavModel>()) {
                        // 使用 delegate 设置当前记录类型
                        translateRecordDelegate.setCurrentRecordType(recordType)
                        // 刷新数据
                        vb.refresh.autoRefresh()
                        // 更新UI
                        notifyDataSetChanged()
                    }
                }
            }.setDifferModels(translateRecordDelegate.getNavItems())
    }

    private fun loadData() {
        lifecycleScope.launch {
            val groupList = translateRecordDelegate.getGroupedTranslateHistoryList(
                search = vb.etSearch.textString(),
                pageIndex = vb.refresh.index,
                pageSize = 20
            )
            vb.refresh.addData(groupList) {
                groupList.size == 20
            }
        }
    }

    override fun onClick() {
        super.onClick()
        //返回或取消选择模式
        vb.ivBack.doClick {
            backPressed()
        }

        //模式选择
//        vb.tvTitle.doClick {
//            TranslationModePop(this@TranslationRecordsAct, translationMode) {
//                translationMode = it
//                val modelName = when (it) {
//                    0 -> "全部"
//                    1 -> "同声传译"
//                    2 -> "同声传译(双语输出)"
//                    3 -> "自由对话(双语输出)"
//                    4 -> "双语对话"
//                    else -> "全部"
//                }
//                vb.tvTitle.text = modelName
//                curTranslateRecordType = it
//                vb.refresh.autoRefresh()
//            }.showPopupWindow(vb.clHeadBanner)
//        }

        //右侧菜单或全选
        vb.ivAction.doClick {
            if (vb.rvTranslationRecord.bindingAdapter.toggleMode) {//选择模式下，全选与反选
//                vb.rvTranslationRecord.bindingAdapter.checkedAll(!vb.rvTranslationRecord.bindingAdapter.isCheckedAll())
            } else {
                vb.rvTranslationRecord.bindingAdapter.toggle()
            }
        }

        //搜索
        vb.clSearchLayout.doClick {
            goto(Rt.TranslationRecordsSearchAct)
        }

        //导出
        vb.includeTranslationRecordsBottomOperation.tvIncludeOutput.doClick {
            ChooseOutputTypePop(this@TranslationRecordsAct) { type ->
                if (type == 1) {
                    val sb = StringBuilder()
                    sb.append("\n")
                    sb.append("========================")
                    sb.append("\n")
                    lifecycleScope.launch {
                        val historyList = translateRecordDelegate.getTranslateHistoryByUUIDs(
                            vb.rvTranslationRecord.bindingAdapter.getCheckedModels<TranslateHistory>().map { it.UUID }
                        )
                        val tempSB = historyList.joinToString("\n") { history ->
                            sb.append(history.sourceText)
                            sb.append("\n")
                            sb.append(history.targetText)
                            sb.append("\n")
                        }
                        if (tempSB.isNotBlank()) {
                            sb.append(tempSB)
                        }
                        sb.toString().insertClipboard(this@TranslationRecordsAct)
                        toast("已复制到剪切板")
                        // 导出成功后退出选择模式
                        vb.rvTranslationRecord.bindingAdapter.toggle()
                    }
                } else {
                    lifecycleScope.launch {
                        val sb = StringBuilder()
                        val historyList = translateRecordDelegate.getTranslateHistoryByUUIDs(
                            vb.rvTranslationRecord.bindingAdapter.getCheckedModels<TranslateHistory>().map { it.UUID }
                        )
                        historyList.joinToString("\n") { history ->
                            sb.append(history.sourceText)
                            sb.append("\n")
                            sb.append(history.targetText)
                            sb.append("\n")
                        }
                        val fileName = "translator_${LocalDateTime.now().toString("yyyy_MM_dd_HH:mm")}.txt"
                        exportTxt(fileName, sb.toString())
                        // 导出成功后退出选择模式
                        vb.rvTranslationRecord.bindingAdapter.toggle()
                    }
                }
            }.showPopupWindow()
        }

        //标记
        vb.includeTranslationRecordsBottomOperation.tvIncludeMark.doClick {
            val checkedList = vb.rvTranslationRecord.bindingAdapter.getCheckedModels<TranslateHistory>()
            translateRecordDelegate.markTranslateHistory(checkedList)
            vb.refresh.refreshing()
            // 标记成功后退出选择模式
            vb.rvTranslationRecord.bindingAdapter.toggle()
        }

        //删除
        vb.includeTranslationRecordsBottomOperation.tvIncludeDel.doClick {
            showConfirmWindow(
                this@TranslationRecordsAct,
                "提示",
                "您要删除这些记录吗？",
                leftStr = "取消"
            ) {
                val checkedList = vb.rvTranslationRecord.bindingAdapter.getCheckedModels<TranslateHistory>()
                translateRecordDelegate.deleteTranslateHistoryByUUIDs(checkedList.map { it.UUID })
                vb.refresh.refreshing()
                // 删除成功后退出选择模式
                vb.rvTranslationRecord.bindingAdapter.toggle()
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
        val toggleMode = vb.rvTranslationRecord.bindingAdapter.toggleMode
        if (toggleMode) {//如果在编辑模式下，顶部左侧Icon为X（取消），顶部右侧Icon为选择
            vb.ivBack.setImageResource(R.drawable.tools_nav_icon_close_def)
            val checkAllIcon =
                if (vb.rvTranslationRecord.bindingAdapter.isCheckedAll()) R.drawable.uikit_control_picker_sel else R.drawable.uikit_control_picker_selction
            vb.ivAction.setImageResource(checkAllIcon)
        } else {
            vb.ivBack.setImageResource(R.drawable.tools_nav_icon_back_black_def)
            vb.ivAction.setImageResource(R.drawable.tools_nav_icon_menu)
        }
    }

    override fun backPressed() {
        if (vb.rvTranslationRecord.bindingAdapter.toggleMode) {
            vb.rvTranslationRecord.bindingAdapter.toggle()
        } else {
            super.backPressed()
        }
    }

    /**
     * 手动生成测试数据（仅用于开发测试）
     */
    private fun generateTestDataManually() {
        lifecycleScope.launch {
            L.d(TAG, "手动生成测试数据")
            translateRecordDelegate.generateTestData()
            // 刷新列表显示
            vb.refresh.autoRefresh()
            toast("测试数据生成完成")
        }
    }

    /**
     * 开发功能：显示测试数据管理菜单（正式发布时可移除）
     */
    private fun showTestDataMenu() {
        val items = arrayOf("生成测试数据", "清除当前版本数据", "取消")
        
        android.app.AlertDialog.Builder(this)
            .setTitle("开发测试功能")
            .setItems(items) { dialog, which ->
                when (which) {
                    0 -> { // 生成测试数据
                        generateTestDataManually()
                    }
                    1 -> { // 清除当前版本数据
                        showConfirmWindow(
                            this@TranslationRecordsAct,
                            "确认清除",
                            "确定要清除当前版本的所有翻译记录吗？",
                            leftStr = "取消"
                        ) {
                            lifecycleScope.launch {
                                // 获取当前版本的所有数据然后删除
                                val allData = translateRecordDelegate.getTranslateHistoryList(pageSize = Int.MAX_VALUE)
                                val uuids = allData.map { it.UUID }.distinct()
                                translateRecordDelegate.deleteTranslateHistoryByUUIDs(uuids)
                                vb.refresh.autoRefresh()
                                toast("当前版本数据已清除")
                            }
                        }
                    }
                    2 -> { // 取消
                        dialog.dismiss()
                    }
                }
            }
            .show()
    }
}