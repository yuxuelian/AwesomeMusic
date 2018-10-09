package com.kaibo.core.util.totp

import java.io.ByteArrayInputStream
import java.io.DataInputStream
import java.nio.ByteBuffer
import kotlin.experimental.and

/**
 * @author kaibo
 * @date 2018/7/3 18:04
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */

class TotpGenerator internal constructor(private val signer: Signer, private val codeLength: Int) {

    interface Signer {
        fun sign(data: ByteArray): ByteArray
    }

    init {
        if (codeLength < 0 || codeLength > MAX_TOTP_LENGTH) {
            throw IllegalArgumentException("PassCodeLength must be between 1 and $MAX_TOTP_LENGTH digits.")
        }
    }

    fun generateResponseCode(state: Long): String {
        val value = ByteBuffer.allocate(8).putLong(state).array()
        return generateResponseCode(value)
    }

    private fun padOutput(value: Int): String {
        var result = Integer.toString(value)
        for (i in result.length until codeLength) {
            result = "0$result"
        }
        return result
    }

    private fun generateResponseCode(challenge: ByteArray): String {
        val hash = signer.sign(challenge)
        val offset = hash[hash.size - 1] and 0xF
        val truncatedHash = hashToInt(hash, offset.toInt()) and 0x7FFFFFFF
        val pinValue = truncatedHash % DIGITS_POWER[codeLength]
        return padOutput(pinValue)
    }

    private fun hashToInt(bytes: ByteArray, start: Int): Int {
        return DataInputStream(ByteArrayInputStream(bytes, start, bytes.size - start)).readInt()
    }

    companion object {
        private const val MAX_TOTP_LENGTH = 9
        // 0  1   2    3     4      5       6        7         8          9
        private val DIGITS_POWER = intArrayOf(1, 10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000, 1000000000)
    }
}
