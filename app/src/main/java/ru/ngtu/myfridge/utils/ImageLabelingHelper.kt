package ru.ngtu.myfridge.utils

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabel
import com.google.mlkit.vision.label.ImageLabeler
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions

fun labelImage(bitmap: Bitmap, onResult: (List<String>) -> Unit) {
    val image = InputImage.fromBitmap(bitmap, 0)
    val labeler: ImageLabeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)
    labeler.process(image)
        .addOnSuccessListener { labels: List<ImageLabel> ->
            val result = labels.map { it.text }
            onResult(result)
        }
        .addOnFailureListener { _ ->
            onResult(emptyList<String>())
        }
}