package com.example.proj_guava

import android.content.Context
import android.graphics.Bitmap
import android.widget.Toast
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class ModelHelper(private val context: Context) {

    private var interpreter: Interpreter? = null

    private val labels = arrayOf(
        "Canker",
        "Dot",
        "Mummification",
        "Rust"
    )

    private val INPUT_SIZE = 224
    private val PIXEL_SIZE = 3
    private val BYTE_PER_FLOAT = 4

    init {
        try {
            val model = loadModelFile(context)

            val options = Interpreter.Options().apply {
                setNumThreads(4)
            }

            interpreter = Interpreter(model, options)

        } catch (e: Exception) {
            interpreter = null
            Toast.makeText(context, "⚠ Model not found", Toast.LENGTH_LONG).show()
        }
    }

    private fun loadModelFile(context: Context): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd("guava_model.tflite")
        FileInputStream(fileDescriptor.fileDescriptor).use { inputStream ->
            val fileChannel = inputStream.channel
            return fileChannel.map(
                FileChannel.MapMode.READ_ONLY,
                fileDescriptor.startOffset,
                fileDescriptor.declaredLength
            )
        }
    }

    fun predict(bitmap: Bitmap): Pair<String, Float> {

        //  SAFE CHECK
        if (interpreter == null) {
            return Pair("Model Missing", 0f)
        }

        val inputBuffer = preprocessImage(bitmap)

        val output = Array(1) { FloatArray(labels.size) }

        interpreter!!.run(inputBuffer, output)

        val probs = output[0]

        var maxIndex = 0
        var maxConfidence = probs[0]

        for (i in probs.indices) {
            if (probs[i] > maxConfidence) {
                maxConfidence = probs[i]
                maxIndex = i
            }
        }

        return Pair(labels[maxIndex], maxConfidence)
    }

    private fun preprocessImage(bitmap: Bitmap): ByteBuffer {

        val resized = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, true)

        val byteBuffer = ByteBuffer.allocateDirect(
            BYTE_PER_FLOAT * INPUT_SIZE * INPUT_SIZE * PIXEL_SIZE
        ).apply {
            order(ByteOrder.nativeOrder())
        }

        val intValues = IntArray(INPUT_SIZE * INPUT_SIZE)
        resized.getPixels(intValues, 0, INPUT_SIZE, 0, 0, INPUT_SIZE, INPUT_SIZE)

        var pixelIndex = 0

        for (i in 0 until INPUT_SIZE) {
            for (j in 0 until INPUT_SIZE) {

                val pixel = intValues[pixelIndex++]

                byteBuffer.putFloat((pixel shr 16 and 0xFF).toFloat()) // R
                byteBuffer.putFloat((pixel shr 8 and 0xFF).toFloat())  // G
                byteBuffer.putFloat((pixel and 0xFF).toFloat())        // B
            }
        }

        byteBuffer.rewind()
        return byteBuffer
    }

    fun close() {
        interpreter?.close()
    }
}