package com.example.torujanapp

import android.app.Application
import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ReturnReceipt(
    val date: String,          // 保存日時（"2026/04/19 19:30" など）
    val janList: List<String>  // その時のJANコード一覧
)
class MyApplication : Application() {
    // 現在スキャン中のリスト（一時保存）
    val currentSessionList = mutableListOf<String>()

    var returnHistory: MutableList<ReturnReceipt> = mutableListOf()

    override fun onCreate() {
        super.onCreate()
        // アプリ起動時に保存されたデータを読み込む
        loadHistory()
    }

    // 履歴を追加して保存する関数
    fun addHistory(newList: List<String>) {
        // 現在時刻をフォーマットして取得
        val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())
        val currentDate = sdf.format(Date())

        // 日時とリストをセットにして先頭に追加
        returnHistory.add(0, ReturnReceipt(currentDate, newList))

        while (returnHistory.size > 30) {
            returnHistory.removeAt(returnHistory.size - 1)
        }
        saveHistory()
    }

    private fun saveHistory() {
        val prefs = getSharedPreferences("TORUJAN_PREFS", MODE_PRIVATE)
        val gson = Gson()
        // リスト全体を JSON 文字列に変換
        val json = gson.toJson(returnHistory)
        prefs.edit().putString("HISTORY_DATA", json).apply()
    }
    fun deleteHistory(index: Int) {
        if (index in returnHistory.indices) {
            returnHistory.removeAt(index)
            saveHistory()
        }
    }
    private fun loadHistory() {
        val prefs = getSharedPreferences("TORUJAN_PREFS", MODE_PRIVATE)
        val json = prefs.getString("HISTORY_DATA", null)
        if (json != null) {
            val gson = Gson()
            // loadHistory の中
            val type = object : TypeToken<MutableList<ReturnReceipt>>() {}.type
            returnHistory = gson.fromJson(json, type)
        }
    }
}