package com.example.torujanapp

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity


class SelectJanActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_jan)

        // 1. Intentからインデックスを受け取る（デフォルトは0）
        val groupIndex = intent.getIntExtra("GROUP_INDEX", 0)

        val app = application as MyApplication

        // 2. 指定されたグループのリストを取り出す
        // returnHistory[groupIndex] が選ばれたグループの List<String> になる
        // SelectJanActivity.kt の onCreate内
        // SelectJanActivity.kt の onCreate 内
        val receipt = app.returnHistory.getOrNull(groupIndex)
        val selectedGroup = receipt?.janList ?: listOf() // receiptからリストを取り出す

        // 3. 表示用の文字列を作る（例：「13桁の番号」）
        val displayList = selectedGroup.map { it }

        val listView = findViewById<ListView>(R.id.historyListView)
        val adapter = ArrayAdapter(this, R.layout.list_item_history, displayList)
        listView.adapter = adapter

        // 4. JANコードがタップされたらバーコード画面へ
        listView.setOnItemClickListener { _, _, position, _ ->
            val janCode = selectedGroup[position]
            val intent = Intent(this, PrintBarcodeActivityForHistory::class.java)
            intent.putExtra("MY_KEY", janCode) // 既存のPrintBarcodeActivityの合言葉に合わせる
            intent.putExtra("GROUP_INDEX", groupIndex) // どのグループか
            intent.putExtra("JAN_INDEX", position)     // その中の何番目か
            startActivity(intent)
        }

        findViewById<Button>(R.id.backButton).setOnClickListener {
            finish()
        }
    }
}