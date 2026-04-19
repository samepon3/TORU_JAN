package com.example.torujanapp

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix

class PrintBarcodeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_print_barcode)
        // 1. レイアウトで作ったボタンを取得
        val btnHome: Button = findViewById(R.id.homeButton)
        // 2. ボタンが押された時の処理を設定
        btnHome.setOnClickListener {
            val app = application as MyApplication

            // もし一時リストに商品があれば、履歴に保存する
            if (app.currentSessionList.isNotEmpty()) {
                // 現在のリストのコピーを履歴に追加
                app.addHistory(app.currentSessionList.toList())
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

        findViewById<Button>(R.id.nextButton).setOnClickListener {
            val intent = Intent(this, SecondActivity::class.java)
            startActivity(intent)
        }
        // 1. Intentから値を取り出す（今回はIntで受け取る設定）
        val janCode = intent.getStringExtra("MY_KEY") ?: ""

        // 2. レイアウトの部品を取得
        val resultJan: TextView = findViewById(R.id.resultJan)
        val barcodeImageView: ImageView = findViewById(R.id.barcodeImageView)

        // 3. テキストを表示
        resultJan.text = janCode

        // 4. バーコード生成処理
        // 0だと入力なしとみなして処理しない（Intのデフォルト値対策）
        if (janCode.isNotEmpty()) {
            try {
                val bitmap = createBarcode(janCode)
                barcodeImageView.setImageBitmap(bitmap)
            } catch (e: Exception) {
                e.printStackTrace()
                // エラー時はヒントを出すと親切です
                resultJan.text = "エラー: 正しいJANコードを入力してください"
            }
        }
    }

    // バーコードを生成する関数（クラスの中に移動させました）
    private fun createBarcode(text: String): Bitmap {
        val width = 700
        val height = 300
        val bitMatrix: BitMatrix = MultiFormatWriter().encode(
            text,
            BarcodeFormat.CODE_128,
            width,
            height
        )
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(x, y, if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE)
            }
        }
        return bitmap
    }
}