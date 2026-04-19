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
import kotlin.jvm.java

class PrintBarcodeActivityForHistory : AppCompatActivity() {
    private var currentGroupIndex = 0
    private var currentJanIndex = 0
    private lateinit var selectedGroup: List<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_print_barcode_for_history)

        // 1. データの受け取り
        currentGroupIndex = intent.getIntExtra("GROUP_INDEX", 0)
        currentJanIndex = intent.getIntExtra("JAN_INDEX", 0)

        val app = application as MyApplication
        val receipt = app.returnHistory.getOrNull(currentGroupIndex)
        selectedGroup = receipt?.janList ?: listOf()

        // 2. 初期表示
        updateUI()

        // 3. ボタンの処理
        findViewById<Button>(R.id.backButton).setOnClickListener {
            if (currentJanIndex > 0) {
                currentJanIndex--
                updateUI()
            } else {
                Toast.makeText(this, "最初の項目です", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<Button>(R.id.nextButton).setOnClickListener {
            if (currentJanIndex < selectedGroup.size - 1) {
                currentJanIndex++
                updateUI()
            } else {
                Toast.makeText(this, "最後の項目です", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<Button>(R.id.homeButton).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }
        findViewById<Button>(R.id.listButton).setOnClickListener {
            val intent = Intent(this, SelectJanActivity::class.java)
            intent.putExtra("GROUP_INDEX", currentGroupIndex)
            startActivity(intent)
        }
        findViewById<Button>(R.id.historyButton).setOnClickListener {
            val intent = Intent(this, HistoryActivity::class.java)
            startActivity(intent)
        }
    }

    // 表示を更新する関数
    private fun updateUI() {
        val janCode = selectedGroup.getOrNull(currentJanIndex) ?: ""
        val resultJan: TextView = findViewById(R.id.resultJan)
        val barcodeImageView: ImageView = findViewById(R.id.barcodeImageView)

        resultJan.text = "$janCode\n(${currentJanIndex + 1} / ${selectedGroup.size})"

        if (janCode.isNotEmpty()) {
            try {
                val bitmap = createBarcode(janCode)
                barcodeImageView.setImageBitmap(bitmap)
            } catch (e: Exception) {
                resultJan.text = "バーコード生成エラー"
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