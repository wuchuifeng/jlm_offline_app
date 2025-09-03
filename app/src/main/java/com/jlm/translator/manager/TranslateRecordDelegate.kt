package com.jlm.translator.manager

import android.content.Context
import com.jlm.translator.database.manager.TranslateHistoryDBManager
import com.jlm.translator.database.table.TranslateHistory
import com.jlm.translator.entity.TranslateHistoryGroup
import com.jlm.translator.entity.TranslateRecordNavModel
import com.safframework.log.L
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.joda.time.LocalDateTime
import java.util.UUID

/**
 * 翻译记录管理代理类
 * 统一处理翻译记录的增删改查和版本过滤
 */
class TranslateRecordDelegate(val context: Context, val versionEnum: IntelligentVersionEnum) {
    val TAG = javaClass.name

    private var currentRecordType = TranslateRecordTypeEnum.RECORD_ALL
    private var navItems = mutableListOf<TranslateRecordNavModel>()

    companion object {
        // 默认分页大小
        const val DEFAULT_PAGE_SIZE = 20
    }

    init {
        initNavItems()
    }

    /**
     * 初始化导航项
     */
    private fun initNavItems() {
        navItems.clear()
        TranslateRecordTypeEnum.getRecordTypesByVersion(versionEnum).forEach { type ->
            navItems.add(TranslateRecordNavModel(type, type == currentRecordType))
        }
    }

    /**
     * 获取导航项列表
     */
    fun getNavItems(): List<TranslateRecordNavModel> {
        return navItems
    }

    /**
     * 设置当前记录类型
     */
    fun setCurrentRecordType(recordType: TranslateRecordTypeEnum) {
        this.currentRecordType = recordType
        // 更新导航项选中状态
        navItems.forEach { it.isSelected = it.recordType == recordType }
    }

    /**
     * 获取当前记录类型
     */
    fun getCurrentRecordType(): TranslateRecordTypeEnum {
        return currentRecordType
    }

    /**
     * 插入翻译记录
     */
    fun insertTranslateHistory(
        uuid: String = UUID.randomUUID().toString(),
        type: Int,
        sourceText: String,
        targetText: String = "",
        targetSecondText: String = ""
    ) {
        val history = TranslateHistory(
            UUID = uuid,
            type = type,
            sourceText = sourceText,
            targetText = targetText,
            targetSecondText = targetSecondText,
            versionCode = versionEnum.code,
            createTime = LocalDateTime.now()
        )
        TranslateHistoryDBManager.insertTranslateHistory(history)
        L.d(TAG, "插入翻译记录: versionCode=${versionEnum.code}, type=$type")
    }

    /**
     * 根据当前版本和类型获取翻译记录列表（分页）
     */
    suspend fun getTranslateHistoryList(
        search: String = "",
        pageIndex: Int = 0,
        pageSize: Int = DEFAULT_PAGE_SIZE
    ): List<TranslateHistory> = withContext(Dispatchers.IO) {
        L.d(TAG, "获取翻译记录: versionCode=${versionEnum.code}, type=${currentRecordType.code}, search=$search")
        
        return@withContext when (currentRecordType) {
            TranslateRecordTypeEnum.RECORD_TONGCHUAN -> {
                // 同声传译合并查询：查询多个相关类型
                val mergedTypes = TranslateRecordTypeEnum.getTongchuanMergedTypes(versionEnum)
                L.d(TAG, "同声传译合并查询，包含类型: $mergedTypes")
                TranslateHistoryDBManager.getTranslateHistoryListByVersionAndModes(
                    versionCode = versionEnum.code,
                    modes = mergedTypes,
                    search = search,
                    pageIndex = pageIndex,
                    pageSize = pageSize
                )
            }
            TranslateRecordTypeEnum.RECORD_ALL -> {
                // 查询所有类型
                TranslateHistoryDBManager.getTranslateHistoryListByVersion(
                    versionCode = versionEnum.code,
                    mode = 0, // 0 表示全部
                    search = search,
                    pageIndex = pageIndex,
                    pageSize = pageSize
                )
            }
            else -> {
                // 查询单一类型
                TranslateHistoryDBManager.getTranslateHistoryListByVersion(
                    versionCode = versionEnum.code,
                    mode = currentRecordType.code,
                    search = search,
                    pageIndex = pageIndex,
                    pageSize = pageSize
                )
            }
        }
    }

