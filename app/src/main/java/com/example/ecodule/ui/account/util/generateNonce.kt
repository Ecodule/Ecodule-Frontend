package com.example.ecodule.ui.account.util

import java.security.SecureRandom
import java.util.Base64

//This function is used to generate a secure nonce to pass in with our request
fun generateSecureRandomNonce(byteLength: Int = 32): String {
    val randomBytes = ByteArray(byteLength)
    SecureRandom.getInstanceStrong().nextBytes(randomBytes)
    return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes)
}