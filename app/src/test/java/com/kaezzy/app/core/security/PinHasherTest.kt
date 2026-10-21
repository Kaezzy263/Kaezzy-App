package com.kaezzy.app.core.security

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PinHasherTest {

    @Test
    fun testGenerateSaltIsUnique() {
        val salt1 = PinHasher.generateSalt()
        val salt2 = PinHasher.generateSalt()
        assertTrue(salt1.isNotEmpty())
        assertTrue(salt2.isNotEmpty())
        assertNotEquals(salt1, salt2)
    }

    @Test
    fun testHashAndVerifyPin() {
        val pin = "123456"
        val salt = PinHasher.generateSalt()
        val hash = PinHasher.hashPin(pin, salt)

        assertTrue(PinHasher.verifyPin("123456", salt, hash))
        assertFalse(PinHasher.verifyPin("654321", salt, hash))
        assertFalse(PinHasher.verifyPin("12345", salt, hash))
    }

    @Test
    fun testHashAndVerifyRecoveryPassphraseCaseAndSpaceInsensitive() {
        val passphrase = "  Rahasia Brok 123  "
        val salt = PinHasher.generateSalt()
        val hash = PinHasher.hashRecoveryPassphrase(passphrase, salt)

        assertTrue(PinHasher.verifyRecoveryPassphrase("rahasia brok 123", salt, hash))
        assertTrue(PinHasher.verifyRecoveryPassphrase("  RAHASIA BROK 123  ", salt, hash))
        assertFalse(PinHasher.verifyRecoveryPassphrase("salah bro", salt, hash))
    }
}
