package com.example.torujanapp

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.ToneGenerator
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.util.concurrent.Executors

class CameraInputActivity : AppCompatActivity() {
    private var currentDetectedText = ""
//  private val toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 300)
    private var mediaPlayer: MediaPlayer? = null
    private var lastReading = ""
    private var readingCount = 0
    private val REQUIRED_STABILITY = 2 // 何回一致したら確定とするか
    private lateinit var previewView: PreviewView
    // ★ クラス全体で使えるようにここに追加
    // ★ 1. ここに TextView 用のプロパティを追加
    private lateinit var detectedTextView: TextView
    private lateinit var confirmButton: Button
    private val cameraExecutor = Executors.newSingleThreadExecutor()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_camera_input)

        previewView = findViewById(R.id.previewView)
        // ★ lateinit変数に代入
        confirmButton = findViewById(R.id.confirmButton)
        detectedTextView = findViewById(R.id.detectedTextView)


        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 10)
        }

        confirmButton.setOnClickListener {
            if (currentDetectedText.isNotEmpty()) {
                val intent = Intent(this, PrintBarcodeActivity::class.java)
                intent.putExtra("MY_KEY", currentDetectedText)
                // リストに追加
                val app = application as MyApplication
                app.currentSessionList.add(currentDetectedText)
                startActivity(intent)
                finish()
            }
        }

        findViewById<Button>(R.id.homeButton).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            val app = application as MyApplication

            // もし一時リストに商品があれば、履歴に保存する
            if (app.currentSessionList.isNotEmpty()) {
                // 現在のリストのコピーを履歴に追加
                app.addHistory(app.currentSessionList.toList())

                // 次回の返品処理のために一時リストを空にする
                app.currentSessionList.clear()

                Toast.makeText(this, "返品データを保存しました", Toast.LENGTH_SHORT).show()
            }
            startActivity(intent)
        }
        findViewById<Button>(R.id.backButton).setOnClickListener {
            val intent = Intent(this, SecondActivity::class.java)
            startActivity(intent)
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                val mediaImage = imageProxy.image
                if (mediaImage != null) {
                    val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                    recognizer.process(image)
                        .addOnSuccessListener { visionText ->
                            for (block in visionText.textBlocks) {
                                val text = block.text.trim().filter { it.isDigit() }

                                // JANコードらしい長さ（8桁〜18桁程度）の場合のみ処理
                                if (text.length >= 8 && text.length <= 18) {

                                    // 【ここがポイント：連続一致ロジック】
                                    if (text == lastReading) {
                                        readingCount++
                                    } else {
                                        lastReading = text
                                        readingCount = 1
                                    }

                                    // 指定回数連続で同じ数字だったら画面に反映
                                    if (readingCount >= REQUIRED_STABILITY) {
                                        if (currentDetectedText != text) { // 新しい数字の時だけ更新
                                            currentDetectedText = text
//                                            // --- 1. 音を鳴らす (ビープ音) ---
//                                            // MAX_VOLUME の 50% の音量で、DTMF_CONFIRM（ピッという確認音）を鳴らす
//                                            toneGenerator.startTone(ToneGenerator.TONE_DTMF_S, 200)

                                            // 前の音が鳴り終わっていなくても再生できるように、一度リセットして作成
                                            mediaPlayer?.release()
                                            mediaPlayer = MediaPlayer.create(this, R.raw.scan_number)
                                            mediaPlayer?.start()
                                            // --- バイブレーションを鳴らす ---
                                            // 1. Context を明示して Vibrator を取得
                                            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                                val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as android.os.VibratorManager
                                                vibratorManager.defaultVibrator
                                            } else {
                                                @Suppress("DEPRECATION")
                                                getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                                            }

                                            // 2. デバイスがバイブレーションに対応しているか確認
                                            if (vibrator.hasVibrator()) {
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                                    // 100ミリ秒、デフォルトの強さで実行
                                                    vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
                                                } else {
                                                    // 古い端末用（警告を抑制して使用）
                                                    @Suppress("DEPRECATION")
                                                    vibrator.vibrate(100)
                                                }
                                            }
                                            runOnUiThread {
                                                detectedTextView.text = "読み取り確定: $currentDetectedText"
                                                confirmButton.visibility = View.VISIBLE

                                            }
                                        }
                                    }
                                }
                            }
                        }
                        .addOnCompleteListener { imageProxy.close() }
                }
            }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()

                val camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis)

                // 離れると読み取り精度が上がったので、ズーム倍率を設定（1.7倍程度に固定）
                val cameraControl = camera.cameraControl
                cameraControl.setZoomRatio(1.7f)
            } catch (e: Exception) {
                Log.e("CameraInput", "Use case binding failed", e)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun allPermissionsGranted() = ContextCompat.checkSelfPermission(
        baseContext, Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
        cameraExecutor.shutdown()
//        toneGenerator.release()
    }
}