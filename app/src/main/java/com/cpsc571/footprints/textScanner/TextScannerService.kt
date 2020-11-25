package com.cpsc571.footprints.textScanner

import android.graphics.Bitmap
import android.util.Log
import com.cpsc571.footprints.entity.ItemObject
import com.cpsc571.footprints.entity.PurchaseObject
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition

//TODO We need image to have a rotation associated with images when they get taken. User might be taking an image sideways
// See https://developers.google.com/ml-kit/vision/text-recognition/android#using-a-media.image

object TextScannerService {
    private val totalKeywords: Array<String> = arrayOf("total", "balance due", "amount due")

    fun getTotalCost(bitmap: Bitmap, onSuccess: (Pair<Array<ItemObject>, String>) -> Unit) {
        scan(bitmap) {
            text ->
            val pairings = findPricePairings(text)
            val pairingsAndTotal = findTotal(pairings)

            onSuccess(pairingsAndTotal)
        }
    }

    private class TextAndLocationTuple(var text: String, var x: Int, var y: Int) {
        fun equals(other: TextAndLocationTuple): Boolean {
            return other.x == x && other.y == y && other.text == (text)
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

    private fun findPricePairings(text: Text): MutableList<ItemObject> {
        var allTextLinesWithBox = text.textBlocks.flatMap {
            block ->
            block.lines
        }

        var errorMargin = 0f
        allTextLinesWithBox.forEachIndexed {
            index, line ->
            if (line.boundingBox != null) {
                errorMargin = (errorMargin * index + (line.boundingBox?.bottom?:0) - (line.boundingBox?.top?:0)) / (index + 1)
            }
        }
        errorMargin /= 2

        var tupleSorter = Comparator<TextAndLocationTuple> { a, b ->
            if (Math.abs(a.y - b.y) <= errorMargin) {
                if (Math.abs(a.x - b.x) <= errorMargin) {
                    0
                } else {
                    if (Math.max(a.x, b.x) == a.x) 1
                    else -1
                }
            } else {
                if (Math.max(a.y, b.y) == a.y) 1
                else -1
            }
        }

        var allTextLines = allTextLinesWithBox.map {
            line ->
            TextAndLocationTuple(line.text, line.boundingBox?.left?:-1, line.boundingBox?.top?:-1)
        }.sortedWith (tupleSorter)

        var prices = allTextLines.filter {
            val regex = Regex(".*\\d+\\. ?\\.?\\d\\d\\D*")
            it.text.matches(regex)
        }.toMutableList()

        // Try clustering algorithm maybe
        val averagePriceXDistance = prices.foldIndexed(0, {
            index, avg, textAndLocation ->
            (avg*index + textAndLocation.x)/(index+1)
        })

        /*allTextLines.forEach {
            tuple ->
            if (Math.abs(tuple.x - averagePriceXDistance) <= errorMargin
                    && prices.all { price -> !price.equals(tuple) }) {
                prices.add(tuple)
            }
        }

        prices = prices.sortedWith(tupleSorter).toMutableList()
        */
        val allPairings = prices.map {
            price ->
            val matchedItem: TextAndLocationTuple = allTextLines.fold(TextAndLocationTuple("No value associated", -1, -1)) {
                original, item ->

                if (Math.abs(item.y - price.y) < errorMargin
                        && Math.abs(item.x - price.x) > errorMargin
                        && Math.abs(item.y - price.y) < Math.abs(original.y - price.y)) {
                    item
                } else original

            }
            ItemObject(matchedItem.text, price.text)
        }.toMutableList()
        return allPairings
    }

    private fun findTotal(pairings: MutableList<ItemObject>): Pair<Array<ItemObject>, String> {
        val total = pairings.find { pair ->
            totalKeywords.any {
                keyword ->
                (pair.cost?.contains(keyword, true)?: false) || (pair.name?.contains(keyword, true)?: false)
            }
        }
        if (total != null) {
            pairings.remove(total)

            val priceRegex = Regex("\\d+\\.\\d\\d\\s*.?$")
            if (total?.cost?.matches(priceRegex) == false) {
                val reg = priceRegex.find(total?.cost?:"")
                total.cost = reg?.value
            }
        }
        return Pair(pairings.toTypedArray(), total?.cost?:"Not found")
    }
}