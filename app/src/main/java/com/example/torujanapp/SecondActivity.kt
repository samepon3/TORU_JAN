package com.example.torujanapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class SecondActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_second)
        // 1. レイアウトで作ったボタンを取得
        val btnA: Button = findViewById(R.id.cameraInputButton)

        // 2. ボタンが押された時の処理を設定
        btnA.setOnClickListener {
            // 3. Intent（インテント）を作って画面遷移を実行
            val intent = Intent(this, CameraInputActivity::class.java)
            startActivity(intent)
        }
        // 1. レイアウトで作ったボタンを取得
        val btnB: Button = findViewById(R.id.manualInputButton)
        // 2. ボタンが押された時の処理を設定
        btnB.setOnClickListener {
            // 3. Intent（インテント）を作って画面遷移を実行
            val intent = Intent(this, manualInputActivity::class.java)
            startActivity(intent)
        }
        // 1. レイアウトで作ったボタンを取得
        val btnHome: Button = findViewById(R.id.homeButton)
        // 2. ボタンが押された時の処理を設定
        btnHome.setOnClickListener {
            val app = application as MyApplication

            // もし一時リストに商品があれば、履歴に保存する
            if (app.currentSessionList.isNotEmpty()) {
                // 現在のリストのコピーを履歴に追加

                app.addHistory(app.currentSessionList.toList()) // ★ここで保存を実行！
                // 次回の返品処理のために一時リストを空にする
                app.currentSessionList.clear()

                Toast.makeText(this, "返品データを保存しました", Toast.LENGTH_SHORT).show()
            }

            // メイン画面へ戻る
            finish()
            // 3. Intent（インテント）を作って画面遷移を実行
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
}