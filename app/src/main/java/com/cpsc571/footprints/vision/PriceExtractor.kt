package com.cpsc571.footprints.vision

import android.graphics.Bitmap
import com.cpsc571.footprints.entity.ItemObject
import com.google.mlkit.vision.text.Text

object PriceExtractor {
    private val totalKeywords: Array<String> = arrayOf("total", "balance due", "amount due", "due", "amount")
    private val nonItems: Array<String> = arrayOf("GST", "subtotal", "taxes", "change", "visa", "mastercard", "american express", "amax", "cash", "loyalty", "visa payment")
    private val forceReplace: Array<Pair<Regex, String>> = arrayOf(
        Pair(Regex("(\\d) ?- ?(\\d+$)"), "$1.$2"), // Receipt5 reads total as "$24 -50" rather than "$24.50"
        Pair(Regex("(\\d) ?, ?(\\d+$)"), "$1.$2") // Receipt16 reads some items as "23,43" as opposed to "23.43"
    )
    private const val minStringMatchError = 2
    class TextAndLocationTuple(var text: String?, var x: Int, var y: Int) {
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
            var fixedText = line.text
            forceReplace.forEach { pair -> fixedText =  pair.first.replace(fixedText, pair.second)}
            TextAndLocationTuple(fixedText, line.boundingBox?.left
                    ?: -1, line.boundingBox?.top ?: -1)
        }.sortedWith (tupleSorter)

        var prices = allTextLines.filter {
            val regex = Regex(".*[\\dOo]+ ?\\. ?\\.?[\\dOo][\\dOo]?\\s*\\D?$")
            it.text?.matches(regex)?:false
        }.toMutableList()

        // Try clustering algorithm maybe

        val allPairings = prices.map {
            price ->
            val matchedItem: TextAndLocationTuple = allTextLines.fold(TextAndLocationTuple(null, -1, -1)) {
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
            val productName = (pair.name?:pair.cost?:"").replace(Regex("\\d"), "").replace(Regex(":"), "").replace(Regex("\\s"), "")
            totalKeywords.any {
                keyword ->
                calculateLevenschtein(productName, keyword) <= minStringMatchError
            }
        }
        var finalPairings = pairings
        if (total != null) {
            finalPairings = pairings.subList(0, pairings.indexOf(total))

            //{Digit or "O" or "o"}+( ?)"."( ?)(.?){Digit or "O" or "o"}{Digit or "O" or "o"}{empty space}*{any non-digit}?ENDOFLINE
            val priceRegex = Regex("[\\dOo]+ ?\\. ?\\.?[\\dOo][\\dOo]?\\s*\\D?$")
            if (priceRegex.containsMatchIn(total?.cost?:"")) {
                val reg = priceRegex.find(total?.cost?:"")
                total.cost = reg?.value
            }
        }
        finalPairings.forEach {
            pair ->
            pair.cost = cleanPrice(pair.cost)
            pair.name = pair.name?:"No value associated"
        }
        return Pair(finalPairings, cleanPrice(total?.cost)?:"Not found")
    }

    private fun removeNonItems(pairings: MutableList<ItemObject>): MutableList<ItemObject> {
        pairings.removeAll {
                item ->
            nonItems.any {
                nonItemName ->
                calculateLevenschtein(item.name, nonItemName) <= minStringMatchError
            }
        }
        return pairings
    }

    private fun cleanPrice(cost: String?): String? {
        return (cost?.replace("O", "0")?.
                replace(Regex("[\\s]"), "")?.
                replace(Regex("\\D*$"), "")?.
                replace(Regex("\\.\\."), "."))
    }

    private fun calculateLevenschtein(s1: String?, s2: String): Int {
        return if (s1 == null) {
            s2.length
        } else {
            val memo =
                Array(s1.length + 1) { IntArray(s2.length + 1) }
            for (i in 0..s1.length) {
                for (j in 0..s2.length) {
                    memo[i][j] = -1
                }
            }
            levenschtein(s1, s2, 0, 0, memo)
        }
    }

    private fun levenschtein(
        s1: String,
        s2: String,
        length1: Int,
        length2: Int,
        memo: Array<IntArray>
    ): Int {
        return if (memo[length1][length2] != -1) {
            memo[length1][length2]
        } else if (s1.length == length1 || s2.length == length2) {
            val ans = Math.max(s1.length - length1, s2.length - length2)
            memo[length1][length2] = ans
            ans
        } else {
            val lev1 = if (s1[length1].equals(s2[length2], true)) levenschtein(
                s1,
                s2,
                length1 + 1,
                length2 + 1,
                memo
            ) else levenschtein(s1, s2, length1 + 1, length2 + 1, memo) + 1
            val lev2 = levenschtein(s1, s2, length1 + 1, length2, memo) + 1
            val lev3 = levenschtein(s1, s2, length1, length2 + 1, memo) + 1
            val ans = Math.min(Math.min(lev1, lev2), lev3)
            memo[length1][length2] = ans
            ans
        }
    }

}