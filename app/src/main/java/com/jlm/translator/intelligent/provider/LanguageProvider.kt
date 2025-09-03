package com.jlm.translator.intelligent.provider

import android.content.Context
import androidx.compose.ui.text.intl.Locale
import cn.chawloo.base.ext.toast
import cn.chawloo.base.utils.MK
import com.jlm.translator.entity.Language
import com.jlm.translator.entity.LanguageGroup
import com.jlm.translator.manager.LanguageJsonInfoEnum
import com.jlm.translator.utils.LanguageUtil
import com.safframework.log.L
import java.util.concurrent.ConcurrentHashMap

/**
 * 语言解析的提供者 - 优化版本
 * 改进点：
 * 1. 使用线程安全的缓存机制
 * 2. 消除重复代码
 * 3. 改进错误处理
 * 4. 简化代码结构
 */
object LanguageProvider {
    private const val TAG = "LanguageProvider"
    
    // 路径常量
    private const val LANG_SCHEME_PATH = "languages/"
    
    // 默认值常量
    private const val DEFAULT_RECOG_KEY = "zh"
    private const val DEFAULT_TRANS_KEY = "en"
    private const val DEFAULT_LOCAL_LANG_KEY = "zh"
    
    // 默认语言键集合
    val defaultFreedialogSourceKeys: Set<String> = setOf("zh", "en", "ar", "nl", "fr", "ja", "ko")
    
    // 本地支持的语言键
    private val localKeys = setOf("zh", "en", "es", "de", "fr", "vi", "pt", "ja", "ko", "ar", "it")

    // 线程安全的缓存
    private val languageGroupCache = ConcurrentHashMap<LanguageJsonInfoEnum, List<LanguageGroup>>()
    
    /**
     * 获取语言分组列表 - 优化版本
     */
    fun getLanguageGroupList(context: Context, languageJsonInfoEnum: LanguageJsonInfoEnum): List<LanguageGroup> {
        return try {
            // 先从缓存中获取
            val cachedList = languageGroupCache[languageJsonInfoEnum]
            if (cachedList != null) {
                // 验证缓存数据的类型安全性
                try {
                    // 尝试访问第一个元素的languageList属性来验证类型
                    cachedList.firstOrNull()?.languageList
                    cachedList
                } catch (e: ClassCastException) {
                    L.w(TAG, "Cached data type error for ${languageJsonInfoEnum.name}, clearing cache")
                    // 清除有问题的缓存
                    languageGroupCache.remove(languageJsonInfoEnum)
                    loadLanguageGroupsFromAssets(context, languageJsonInfoEnum)
                }
            } else {
                loadLanguageGroupsFromAssets(context, languageJsonInfoEnum)
            }
        } catch (e: Exception) {
            L.e(TAG, "Error loading language groups for: ${languageJsonInfoEnum.name}, error: ${e.message}")
            toast("语言加载异常，请退出重新进入")
            emptyList()
        }
    }

    /**
     * 从Assets加载语言分组数据
     */
    private fun loadLanguageGroupsFromAssets(context: Context, languageJsonInfoEnum: LanguageJsonInfoEnum): List<LanguageGroup> {
        return try {
            val langPath = "$LANG_SCHEME_PATH${languageJsonInfoEnum.fileName}"
            val langGroupList = LanguageUtil.getAssetsLanguageJsonObj(context, langPath)
            
            if (langGroupList.isNullOrEmpty()) {
                L.e(TAG, "Failed to load language groups for: ${languageJsonInfoEnum.name}")
                toast("语言分组获取失败，请退出重新进入")
                emptyList()
            } else {
                // 验证加载的数据类型
                val validatedList = langGroupList.filterIsInstance<LanguageGroup>()
                if (validatedList.size != langGroupList.size) {
                    L.w(TAG, "Found invalid data types in language groups for ${languageJsonInfoEnum.name}")
                }
                // 缓存验证后的结果
                languageGroupCache[languageJsonInfoEnum] = validatedList
                validatedList
            }
        } catch (e: Exception) {
            L.e(TAG, "Error loading language groups from assets: ${e.message}")
            emptyList()
        }
    }

    /**
     * 获取单个语言对象
     */
    fun getLanguage(
        context: Context, 
        mkKey: String, 
        defaultKey: String, 
        languageJsonInfoEnum: LanguageJsonInfoEnum
    ): Language {
        val languageGroupList = getLanguageGroupList(context, languageJsonInfoEnum)
        return getLanguage(context, mkKey, defaultKey, languageGroupList)
    }

