package com.cpsc571.footprints.vision

import android.graphics.Bitmap
import com.cpsc571.footprints.entity.ItemObject
import com.google.mlkit.vision.text.Text

object PriceExtractor {
    private val totalKeywords: Array<String> = arrayOf("total", "balance due", "amount due")
    private val nonItems: Array<String> = arrayOf("subtotal", "taxes", "change", "visa", "mastercard", "american express", "amax")
    class TextAndLocationTuple(var text: String, var x: Int, var y: Int) {
        fun equals(other: TextAndLocationTuple): Boolean {
            return other.x == x && other.y == y && other.text == text
        }
    }

    fun getTotalCost(bitmap: Bitmap, onSuccess: (Pair<List<ItemObject>, String>) -> Unit) {
        MLKitService.scan(bitmap) { text ->
            val pairings = findPricePairings(text)
            var pairingsAndTotal = findTotal(pairings)
            pairingsAndTotal = Pair(removeNonItems(pairingsAndTotal.first.toMutableList()), pairingsAndTotal.second)

            onSuccess(pairingsAndTotal)
        }
    }

    private fun findPricePairings(text: Text): MutableList<ItemObject> {
        var errorMargin = estimateHeightOfTextLine(text.textBlocks.flatMap {
            block ->
            block.lines
        }) / 2

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

        var allTextLines = text.textBlocks.flatMap {
            block ->
            block.lines
        }.map {
            line ->
            TextAndLocationTuple(line.text, line.boundingBox?.left
                    ?: -1, line.boundingBox?.top ?: -1)
        }.sortedWith (tupleSorter)

        var prices = allTextLines.filter {
            val regex = Regex(".*[\\dOo]+ ?\\. ?\\.?[\\dOo][\\dOo]\\s*\\D?$")
            it.text.matches(regex)
        }.toMutableList()

        // Try clustering algorithm maybe
        val averagePriceXDistance = prices.foldIndexed(0, {
            index, avg, textAndLocation ->
            (avg*index + textAndLocation.x)/(index+1)
        })

        allTextLines.forEach {
            tuple ->
            if (Math.abs(tuple.x - averagePriceXDistance) <= errorMargin
                    && prices.all { price -> !price.equals(tuple) }) {
                prices.add(tuple)
            }
        }

        prices = prices.sortedWith(tupleSorter).toMutableList()

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

    private fun estimateHeightOfTextLine(textLines: List<Text.Line>): Float {
        var lineHeight = 0f
        textLines.forEachIndexed {
            index, line ->
            if (line.boundingBox != null) {
                lineHeight = (lineHeight * index + (line.boundingBox?.bottom?:0) - (line.boundingBox?.top?:0)) / (index + 1)
            }
        }
        return lineHeight
    }

    private fun findTotal(pairings: MutableList<ItemObject>): Pair<List<ItemObject>, String> {
        val total = pairings.find { pair ->
            totalKeywords.any {
                keyword ->
                (pair.cost?.contains(keyword, true)?: false) && !(pair.cost?.contains("subtotal", true)?: true)
                        || (pair.name?.contains(keyword, true)?: false) && !(pair.name?.contains("subtotal", true)?: true)
            }
        }
        if (total != null) {
            pairings.remove(total)

            //{Digit or "O" or "o"}+( ?)"."( ?)(.?){Digit or "O" or "o"}{Digit or "O" or "o"}{empty space}*{any non-digit}?ENDOFLINE
            val priceRegex = Regex("[\\dOo]+ ?\\. ?\\.?[\\dOo][\\dOo]\\s*\\D?$")
            if (total?.cost?.matches(priceRegex) == false) {
                val reg = priceRegex.find(total?.cost?:"")
                total.cost = reg?.value
            }
        }
        pairings.map {
            pair ->
            pair.cost = cleanPrice(pair.cost)
        }.toMutableList()
        return Pair(pairings, cleanPrice(total?.cost)?:"Not found")
    }

    private fun removeNonItems(pairings: MutableList<ItemObject>): MutableList<ItemObject> {
        pairings.removeAll {
                item ->
            nonItems.any {
                    nonItemName ->
                item.cost?.contains(nonItemName, true)?:false || item.name?.contains(nonItemName, true)?:false
            }
        }
        return pairings
    }

    private fun cleanPrice(cost: String?): String? {
        return cost?.replace("O", "0")?.replace(Regex("\\s"), "")
    }
}