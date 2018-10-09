package com.kaibo.core.util.totp

import java.security.GeneralSecurityException
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * @author kaibo
 * @date 2018/7/3 18:04
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */

object LinAuthentication {

    /**
     * 默认秘钥
     */
    const val KEY = "BV6NVZYTWEKSI4E3"

    private const val startTime: Long = 0
    private const val timeStep: Long = 120

    fun getCurrentCode(secret: String = LinAuthentication.KEY): String {
        val otpState = getValueAtTime(System.currentTimeMillis() / 1000)
        return computePin(secret, otpState)
    }

    private fun computePin(secret: String?, otp_state: Long): String {
        if (secret == null || secret.isEmpty()) {
            throw IllegalArgumentException("Null or empty secret")
        }
        try {
            val signer = getSigningOracle(secret)
            val pcg = TotpGenerator(signer, 6)
            return pcg.generateResponseCode(otp_state)
        } catch (e: GeneralSecurityException) {
            throw RuntimeException("Crypto failure", e)
        }
    }

    private fun getValueAtTime(time: Long): Long {
        val timeSinceStartTime = time - startTime
        return if (timeSinceStartTime >= 0) {
            timeSinceStartTime / timeStep
        } else {
            (timeSinceStartTime - (timeStep - 1)) / timeStep
        }
    }

    private fun getSigningOracle(secret: String): TotpGenerator.Signer {
        val keyBytes = Base32String.decode(secret)
        val mac: Mac = Mac.getInstance("HMACSHA1")
        mac.init(SecretKeySpec(keyBytes, ""))
        return object : TotpGenerator.Signer {
            override fun sign(data: ByteArray): ByteArray {
                return mac.doFinal(data)
            }
        }
    }
}