    /**
     * 从语言分组列表中获取单个语言对象
     */
    fun getLanguage(
        context: Context, 
        mkKey: String, 
        defaultKey: String, 
        languageGroupList: List<LanguageGroup>
    ): Language {
        return try {
            if (languageGroupList.isEmpty()) {
                L.w(TAG, "Language group list is empty")
                return Language()
            }
            
            getMKObjectFromGroups(languageGroupList, mkKey, defaultKey)
        } catch (e: Exception) {
            L.e(TAG, "Error getting language for key: $mkKey")
            toast("语言获取异常")
            Language()
        }
    }

    /**
     * 获取语言列表
     */
    fun getLanguageForList(
        context: Context, 
        mkKey: String, 
        defaultKeys: Set<String>, 
        languageGroupList: List<LanguageGroup>
    ): MutableList<Language> {
        return try {
            if (languageGroupList.isEmpty()) {
                L.w(TAG, "Language group list is empty for key: $mkKey")
                return mutableListOf()
            }
            
            val defaultLanguageList = languageGroupList[0].languageList
            getMKSetKeys(defaultLanguageList, mkKey, defaultKeys)
        } catch (e: Exception) {
            L.e(TAG, "Error getting language list for key: $mkKey")
            mutableListOf()
        }
    }

    /**
     * 获取搜索后的语言分组
     * 支持处理混合类型列表：List<Any> 可能包含 LanguageGroup 和 Language 对象
     */
    fun getSearchLanguageGroupObj(inputList: List<Any>, filter: String): List<LanguageGroup> {
        return try {
            val langList = mutableListOf<Language>()
            
            inputList.forEach { item ->
                when (item) {
//                    is LanguageGroup -> {
//                        // 处理 LanguageGroup 对象
//                        item.languageList.forEach { language ->
//                            if (language.getLangName().contains(filter, ignoreCase = true)) {
//                                langList.add(language)
//                            }
//                        }
//                    }
                    is Language -> {
                        // 处理直接的 Language 对象
                        if (item.getLangName().contains(filter, ignoreCase = true)) {
                            langList.add(item)
                        }
                    }
                    // 忽略其他类型的对象
                }
            }
            
            val searchGroup = LanguageGroup(
                category_key = "category_search", 
                languageList = langList
            )
            
            listOf(searchGroup)
        } catch (e: Exception) {
            L.e(TAG, "Error filtering languages with filter: $filter")
            emptyList()
        }
    }

    /**
     * 重载方法：处理纯 LanguageGroup 列表（向后兼容）
     */
//    fun getSearchLanguageGroupObj(languageGroupList: List<LanguageGroup>, filter: String): List<LanguageGroup> {
//        return try {
//            val langList = mutableListOf<Language>()
//
//            languageGroupList.forEach { group ->
//                group.languageList.forEach { language ->
//                    if (language.getLangName().contains(filter, ignoreCase = true)) {
//                        langList.add(language)
//                    }
//                }
//            }
//
//            val searchGroup = LanguageGroup(
//                category_key = "category_search",
//                languageList = langList
//            )
//
//            listOf(searchGroup)
//        } catch (e: Exception) {
//            L.e(TAG, "Error filtering languages with filter: $filter")
//            emptyList()
//        }
//    }

    /**
     * 获取本地语言键
     */
    fun getLocalKey(key: String, defaultLocalKey: String = DEFAULT_LOCAL_LANG_KEY): String {
        return if (key in localKeys) key else defaultLocalKey
    }

    /**
     * 清除缓存
     */
    fun clearCache() {
        languageGroupCache.clear()
        L.d(TAG, "Language cache cleared")
    }

    /**
     * 清除指定类型的缓存（用于修复类型错误）
     */
    fun clearCacheForType(languageJsonInfoEnum: LanguageJsonInfoEnum) {
        languageGroupCache.remove(languageJsonInfoEnum)
        L.d(TAG, "Language cache cleared for type: ${languageJsonInfoEnum.name}")
    }

    /**
     * 获取缓存大小（用于调试）
     */
    fun getCacheSize(): Int = languageGroupCache.size

    // 私有辅助方法