    /**
     * 获取分组后的翻译记录列表
     */
    suspend fun getGroupedTranslateHistoryList(
        search: String = "",
        pageIndex: Int = 0,
        pageSize: Int = DEFAULT_PAGE_SIZE
    ): List<TranslateHistoryGroup> = withContext(Dispatchers.IO) {
        val dataList = getTranslateHistoryList(search, pageIndex, pageSize)
        return@withContext dataList.groupBy { it.createTime.toString("yyyy/MM/dd") }
            .map { TranslateHistoryGroup(createTime = it.key, historyList = it.value) }
    }

    /**
     * 根据UUID获取翻译记录详情列表
     */
    suspend fun getTranslateHistoryDetailsByUUID(
        uuid: String,
        pageIndex: Int = 0,
        pageSize: Int = DEFAULT_PAGE_SIZE
    ): List<TranslateHistory> = withContext(Dispatchers.IO) {
        return@withContext TranslateHistoryDBManager.getTranslateHistoryListByUUID(
            uuid = uuid,
            pageIndex = pageIndex,
            pageSize = pageSize
        )
    }

    /**
     * 根据UUIDs获取翻译记录列表
     */
    suspend fun getTranslateHistoryByUUIDs(uuids: List<String>): List<TranslateHistory> = withContext(Dispatchers.IO) {
        return@withContext TranslateHistoryDBManager.getTranslateHistoryListByUUIDs(uuids)
    }

    /**
     * 更新翻译记录
     */
    fun updateTranslateHistory(historyList: List<TranslateHistory>) {
        TranslateHistoryDBManager.update(historyList)
        L.d(TAG, "更新翻译记录: ${historyList.size}条")
    }

    /**
     * 标记翻译记录
     */
    fun markTranslateHistory(historyList: List<TranslateHistory>) {
        historyList.forEach { it.isMarked = true }
        updateTranslateHistory(historyList)
    }

    /**
     * 根据UUID删除翻译记录组
     */
    fun deleteTranslateHistoryByUUID(uuid: String) {
        TranslateHistoryDBManager.deleteByUUID(uuid)
        L.d(TAG, "删除翻译记录组: $uuid")
    }

    /**
     * 根据UUID列表删除翻译记录组
     */
    fun deleteTranslateHistoryByUUIDs(uuids: List<String>) {
        TranslateHistoryDBManager.deleteByUUIDList(uuids)
        L.d(TAG, "删除翻译记录组: ${uuids.size}组")
    }

    /**
     * 根据ID列表删除翻译记录
     */
    fun deleteTranslateHistoryByIDs(ids: List<Long>) {
        TranslateHistoryDBManager.deleteByIDs(ids)
        L.d(TAG, "删除翻译记录: ${ids.size}条")
    }

    /**
     * 根据功能模式获取记录类型
     */
    fun getRecordTypeByFuncMode(mode: IntelligentFuncModeEnum): Int {
        return when (mode) {
            IntelligentFuncModeEnum.MODE_TONGCHUAN -> TranslateHistory.type_listen
            IntelligentFuncModeEnum.MODE_TONGCHUAN_FREE -> TranslateHistory.type_listen_orignal
            IntelligentFuncModeEnum.MODE_TONGCHUAN_2 -> TranslateHistory.type_listen_bilingual
            IntelligentFuncModeEnum.MODE_FREEDIALOG_2 -> TranslateHistory.type_listen_freeformulti
            IntelligentFuncModeEnum.MODE_FREEDIALOG_DOUBLE -> TranslateHistory.type_listen_freefortwo
            else -> TranslateHistory.type_listen
        }
    }

    /**
     * 创建翻译记录实例
     */
    fun createTranslateHistory(
        uuid: String = UUID.randomUUID().toString(),
        mode: IntelligentFuncModeEnum
    ): TranslateHistory {
        return TranslateHistory(
            UUID = uuid,
            type = getRecordTypeByFuncMode(mode),
            versionCode = versionEnum.code,
            createTime = LocalDateTime.now()
        )
    }

