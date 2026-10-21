package com.kaezzy.app.features.skills

import com.kaezzy.app.core.database.dao.ReminderDao
import com.kaezzy.app.core.database.dao.SkillDao
import com.kaezzy.app.core.database.dao.UserProgressDao
import com.kaezzy.app.core.database.dao.XpLedgerDao
import com.kaezzy.app.core.database.entity.SkillEntity
import com.kaezzy.app.core.database.entity.SkillEntryEntity
import com.kaezzy.app.core.database.entity.UserProgressEntity
import com.kaezzy.app.core.database.entity.XpLedgerEntity
import com.kaezzy.app.features.levelup.GamificationEngine
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SkillRepository(
    private val skillDao: SkillDao,
    private val xpLedgerDao: XpLedgerDao,
    private val userProgressDao: UserProgressDao,
    private val reminderDao: ReminderDao
) {
    fun getAllSkills(): Flow<List<SkillEntity>> = skillDao.getAllSkills()
    suspend fun getSkillById(id: String): SkillEntity? = skillDao.getSkillById(id)

    fun getEntriesForSkill(skillId: String): Flow<List<SkillEntryEntity>> = skillDao.getEntriesForSkill(skillId)

    suspend fun addSkill(name: String, category: String) {
        val nowMs = System.currentTimeMillis()
        skillDao.insertSkill(
            SkillEntity(
                name = name.trim(),
                category = category.trim(),
                status = "NOT_STARTED",
                createdAt = nowMs,
                updatedAt = nowMs
            )
        )
    }

    suspend fun addSkillEntry(
        skillId: String,
        entryType: String, // NOTE, CODE_SNIPPET, REFERENCE_LINK
        title: String,
        content: String,
        codeLanguage: String? = null,
        referenceUrl: String? = null
    ) {
        val nowMs = System.currentTimeMillis()
        val entry = SkillEntryEntity(
            skillId = skillId,
            entryType = entryType,
            title = title.trim(),
            content = content,
            codeLanguage = codeLanguage,
            referenceUrl = referenceUrl,
            createdAt = nowMs,
            updatedAt = nowMs
        )
        skillDao.insertSkillEntry(entry)

        // Mark skill as studying
        markStudiedToday(skillId)
    }

    suspend fun markStudiedToday(skillId: String) {
        val skill = skillDao.getSkillById(skillId) ?: return
        val nowMs = System.currentTimeMillis()
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        val updatedSkill = skill.copy(
            status = if (skill.status == "NOT_STARTED") "LEARNING" else skill.status,
            lastStudiedAt = nowMs,
            updatedAt = nowMs
        )
        skillDao.updateSkill(updatedSkill)

        // Grant +20 XP (max 1x per skill per day)
        val sourceId = "${skill.id}_$todayStr"
        val netXp = xpLedgerDao.getNetXpForSource("SKILL_STUDY", sourceId)
        if (netXp == 0) {
            xpLedgerDao.insertLedger(
                XpLedgerEntity(
                    sourceType = "SKILL_STUDY",
                    sourceId = sourceId,
                    xpDelta = 20,
                    reason = "Belajar Skill: ${skill.name}",
                    eventDate = todayStr,
                    createdAt = nowMs
                )
            )
            val current = userProgressDao.getUserProgressDirect() ?: UserProgressEntity()
            val newXp = current.totalXp + 20
            userProgressDao.insertOrUpdate(
                current.copy(
                    totalXp = newXp,
                    currentLevel = GamificationEngine.calculateLevel(newXp),
                    updatedAt = nowMs
                )
            )
        }
    }

    suspend fun updateSkillStatus(skill: SkillEntity, newStatus: String) {
        skillDao.updateSkill(skill.copy(status = newStatus, updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteSkill(skill: SkillEntity) {
        skillDao.deleteSkill(skill)
        reminderDao.deleteReminderForTarget("SKILL_REVIEW", skill.id)
    }

    suspend fun deleteSkillEntry(entry: SkillEntryEntity) {
        skillDao.deleteSkillEntry(entry)
    }
}
