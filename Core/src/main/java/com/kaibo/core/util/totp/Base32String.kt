package com.kaibo.core.util.totp

import java.util.*

/**
 * @author kaibo
 * @date 2018/7/3 18:04
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */

object Base32String {

    private const val separator = "-"

    private val mask: Int
    private val shift: Int
    private val charMap: HashMap<Char, Int>
    private val digits: CharArray = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567".toCharArray()

    init {
        mask = digits.size - 1
        shift = Integer.numberOfTrailingZeros(digits.size)
        charMap = HashMap()
        for (i in digits.indices) {
            charMap[digits[i]] = i
        }
    }

    fun decode(encoded: String): ByteArray {
        var tempEncoded = encoded
        tempEncoded = tempEncoded.trim { it <= ' ' }.replace(separator.toRegex(), "").replace(" ".toRegex(), "")
        tempEncoded = tempEncoded.replaceFirst("[=]*$".toRegex(), "")
        tempEncoded = tempEncoded.toUpperCase(Locale.US)
        if (tempEncoded.isEmpty()) {
            return ByteArray(0)
        }
        val encodedLength = tempEncoded.length
        val outLength = encodedLength * shift / 8
        val result = ByteArray(outLength)
        var buffer = 0
        var next = 0
        var bitsLeft = 0
        for (c in tempEncoded.toCharArray()) {
            if (!charMap.containsKey(c)) {
                throw RuntimeException("Illegal character: $c")
            }
            buffer = buffer shl shift
            buffer = buffer or (charMap[c]!! and mask)
            bitsLeft += shift
            if (bitsLeft >= 8) {
                result[next++] = (buffer shr bitsLeft - 8).toByte()
                bitsLeft -= 8
            }
        }
        return result
    }
}