    /**
     * 检查记录是否属于当前版本
     */
    fun isRecordBelongsToCurrentVersion(history: TranslateHistory): Boolean {
        return history.versionCode == versionEnum.code
    }

    /**
     * 获取当前版本支持的记录类型
     */
    fun getSupportedRecordTypes(): List<TranslateRecordTypeEnum> {
        return TranslateRecordTypeEnum.getRecordTypesByVersion(versionEnum)
    }

    /**
     * 清理缓存
     */
    fun clearCache() {
        navItems.clear()
        currentRecordType = TranslateRecordTypeEnum.RECORD_ALL
        initNavItems()
    }

    /**
     * 获取统计信息
     */
    suspend fun getRecordStatistics(): RecordStatistics = withContext(Dispatchers.IO) {
        val allRecords = TranslateHistoryDBManager.getTranslateHistoryListByVersion(
            versionCode = versionEnum.code,
            mode = 0, // 全部
            pageSize = Int.MAX_VALUE // 获取所有记录用于统计
        )
        
        // 统计信息中需要考虑合并类型
        val typeStatistics = mutableMapOf<Int, Int>()
        val tongchuanTypes = TranslateRecordTypeEnum.getTongchuanMergedTypes(versionEnum)
        
        allRecords.groupBy { it.type }.forEach { (type, records) ->
            when {
                tongchuanTypes.contains(type) -> {
                    // 同声传译相关类型都归类到合并类型中
                    val currentCount = typeStatistics[TranslateRecordTypeEnum.RECORD_TONGCHUAN.code] ?: 0
                    typeStatistics[TranslateRecordTypeEnum.RECORD_TONGCHUAN.code] = currentCount + records.size
                }
                else -> {
                    typeStatistics[type] = records.size
                }
            }
        }
        
        val statistics = RecordStatistics(
            totalCount = allRecords.size,
            markedCount = allRecords.count { it.isMarked },
            typeStatistics = typeStatistics
        )
        
        L.d(TAG, "记录统计: $statistics")
        return@withContext statistics
    }

    /**
     * 记录统计数据类
     */
    data class RecordStatistics(
        val totalCount: Int,
        val markedCount: Int,
        val typeStatistics: Map<Int, Int>
    )

