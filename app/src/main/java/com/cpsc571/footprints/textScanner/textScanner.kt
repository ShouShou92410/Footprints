package com.cpsc571.footprints.textScanner

import android.graphics.Bitmap
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition

//TODO We need image to have a rotation associated with images when they get taken. User might be taking an image sideways
// See https://developers.google.com/ml-kit/vision/text-recognition/android#using-a-media.image

/*
        val textScanner = TextScanner
        val test = resources.openRawResource(R.drawable.testreceipt)
        val btmp = BitmapFactory.decodeStream(test)
        val textMaybe = textScanner.scan(btmp)
 */

object TextScanner {
    fun scan(test: Bitmap) {
        val inputImg = bitMapToInputImage(test)
        val recognizer = TextRecognition.getClient()

        recognizer.process(inputImg)
                .addOnSuccessListener { visionText ->
                    visionText.toString()
                }
                .addOnFailureListener { e ->
                    Log.d("TextScanner", e.message.toString())
                }
    }

    private fun bitMapToInputImage(bitmap: Bitmap, rotateImageBy: Int = 0): InputImage {
        return InputImage.fromBitmap(bitmap, rotateImageBy)
    }
}