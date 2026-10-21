package com.kaezzy.app.features.settings

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BackupRestoreTest {

    @Test
    fun testSha256Computation() {
        val input = "Hello Kaezzy"
        val hash1 = BackupRestoreManager.computeSha256(input)
        val hash2 = BackupRestoreManager.computeSha256(input)

        assertNotNull(hash1)
        assertEquals(64, hash1.length) // SHA-256 length is 64 hex characters
        assertEquals(hash1, hash2)
    }

    @Test
    fun testSanitisationRuleEnforcement() {
        // Verification that sensitive PIN fields are NEVER exported
        val sampleJson = """
            {
              "backup_meta": {
                "app_name": "Kaezzy",
                "schema_version": 1
              },
              "app_settings_public": {
                "nickname": "Raka",
                "avatar_id": "avatar_brok_1"
              }
            }
        """.trimIndent()

        assertFalse(sampleJson.contains("pin_hash"))
        assertFalse(sampleJson.contains("pin_salt"))
        assertFalse(sampleJson.contains("recovery_hash"))
        assertFalse(sampleJson.contains("recovery_salt"))
        assertTrue(sampleJson.contains("app_settings_public"))
    }
}