    /**
     * 生成测试数据（仅用于开发测试和UI效果查看）
     * 注意：正式发布时此方法可以保留，但不应该被自动调用
     */
    fun generateTestData() {
        L.d(TAG, "开始生成测试数据")
        
        val testRecords = mutableListOf<TranslateHistory>()
        val currentTime = LocalDateTime.now()
        
        // 1. 今天的记录
        val todayUUID1 = UUID.randomUUID().toString()
        testRecords.add(TranslateHistory(
            UUID = todayUUID1,
            type = TranslateHistory.type_listen,
            sourceText = "欢迎来到AI智能翻译系统，我们将为您提供专业的同声传译服务。",
            targetText = "Welcome to the AI intelligent translation system, we will provide you with professional simultaneous interpretation services.",
            versionCode = versionEnum.code,
            createTime = currentTime.minusMinutes(30),
            isMarked = true
        ))
        
        testRecords.add(TranslateHistory(
            UUID = todayUUID1,
            type = TranslateHistory.type_listen,
            sourceText = "这是一个测试句子，用于验证翻译功能是否正常工作。",
            targetText = "This is a test sentence to verify that the translation function is working properly.",
            versionCode = versionEnum.code,
            createTime = currentTime.minusMinutes(29)
        ))

        // 2. 双语播报记录
        val todayUUID2 = UUID.randomUUID().toString()
        testRecords.add(TranslateHistory(
            UUID = todayUUID2,
            type = TranslateHistory.type_listen_bilingual,
            sourceText = "今天的天气很好，适合外出游玩。",
            targetText = "The weather is very good today, suitable for going out to play.",
            targetSecondText = "El clima está muy bueno hoy, perfecto para salir a pasear.",
            versionCode = versionEnum.code,
            createTime = currentTime.minusHours(2)
        ))

        // 4. 昨天的记录 - 多人自由对话
        val yesterdayUUID1 = UUID.randomUUID().toString()
        testRecords.add(TranslateHistory(
            UUID = yesterdayUUID1,
            type = TranslateHistory.type_listen_freeformulti,
            sourceText = "多人会议讨论：关于项目进度的汇报。",
            targetText = "Multi-person meeting discussion: Report on project progress.",
            targetSecondText = "Discusión de reunión multipersonal: Informe sobre el progreso del proyecto.",
            versionCode = versionEnum.code,
            createTime = currentTime.minusDays(1).minusHours(5)
        ))
        
        testRecords.add(TranslateHistory(
            UUID = yesterdayUUID1,
            type = TranslateHistory.type_listen_freeformulti,
            sourceText = "我们需要在下周完成这个功能模块的开发。",
            targetText = "We need to complete the development of this functional module next week.",
            targetSecondText = "Necesitamos completar el desarrollo de este módulo funcional la próxima semana.",
            versionCode = versionEnum.code,
            createTime = currentTime.minusDays(1).minusHours(4)
        ))

        // 5. 双人自由对话
        val yesterdayUUID2 = UUID.randomUUID().toString()
        testRecords.add(TranslateHistory(
            UUID = yesterdayUUID2,
            type = TranslateHistory.type_listen_freefortwo,
            sourceText = "你好，很高兴见到你！",
            targetText = "Hello, nice to meet you!",
            versionCode = versionEnum.code,
            createTime = currentTime.minusDays(1).minusHours(8)
        ))
        
        testRecords.add(TranslateHistory(
            UUID = yesterdayUUID2,
            type = TranslateHistory.type_listen_freefortwo,
            sourceText = "我也很高兴见到你，今天过得怎么样？",
            targetText = "I'm also very happy to meet you, how was your day?",
            versionCode = versionEnum.code,
            createTime = currentTime.minusDays(1).minusHours(7),
            isMarked = true
        ))

        // 6. 前天的记录
        val dayBeforeUUID = UUID.randomUUID().toString()
        testRecords.add(TranslateHistory(
            UUID = dayBeforeUUID,
            type = TranslateHistory.type_listen,
            sourceText = "人工智能技术正在快速发展，为我们的生活带来了很多便利。",
            targetText = "Artificial intelligence technology is developing rapidly, bringing many conveniences to our lives.",
            versionCode = versionEnum.code,
            createTime = currentTime.minusDays(2).minusHours(3)
        ))

        // 7. 一周前的记录
        val weekAgoUUID = UUID.randomUUID().toString()
        testRecords.add(TranslateHistory(
            UUID = weekAgoUUID,
            type = TranslateHistory.type_listen_bilingual,
            sourceText = "国际会议即将开始，请各位代表准备好发言稿。",
            targetText = "The international conference is about to begin, please prepare your speeches.",
            targetSecondText = "La conferencia internacional está a punto de comenzar, por favor preparen sus discursos.",
            versionCode = versionEnum.code,
            createTime = currentTime.minusDays(7).minusHours(2),
            isMarked = true
        ))

        // 8. 更多不同类型的记录
        val moreTestUUID = UUID.randomUUID().toString()
        testRecords.add(TranslateHistory(
            UUID = moreTestUUID,
            type = TranslateHistory.type_listen_freeformulti,
            sourceText = "团队协作是成功的关键，每个人都应该发挥自己的优势。",
            targetText = "Teamwork is the key to success, everyone should leverage their strengths.",
            targetSecondText = "El trabajo en equipo es la clave del éxito, todos deben aprovechar sus fortalezas.",
            versionCode = versionEnum.code,
            createTime = currentTime.minusDays(3).minusHours(1)
        ))

        // 批量插入测试数据
        testRecords.forEach { history ->
            TranslateHistoryDBManager.insertTranslateHistory(history)
        }
        
        L.d(TAG, "测试数据生成完成，共生成 ${testRecords.size} 条记录")
    }

    /**
     * 清除测试数据（开发测试功能，可选实现）
     */
    fun clearTestData() {
        L.d(TAG, "清除当前版本的所有测试数据")
        // 这里可以根据需要实现特定的测试数据清除逻辑
        // 目前通过UI界面的"清除当前版本数据"功能实现
    }
} 