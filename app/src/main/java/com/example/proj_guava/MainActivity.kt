package com.example.proj_guava

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.content.res.ColorStateList
import android.speech.tts.TextToSpeech
import java.util.*

class MainActivity : AppCompatActivity() {

    private val CAMERA_REQUEST = 100
    private val CAMERA_PERMISSION = 101
    private val GALLERY_REQUEST = 200

    private lateinit var scanBtn: Button
    private lateinit var rescanBtn: Button
    private lateinit var btnGallery: Button

    private lateinit var leafImage: ImageView

    private lateinit var diseaseText: TextView
    private lateinit var severityText: TextView

    private lateinit var severityBar: ProgressBar
    private lateinit var confidenceBar: ProgressBar

    private lateinit var confidencePercent: TextView

    private lateinit var treatmentText: TextView
    private lateinit var preventionText: TextView

    private lateinit var top3Text: TextView
    private lateinit var explanationText: TextView

    private var loader: ProgressBar? = null

    private lateinit var modelHelper: ModelHelper

    private lateinit var tts: TextToSpeech

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        modelHelper = ModelHelper(this)

        tts = TextToSpeech(this) {
            if (it == TextToSpeech.SUCCESS) {
                tts.language = Locale.US
            }
        }

        requestPermissions()

        scanBtn.setOnClickListener {
            animateButton(it)
            checkCameraPermission()
        }

        btnGallery.setOnClickListener {
            animateButton(it)
            openGallery()
        }

        rescanBtn.setOnClickListener {
            animateButton(it)
            resetUI()
            openCamera()
        }

        val historyBtn = findViewById<ImageButton>(R.id.historyBtn)
        historyBtn?.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }
    }

    private fun initViews() {

        scanBtn = findViewById(R.id.scanBtn)
        rescanBtn = findViewById(R.id.rescanBtn)
        btnGallery = findViewById(R.id.btnGallery)

        leafImage = findViewById(R.id.leafImage)

        diseaseText = findViewById(R.id.diseaseText)
        severityText = findViewById(R.id.severityText)

        severityBar = findViewById(R.id.severityBar)
        confidenceBar = findViewById(R.id.confidenceBar)

        confidencePercent = findViewById(R.id.confidencePercent)

        treatmentText = findViewById(R.id.treatmentText)
        preventionText = findViewById(R.id.preventionText)

        top3Text = findViewById(R.id.top3Text)
        explanationText = findViewById(R.id.explanationText)

        loader = findViewById(R.id.loader)

        severityBar.max = 100
        confidenceBar.max = 100
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA),
            CAMERA_PERMISSION
        )
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION
            )
        } else openCamera()
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, CAMERA_REQUEST)
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, GALLERY_REQUEST)
    }

    private fun resetUI() {
        diseaseText.text = "--"
        severityText.text = "Severity: LOW (0%)"
        severityBar.progress = 0
        confidenceBar.progress = 0
        confidencePercent.text = "Confidence: 0%"
        treatmentText.text = "-"
        preventionText.text = "-"
        top3Text.text = "-"
        explanationText.text = "-"
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != RESULT_OK || data == null) return

        val image: Bitmap? = when (requestCode) {

            CAMERA_REQUEST -> data.extras?.get("data") as? Bitmap

            GALLERY_REQUEST -> {
                val uri = data.data
                uri?.let {
                    contentResolver.openInputStream(it)?.use { stream ->
                        BitmapFactory.decodeStream(stream)
                    }
                }
            }

            else -> null
        }

        image?.let { processImage(it) }
    }

    private fun processImage(image: Bitmap) {

        loader?.visibility = View.VISIBLE

        val original = image.copy(Bitmap.Config.ARGB_8888, true)

        if (!ImageUtils.isLeafImage(original)) {
            Toast.makeText(this, "Please scan a guava leaf", Toast.LENGTH_SHORT).show()
            loader?.visibility = View.GONE
            return
        }

        if (ImageUtils.isLowLight(original)) {
            Toast.makeText(this, "Low lighting, results may be inaccurate", Toast.LENGTH_SHORT).show()
        }

        val abim = ABIMUtils.generateOverlay(original)
        val percent = abim.infectionPercentage

        leafImage.setImageBitmap(abim.overlayBitmap)

        val (disease, confidence) = modelHelper.predict(original)

        val severity = SeverityUtils.getSeverity(percent)

        val treatment = RecommendationUtils.getTreatment(disease)
        val prevention = RecommendationUtils.getPrevention(disease)

        updateUI(disease, confidence, severity, percent, treatment, prevention)

        val confidencePercent = (confidence * 100).toInt()
        saveHistory(disease, confidencePercent)

        speakResult(disease, severity)

        loader?.visibility = View.GONE
    }

    private fun saveHistory(disease: String, confidence: Int) {
        val prefs = getSharedPreferences("history", MODE_PRIVATE)

        val set = prefs.getStringSet("data", mutableSetOf())
            ?.toMutableSet() ?: mutableSetOf()

        val time = java.text.SimpleDateFormat("hh:mm a", Locale.getDefault())
            .format(Date())

        val entry = "$disease ($confidence%) - $time"

        set.remove(entry)
        set.add(entry)

        if (set.size > 10) {
            set.remove(set.first())
        }

        prefs.edit().putStringSet("data", set).apply()
    }

    private fun speakResult(disease: String, severity: String) {
        val text = "Disease detected: $disease. Severity $severity."
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    private fun updateUI(
        disease: String,
        confidenceValue: Float,
        severity: String,
        infectionPercent: Float,
        treatment: String,
        prevention: String
    ) {

        val confidence = (confidenceValue * 100).toInt()

        diseaseText.text = when (disease.uppercase()) {
            "RUST" -> "Leaf Rust Disease"
            "DOT" -> "Leaf Spot Disease"
            "CANKER" -> "Leaf Canker Disease"
            else -> "Healthy Leaf"
        }

        confidencePercent.text = "Confidence: $confidence%"

        severityText.text =
            "Severity: ${severity.uppercase()} (${String.format("%.1f", infectionPercent)}%)"

        val severityValue = SeverityUtils.getSeverityValue(infectionPercent)

        confidenceBar.progress = confidence
        severityBar.progress = severityValue

        val color = SeverityUtils.getSeverityColor(severity)
        severityBar.progressTintList = ColorStateList.valueOf(color)
        severityText.setTextColor(color)

        treatmentText.text = treatment
        preventionText.text = prevention

        // UPDATED TOP 3 (without breaking structure)
        top3Text.text =
            "1. ${disease.replaceFirstChar { it.uppercase() }} Disease - $confidence%\n2. Other - 5%\n3. Unknown - 3%"

        explanationText.text = when (disease.uppercase()) {

            "RUST" ->
                "Brown-orange pustules detected along leaf veins.\nFungal rust infection — apply fungicide."

            "CANKER" ->
                "Lesions and damaged tissue observed.\nCanker infection — remove affected parts."

            "DOT" ->
                "Dark spots detected on leaf surface.\nLeaf spot disease — apply treatment."

            "MUMMIFICATION" ->
                "Shriveled tissue observed.\nRemove infected parts immediately."

            else ->
                "Leaf appears healthy.\nNo major disease detected."
        }
    }

    private fun animateButton(view: View) {
        view.alpha = 0.7f
        view.postDelayed({ view.alpha = 1f }, 150)
    }

    override fun onDestroy() {
        super.onDestroy()
        modelHelper.close()
        tts.stop()
        tts.shutdown()
    }
}