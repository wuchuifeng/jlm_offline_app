package com.jlm.translator.utils

import android.content.Context
import cn.chawloo.base.ext.fromJson
import com.jlm.translator.entity.Language
import com.jlm.translator.entity.LanguageGroup
import com.safframework.log.L
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

object LanguageUtil {

    val TAG: String = javaClass.name

    private var languageGroupList: List<LanguageGroup> = emptyList()
    private var languageRecognitionGroupList: List<LanguageGroup> = emptyList()
    private var languageIdentificationGroupList: List<LanguageGroup> = emptyList()
    fun getAssetsTestJsonObj(context: Context): List<Language> {
        var memberList: List<Language> = ArrayList()
        try {
            val inputStream = context.assets.open("language1.json")
            val text: String = try {
                readJsonFromAssets(inputStream)
            } catch (e: Exception) {
                e.printStackTrace()
                "[]"
            }
            memberList = text.fromJson<List<Language>>()
        } catch (e: Exception) {
            L.e("LanguageUtil.getAssetsTestJsonObj>>>error", e.message)
            e.printStackTrace()
        }
        return memberList
    }

    fun getLanguageGroupObj(context: Context): List<LanguageGroup> {
        if(languageGroupList.isEmpty()) {
            languageGroupList = getAssetsLanguageJsonObj(context)
        }
        return languageGroupList
    }

    fun getLanguageRecognitionGroupObj(context: Context): List<LanguageGroup> {
        if(languageRecognitionGroupList.isEmpty()) {
            languageRecognitionGroupList = getAssetsRecognitionLanguageJsonObj(context)
        }
        return languageRecognitionGroupList
    }

    fun getLanguageIdentificationGroupObj(context: Context): List<LanguageGroup> {
        if(languageIdentificationGroupList.isEmpty()) {
            languageIdentificationGroupList = getAssetsIdentificationLanguageJsonObj(context)
        }
        return languageIdentificationGroupList
    }

    /**
     * 获取最新的语言列表
     * */
    fun getAssetsLanguageJsonObj(context: Context): List<LanguageGroup> {
        var memberList: List<LanguageGroup> = ArrayList()
        try {
            val inputStream = context.assets.open("language/lang_synthesis.json")
            val text: String = try {
                readJsonFromAssets(inputStream)
            } catch (e: Exception) {
                e.printStackTrace()
                "[]"
            }
            memberList = text.fromJson<List<LanguageGroup>>()
        } catch (e: Exception) {
            L.e(TAG, "getAssetsLanguageJsonObj>>>${e.message}")
            e.printStackTrace()
        }
        return memberList
    }

    internal fun getAssetsLanguageJsonObj(context: Context, path: String): List<LanguageGroup> {
        L.d(TAG, "getAssetsLanguageJsonObj>>>${path}")
        var memberList: List<LanguageGroup> = ArrayList()
        try {
            val inputStream = context.assets.open(path)
            var text: String = try {
                readJsonFromAssets(inputStream)
            } catch (e: Exception) {
                e.printStackTrace()
                "[]"
            }
            memberList = text.fromJson<List<LanguageGroup>>()
        } catch (e: Exception) {
            L.e(TAG, "getAssetsLanguageJsonObj>>>${e.message}")
            e.printStackTrace()
        }
        return memberList
    }

    /**
     * 获取语音可识别的语言列表
     * */
    private fun getAssetsRecognitionLanguageJsonObj(context: Context): List<LanguageGroup> {
        var memberList: List<LanguageGroup> = ArrayList()
        try {
            val inputStream = context.assets.open("language/lang_recognition.json")
            val text: String = try {
                readJsonFromAssets(inputStream)
            } catch (e: Exception) {
                e.printStackTrace()
                "[]"
            }
            memberList = text.fromJson<List<LanguageGroup>>()
        } catch (e: Exception) {
            L.e(TAG, "getAssetsRecognitionLanguageJsonObj>>>${e.message}")
            e.printStackTrace()
        }
        return memberList
    }

    /**
     * 获取语音可识别的语言列表
     * */
    private fun getAssetsIdentificationLanguageJsonObj(context: Context): List<LanguageGroup> {
        var memberList: List<LanguageGroup> = ArrayList()
        try {
            val inputStream = context.assets.open("language/lang_identification.json")
            val text: String = try {
                readJsonFromAssets(inputStream)
            } catch (e: Exception) {
                e.printStackTrace()
                "[]"
            }
            memberList = text.fromJson<List<LanguageGroup>>()
        } catch (e: Exception) {
            L.e(TAG, "getAssetsRecognitionLanguageJsonObj>>>${e.message}")
            e.printStackTrace()
        }
        return memberList
    }


    @Throws(java.lang.Exception::class)
    internal fun readJsonFromAssets(inputStream: InputStream): String {
        val reader = InputStreamReader(inputStream)
        val bufferedReader = BufferedReader(reader)
        val buffer = StringBuilder()
        bufferedReader.forEachLine {
            buffer.append(it)
            buffer.append("\n")
        }
        return buffer.toString()
    }

    /**
     * 获取搜索后的语言分组
     * */
    fun getSearchLanguageGroupObj(languageGroupList: List<LanguageGroup> ,filter: String): List<LanguageGroup> {
        val langList = mutableListOf<Language>()
        languageGroupList.forEach { g ->
            g.languageList.forEach {
                //将搜索到的语言加入到列表中
                it.getLangName()?.run {
                    if (contains(filter)) {
                        langList.add(it)
                    }
                }
            }
        }
        // 汇总到一个分组
        var langGroup = LanguageGroup(name = "搜索结果", languageList = langList)
        var list = listOf(langGroup)

        return list
    }
}