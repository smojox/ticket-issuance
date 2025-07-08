package com.ceo.ticketissuance.presentation.ui.camera

import android.annotation.SuppressLint
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

class TextRecognitionAnalyzer(
    private val onTextDetected: (String) -> Unit
) : ImageAnalysis.Analyzer {
    
    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    
    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(image: ImageProxy) {
        val mediaImage = image.image
        if (mediaImage != null) {
            val inputImage = InputImage.fromMediaImage(mediaImage, image.imageInfo.rotationDegrees)
            
            textRecognizer.process(inputImage)
                .addOnSuccessListener { visionText ->
                    // Process all detected text blocks
                    for (block in visionText.textBlocks) {
                        for (line in block.lines) {
                            val detectedText = line.text.trim()
                            if (detectedText.isNotBlank()) {
                                onTextDetected(detectedText)
                            }
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    // Handle text recognition failure
                    exception.printStackTrace()
                }
                .addOnCompleteListener {
                    image.close()
                }
        } else {
            image.close()
        }
    }
}