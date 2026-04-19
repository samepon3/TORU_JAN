package com.example.torujanapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class manualInputActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_manual_input)
        // 1. レイアウトで作ったボタンを取得
        val btnHome: Button = findViewById(R.id.homeButton)
        // 2. ボタンが押された時の処理を設定
        btnHome.setOnClickListener {
            // 3. Intent（インテント）を作って画面遷移を実行
            val app = application as MyApplication

            // もし一時リストに商品があれば、履歴に保存する
            if (app.currentSessionList.isNotEmpty()) {
                app.addHistory(app.currentSessionList.toList()) // ★ここで保存を実行！
                // 次回の返品処理のために一時リストを空にする
                app.currentSessionList.clear()

                Toast.makeText(this, "返品データを保存しました", Toast.LENGTH_SHORT).show()
            }

            // メイン画面へ戻る
            finish()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
        findViewById<Button>(R.id.backButton).setOnClickListener {
            val intent = Intent(this, SecondActivity::class.java)
            startActivity(intent)
        }
        val janInput: EditText = findViewById(R.id.janInput)
        val okButton: Button = findViewById(R.id.okButton)

        okButton.setOnClickListener {
            // 1. 文字列として取得
            val janCode = janInput.text.toString()

//            // 2. 数値（Intなど）に変換
//            // 空っぽだとエラーになるので、安全に変換します
//            val janNumber = inputText.toIntOrNull()

            if (janCode.isNotEmpty()) {
                // 数字が入っていた時の処理
                val app = application as MyApplication
                app.currentSessionList.add(janCode)
                // 3. Intent（インテント）を作って画面遷移を実行
                val intent = Intent(this, PrintBarcodeActivity::class.java)
                // 4. 値をセットする（"MY_KEY" は受け取り側で使う合言葉です）
                intent.putExtra("MY_KEY", janCode)
                startActivity(intent)
            } else {
                // 空だったり数字以外だった時の処理
                janInput.error = "数字を入れてください"
            }
        }
    }
}