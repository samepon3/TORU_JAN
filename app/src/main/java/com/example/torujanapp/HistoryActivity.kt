package com.example.torujanapp

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class HistoryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        val app = application as MyApplication
        val listView = findViewById<ListView>(R.id.historyListView)

        // 1. 初回のリスト表示
        updateListView(listView, app)

        // 2. 長押し削除の処理
        listView.setOnItemLongClickListener { _, _, position, _ ->
            android.app.AlertDialog.Builder(this)
                .setTitle("削除の確認")
                .setMessage("この履歴を削除しますか？")
                .setPositiveButton("削除") { _, _ ->
                    app.deleteHistory(position) // MyApplication側の削除関数
                    updateListView(listView, app) // 表示を最新状態に更新
                    Toast.makeText(this, "削除しました", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("キャンセル", null)
                .show()
            true
        }

        // 3. タップで詳細画面へ
        listView.setOnItemClickListener { _, _, position, _ ->
            val intent = Intent(this, SelectJanActivity::class.java)
            intent.putExtra("GROUP_INDEX", position)
            startActivity(intent)
        }

        // 4. 戻るボタン
        findViewById<Button>(R.id.backButton).setOnClickListener {
            finish()
        }
    }

    // 表示を更新するための共通関数（装飾ロジックをここに集約）
    private fun updateListView(listView: ListView, app: MyApplication) {
        val displayList = app.returnHistory.mapIndexed { index, receipt ->
            val title = "返品レシート ${index + 1}個目"
            val content = "\n保存日時: ${receipt.date}" // ここをJANコードから日時に変更！
            val fullText = title + content

            val builder = SpannableStringBuilder(fullText)

            // タイトルの装飾（ピンク）
            builder.setSpan(StyleSpan(Typeface.BOLD), 0, title.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            builder.setSpan(ForegroundColorSpan(Color.parseColor("#E21782")), 0, title.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

            // 日時部分の色をグレーにする（任意）
            builder.setSpan(ForegroundColorSpan(Color.GRAY), title.length, fullText.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

            builder
        }
        val adapter = ArrayAdapter<CharSequence>(this, R.layout.list_item_history, displayList)
        listView.adapter = adapter
    }
}