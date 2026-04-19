package com.example.torujanapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. レイアウトで作ったボタンを取得
        val btnA: Button = findViewById(R.id.createJanButton)

        // 2. ボタンが押された時の処理を設定
        btnA.setOnClickListener {
            // 3. Intent（インテント）を作って画面遷移を実行
            val intent = Intent(this, SecondActivity::class.java)
            startActivity(intent)
        }
        findViewById<Button>(R.id.showHistoryButton).setOnClickListener {
            val intent = Intent(this, HistoryActivity::class.java)
            startActivity(intent)
        }
    }
}



