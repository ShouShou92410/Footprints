package com.cpsc571.footprints.vision

import android.graphics.Bitmap
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition

//TODO We need image to have a rotation associated with images when they get taken. User might be taking an image sideways
// See https://developers.google.com/ml-kit/vision/text-recognition/android#using-a-media.image

object MLKitService {

    fun scan(bitmap: Bitmap, onSuccess: (Text) -> Unit) {
        val inputImg = bitMapToInputImage(bitmap)
        val recognizer = TextRecognition.getClient()

        recognizer.process(inputImg)
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener { e ->
                    Log.d("TextScanner", e.message.toString())
                }
    }

    private fun bitMapToInputImage(bitmap: Bitmap, rotateImageBy: Int = 0): InputImage {
        return InputImage.fromBitmap(bitmap, rotateImageBy)
    }
}