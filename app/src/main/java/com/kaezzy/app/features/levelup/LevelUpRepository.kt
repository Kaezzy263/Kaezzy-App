package com.kaezzy.app.features.levelup

import com.kaezzy.app.core.database.dao.MissionDao
import com.kaezzy.app.core.database.dao.PersonalGoalDao
import com.kaezzy.app.core.database.dao.UserProgressDao
import com.kaezzy.app.core.database.dao.XpLedgerDao
import com.kaezzy.app.core.database.entity.MissionEntity
import com.kaezzy.app.core.database.entity.PersonalGoalEntity
import com.kaezzy.app.core.database.entity.UserProgressEntity
import com.kaezzy.app.core.database.entity.XpLedgerEntity
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class LevelUpRepository(
    private val missionDao: MissionDao,
    private val personalGoalDao: PersonalGoalDao,
    private val xpLedgerDao: XpLedgerDao,
    private val userProgressDao: UserProgressDao
) {
    fun getUserProgress(): Flow<UserProgressEntity?> = userProgressDao.getUserProgress()
    fun getXpLedger(): Flow<List<XpLedgerEntity>> = xpLedgerDao.getAllLedgerEntries()

    suspend fun getOrGenerateMissions(todayStr: String, isoWeekStr: String): Flow<List<MissionEntity>> {
        // Ensure default daily missions
        ensureDailyMissions(todayStr)
        ensureWeeklyMissions(isoWeekStr)
        return missionDao.getMissionsForPeriod(todayStr)
    }

    suspend fun ensureDailyMissions(todayStr: String) {
        val defaultMissions = listOf(
            Triple("DAILY_COMPLETE_1_TASK", "Selesaikan 1 tugas hari ini", 1 to 30),
            Triple("DAILY_COMPLETE_3_TASKS", "Selesaikan 3 tugas hari ini", 3 to 30),
            Triple("DAILY_LOG_FILL", "Isi cerita/aktivitas Daily Log hari ini", 1 to 15)
        )

        for ((code, title, countAndXp) in defaultMissions) {
            val existing = missionDao.getMission(code, todayStr)
            if (existing == null) {
                missionDao.insertMission(
                    MissionEntity(
                        missionCode = code,
                        title = title,
                        cadence = "DAILY",
                        periodKey = todayStr,
                        targetCount = countAndXp.first,
                        currentCount = 0,
                        xpReward = countAndXp.second
                    )
                )
            }
        }
    }

    suspend fun ensureWeeklyMissions(isoWeekStr: String) {
        val defaultMissions = listOf(
            Triple("WEEKLY_COMPLETE_10_TASKS", "Selesaikan 10 tugas minggu ini", 10 to 100),
            Triple("WEEKLY_LEARN_2_SKILLS", "Pelajari 2 skill minggu ini", 2 to 100)
        )

        for ((code, title, countAndXp) in defaultMissions) {
            val existing = missionDao.getMission(code, isoWeekStr)
            if (existing == null) {
                missionDao.insertMission(
                    MissionEntity(
                        missionCode = code,
                        title = title,
                        cadence = "WEEKLY",
                        periodKey = isoWeekStr,
                        targetCount = countAndXp.first,
                        currentCount = 0,
                        xpReward = countAndXp.second
                    )
                )
            }
        }
    }

    suspend fun updateMissionProgress(code: String, periodKey: String, delta: Int) {
        val mission = missionDao.getMission(code, periodKey) ?: return
        val newCount = (mission.currentCount + delta).coerceAtLeast(0)
        val isNowCompleted = newCount >= mission.targetCount
        val nowMs = System.currentTimeMillis()
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        if (isNowCompleted && mission.isCompleted == 0) {
            // Mission completed -> grant reward
            val updated = mission.copy(
                currentCount = newCount,
                isCompleted = 1,
                completedAt = nowMs,
                updatedAt = nowMs
            )
            missionDao.updateMission(updated)

            val netXp = xpLedgerDao.getNetXpForSource("MISSION", mission.id)
            if (netXp == 0) {
                xpLedgerDao.insertLedger(
                    XpLedgerEntity(
                        sourceType = "MISSION",
                        sourceId = mission.id,
                        xpDelta = mission.xpReward,
                        reason = "Misi Selesai: ${mission.title}",
                        eventDate = todayStr
                    )
                )
                grantXp(mission.xpReward)
            }
        } else if (!isNowCompleted && mission.isCompleted == 1) {
            // Rollback mission
            val updated = mission.copy(
                currentCount = newCount,
                isCompleted = 0,
                completedAt = null,
                updatedAt = nowMs
            )
            missionDao.updateMission(updated)

            val netXp = xpLedgerDao.getNetXpForSource("MISSION", mission.id)
            if (netXp > 0) {
                xpLedgerDao.insertLedger(
                    XpLedgerEntity(
                        sourceType = "REVERSAL",
                        sourceId = mission.id,
                        xpDelta = -netXp,
                        reason = "Batal Misi: ${mission.title}",
                        eventDate = todayStr
                    )
                )
                deductXp(netXp)
            }
        } else {
            val updated = mission.copy(
                currentCount = newCount,
                updatedAt = nowMs
            )
            missionDao.updateMission(updated)
        }
    }

    // Personal Goals
    fun getActiveGoals(): Flow<List<PersonalGoalEntity>> = personalGoalDao.getGoalsByStatus("ACTIVE")

    suspend fun addGoal(goal: PersonalGoalEntity) {
        personalGoalDao.insertGoal(goal)
    }

    suspend fun updateGoalProgress(goalId: String, newValue: Int) {
        val goal = personalGoalDao.getGoalById(goalId) ?: return
        val nowMs = System.currentTimeMillis()
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val isCompleted = newValue >= goal.targetValue

        val updated = goal.copy(
            currentValue = newValue,
            status = if (isCompleted) "COMPLETED" else goal.status,
            completedAt = if (isCompleted) nowMs else null,
            updatedAt = nowMs
        )
        personalGoalDao.updateGoal(updated)

        if (isCompleted && goal.status != "COMPLETED") {
            // Grant +150 XP for Goal completion
            val netXp = xpLedgerDao.getNetXpForSource("GOAL", goal.id)
            if (netXp == 0) {
                xpLedgerDao.insertLedger(
                    XpLedgerEntity(
                        sourceType = "GOAL",
                        sourceId = goal.id,
                        xpDelta = 150,
                        reason = "Target Tercapai: ${goal.title}",
                        eventDate = todayStr
                    )
                )
                grantXp(150)
            }
        }
    }

    private suspend fun grantXp(xp: Int) {
        val nowMs = System.currentTimeMillis()
        val current = userProgressDao.getUserProgressDirect() ?: UserProgressEntity()
        val newXp = current.totalXp + xp
        val newLevel = GamificationEngine.calculateLevel(newXp)
        userProgressDao.insertOrUpdate(
            current.copy(
                totalXp = newXp,
                currentLevel = newLevel,
                updatedAt = nowMs
            )
        )
    }

    private suspend fun deductXp(xp: Int) {
        val nowMs = System.currentTimeMillis()
        val current = userProgressDao.getUserProgressDirect() ?: UserProgressEntity()
        val newXp = (current.totalXp - xp).coerceAtLeast(0)
        val newLevel = GamificationEngine.calculateLevel(newXp)
        userProgressDao.insertOrUpdate(
            current.copy(
                totalXp = newXp,
                currentLevel = newLevel,
                updatedAt = nowMs
            )
        )
    }
}
