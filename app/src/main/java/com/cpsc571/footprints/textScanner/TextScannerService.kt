package com.cpsc571.footprints.textScanner

import android.graphics.Bitmap
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition

//TODO We need image to have a rotation associated with images when they get taken. User might be taking an image sideways
// See https://developers.google.com/ml-kit/vision/text-recognition/android#using-a-media.image

object TextScannerService {
    private class TextAndLocationTuple(var text: String, var x: Int, var y: Int) {}

    fun getTotalCost(bitmap: Bitmap, onSuccess: (Int) -> Unit) {
        scan(bitmap) {
            text ->
            val total = findTotalCost(text)
            onSuccess(total)
        }
    }

    private fun scan(bitmap: Bitmap, onSuccess: (Text) -> Unit) {
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

    private fun findTotalCost(text: Text): Int {
        val prices: Array<String>
        val allTextLines = text.textBlocks.flatMap {
            block ->
            block.lines
        }.map {
            line ->
            TextAndLocationTuple(line.text, line.boundingBox?.left?:-1, line.boundingBox?.top?:-1)
        }

        /*prices = allTextLines.filter {
            val regex = Regex(".*\\d+\\. ?\\.?\\d\\d\\D*")
            it.
        }.toTypedArray()
        */

        return 0
    }
}