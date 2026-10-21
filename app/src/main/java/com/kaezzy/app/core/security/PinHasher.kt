package com.kaezzy.app.core.security

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

object PinHasher {
    fun generateSalt(): String {
        val random = SecureRandom()
        val saltBytes = ByteArray(16)
        random.nextBytes(saltBytes)
        return Base64.getEncoder().encodeToString(saltBytes)
    }

    fun hashPin(pin: String, salt: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        md.update(salt.toByteArray())
        val digest = md.digest(pin.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }

    fun hashRecoveryPassphrase(passphrase: String, salt: String): String {
        val normalized = passphrase.trim().lowercase()
        return hashPin(normalized, salt)
    }

    fun verifyPin(pin: String, salt: String, expectedHash: String): Boolean {
        val computed = hashPin(pin, salt)
        return computed.equals(expectedHash, ignoreCase = true)
    }

    fun verifyRecoveryPassphrase(passphrase: String, salt: String, expectedHash: String): Boolean {
        val normalized = passphrase.trim().lowercase()
        return verifyPin(normalized, salt, expectedHash)
    }
}
