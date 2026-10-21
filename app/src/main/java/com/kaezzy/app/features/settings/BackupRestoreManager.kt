package com.kaezzy.app.features.settings

import com.kaezzy.app.core.copy.KaezzyMicrocopy
import com.kaezzy.app.core.database.KaezzyDatabase
import com.kaezzy.app.core.database.entity.DailyLogEntity
import com.kaezzy.app.core.database.entity.MissionEntity
import com.kaezzy.app.core.database.entity.MoneyTransactionEntity
import com.kaezzy.app.core.database.entity.NoteEntity
import com.kaezzy.app.core.database.entity.PersonalGoalEntity
import com.kaezzy.app.core.database.entity.ReminderEntity
import com.kaezzy.app.core.database.entity.SavingsGoalEntity
import com.kaezzy.app.core.database.entity.SkillEntity
import com.kaezzy.app.core.database.entity.SkillEntryEntity
import com.kaezzy.app.core.database.entity.TaskEntity
import com.kaezzy.app.core.database.entity.UserProgressEntity
import com.kaezzy.app.core.database.entity.XpLedgerEntity
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest

object BackupRestoreManager {

    fun computeSha256(input: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(input.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }

    suspend fun exportBackupJson(database: KaezzyDatabase): String {
        val root = JSONObject()

        // 1. Meta
        val meta = JSONObject().apply {
            put("app_name", "Kaezzy")
            put("schema_version", 1)
            put("exported_at", System.currentTimeMillis())
        }

        // 2. App Settings (Sanitised: DO NOT include pin_hash, pin_salt, recovery_hash, recovery_salt)
        val settings = database.appSettingsDao().getAppSettingsDirect()
        val settingsPublic = JSONObject().apply {
            put("nickname", settings?.nickname ?: "Brok")
            put("avatar_id", settings?.avatarId ?: "avatar_brok_1")
            put("lock_timeout_sec", settings?.lockTimeoutSec ?: 60)
            put("daily_log_rem_time", settings?.dailyLogRemTime ?: "21:00")
            put("is_daily_log_rem_on", settings?.isDailyLogRemOn ?: 1)
        }

        // 3. User Progress
        val progress = database.userProgressDao().getUserProgressDirect() ?: UserProgressEntity()
        val progressObj = JSONObject().apply {
            put("total_xp", progress.totalXp)
            put("current_level", progress.currentLevel)
            put("current_streak", progress.currentStreak)
            put("longest_streak", progress.longestStreak)
            put("last_active_date", progress.lastActiveDate ?: JSONObject.NULL)
        }

        root.put("app_settings_public", settingsPublic)
        root.put("user_progress", progressObj)

        // Raw json data string for checksum calculation
        val dataPayload = root.toString()
        meta.put("sha256_checksum", computeSha256(dataPayload))
        root.put("backup_meta", meta)

        return root.toString(2)
    }

    suspend fun restoreBackupJson(database: KaezzyDatabase, jsonContent: String): String? {
        try {
            val root = JSONObject(jsonContent)

            // Validate Meta & Checksum
            if (!root.has("backup_meta")) return KaezzyMicrocopy.ERROR_BACKUP_INVALID
            val meta = root.getJSONObject("backup_meta")
            val checksum = meta.optString("sha256_checksum")
            val schemaVer = meta.optInt("schema_version", 0)

            if (schemaVer != 1) return KaezzyMicrocopy.ERROR_BACKUP_INVALID

            // Re-verify payload checksum
            val copy = JSONObject(jsonContent)
            copy.remove("backup_meta")
            val expectedChecksum = computeSha256(copy.toString())

            if (checksum.isNotBlank() && !checksum.equals(expectedChecksum, ignoreCase = true)) {
                return KaezzyMicrocopy.ERROR_BACKUP_INVALID
            }

            // Restore App Settings public profile (preserving existing local PIN credentials)
            val currentSettings = database.appSettingsDao().getAppSettingsDirect()
            if (root.has("app_settings_public")) {
                val pub = root.getJSONObject("app_settings_public")
                val updatedSettings = (currentSettings ?: com.kaezzy.app.core.database.entity.AppSettingsEntity()).copy(
                    nickname = pub.optString("nickname", "Brok"),
                    avatarId = pub.optString("avatar_id", "avatar_brok_1"),
                    lockTimeoutSec = pub.optInt("lock_timeout_sec", 60),
                    dailyLogRemTime = pub.optString("daily_log_rem_time", "21:00"),
                    isDailyLogRemOn = pub.optInt("is_daily_log_rem_on", 1)
                )
                database.appSettingsDao().insertOrUpdate(updatedSettings)
            }

            // Restore User Progress
            if (root.has("user_progress")) {
                val prog = root.getJSONObject("user_progress")
                val updatedProgress = UserProgressEntity(
                    totalXp = prog.optInt("total_xp", 0),
                    currentLevel = prog.optInt("current_level", 1),
                    currentStreak = prog.optInt("current_streak", 0),
                    longestStreak = prog.optInt("longest_streak", 0),
                    lastActiveDate = if (prog.isNull("last_active_date")) null else prog.optString("last_active_date")
                )
                database.userProgressDao().insertOrUpdate(updatedProgress)
            }

            return null // Success
        } catch (_: Exception) {
            return KaezzyMicrocopy.ERROR_BACKUP_INVALID
        }
    }
}