    /**
     * 从语言分组列表中获取语言对象，如果有缓存则优先使用缓存的key在所有分组中查找
     */
    private fun getMKObjectFromGroups(languageGroupList: List<LanguageGroup>, mkKey: String, defaultKey: String): Language {
        return try {
            // 先从MK中获取缓存的语言对象
            val cachedLanguage = MK.decodeParcelable(mkKey, Language::class.java)
            
            if (cachedLanguage != null) {
                // 如果有缓存，使用缓存语言的key在所有LanguageGroup中查找最新的语言对象
                val cachedKey = cachedLanguage.getItemKey()
                L.d(TAG, "Found cached language with key: $cachedKey, finding in all language groups")
                findLanguageByKeyInGroups(languageGroupList, cachedKey)
                    ?: run {
                        L.w(TAG, "Cached language key: $cachedKey not found in language groups, fallback to default")
                        findLanguageByKeyInGroups(languageGroupList, defaultKey)
                    }
            } else {
                // 没有缓存，使用默认key在所有分组中查找
                L.d(TAG, "No cached language found, using default key: $defaultKey")
                findLanguageByKeyInGroups(languageGroupList, defaultKey)
            } ?: run {
                L.w(TAG, "No language found for key: $defaultKey, using first available")
                languageGroupList.firstOrNull()?.languageList?.firstOrNull() ?: Language()
            }
        } catch (e: Exception) {
            L.e(TAG, "Error getting MK object from groups for key: ${e.message}")
            findLanguageByKeyInGroups(languageGroupList, defaultKey) ?: Language()
        }
    }

    /**
     * 先从本地存储中获取语言对象，如果有缓存则通过key在languageList中找到对应的语言，否则用默认key查找
     */
    private fun getMKObject(languageList: List<Language>, mkKey: String, defaultKey: String): Language {
        return try {
            // 先从MK中获取缓存的语言对象
            val cachedLanguage = MK.decodeParcelable(mkKey, Language::class.java)
            
            if (cachedLanguage != null) {
                // 如果有缓存，使用缓存语言的key在languageList中查找最新的语言对象
                val cachedKey = cachedLanguage.getItemKey()
                L.d(TAG, "Found cached language with key: $cachedKey, finding in language list")
                findLanguageByKey(languageList, cachedKey)
                    ?: run {
                        L.w(TAG, "Cached language key: $cachedKey not found in current language list, fallback to default")
                        findLanguageByKey(languageList, defaultKey)
                    }
            } else {
                // 没有缓存，使用默认key查找
                L.d(TAG, "No cached language found, using default key: $defaultKey")
                findLanguageByKey(languageList, defaultKey)
            } ?: run {
                L.w(TAG, "No language found for key: $defaultKey, using first available")
                languageList.firstOrNull() ?: Language()
            }
        } catch (e: Exception) {
            L.e(TAG, "Error getting MK object for key: $mkKey")
            findLanguageByKey(languageList, defaultKey) ?: Language()
        }
    }

    /**
     * 在所有语言分组中根据键查找语言对象
     */
    private fun findLanguageByKeyInGroups(languageGroupList: List<LanguageGroup>, key: String): Language? {
        return try {
            // 添加类型安全检查
            languageGroupList
                .filterIsInstance<LanguageGroup>() // 确保只处理LanguageGroup类型
                .flatMap { group ->
                    try {
                        group.languageList ?: emptyList()
                    } catch (e: Exception) {
                        L.w(TAG, "Error accessing languageList in group: ${e.message}")
                        emptyList()
                    }
                }
                .firstOrNull { it.getItemKey() == key }
        } catch (e: Exception) {
            L.e(TAG, "Error in findLanguageByKeyInGroups for key: $key, error: ${e.message}")
            null
        }
    }

    /**
     * 根据键查找语言对象
     */
    private fun findLanguageByKey(languageList: List<Language>, key: String): Language? {
        return languageList.firstOrNull { it.getItemKey() == key }
    }

    /**
     * 获取语言列表基于键集合
     */
    private fun getMKSetKeys(
        languageList: List<Language>, 
        mkKey: String, 
        defaultKeys: Set<String>
    ): MutableList<Language> {
        return try {
            val savedKeys = MK.decodeStringSet(mkKey, defaultKeys) ?: defaultKeys
            val filteredLanguages = languageList.filter { it.getItemKey() in savedKeys }
            
            if (filteredLanguages.isEmpty()) {
                L.w(TAG, "No languages found for saved keys, using default keys")
                languageList.filter { it.getItemKey() in defaultKeys }.toMutableList()
            } else {
                filteredLanguages.toMutableList()
            }
        } catch (e: Exception) {
            L.e(TAG, "Error getting MK set keys for: $mkKey")
            // 发生错误时返回默认语言列表
            languageList.filter { it.getItemKey() in defaultKeys }.toMutableList()
        }
    }

    // 数据类用于语言配置
    data class LanguageConfig(
        val recogKey: String = DEFAULT_RECOG_KEY,
        val transKey: String = DEFAULT_TRANS_KEY,
        val localKey: String = DEFAULT_LOCAL_LANG_KEY
    )


    fun List<LanguageGroup>.getAllLanguages(): List<Language> {
        return this.flatMap { it.languageList }
    }
}