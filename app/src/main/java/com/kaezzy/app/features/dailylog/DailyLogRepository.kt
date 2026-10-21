package com.kaezzy.app.features.dailylog

import com.kaezzy.app.core.database.dao.DailyLogDao
import com.kaezzy.app.core.database.dao.UserProgressDao
import com.kaezzy.app.core.database.dao.XpLedgerDao
import com.kaezzy.app.core.database.entity.DailyLogEntity
import com.kaezzy.app.core.database.entity.UserProgressEntity
import com.kaezzy.app.core.database.entity.XpLedgerEntity
import com.kaezzy.app.features.levelup.GamificationEngine
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DailyLogRepository(
    private val dailyLogDao: DailyLogDao,
    private val xpLedgerDao: XpLedgerDao,
    private val userProgressDao: UserProgressDao
) {
    fun getLogsForDate(date: String): Flow<List<DailyLogEntity>> = dailyLogDao.getLogsForDate(date)

    suspend fun saveDailySummary(date: String, content: String) {
        val existing = dailyLogDao.getSummaryForDate(date)
        val nowMs = System.currentTimeMillis()
        if (existing != null) {
            dailyLogDao.updateLog(existing.copy(content = content.trim(), updatedAt = nowMs))
        } else {
            dailyLogDao.insertLog(
                DailyLogEntity(
                    activityDate = date,
                    entryKind = "DAILY_SUMMARY",
                    content = content.trim(),
                    createdAt = nowMs,
                    updatedAt = nowMs
                )
            )
        }
        checkGrantLogXp(date)
    }

    suspend fun addActivityItem(date: String, time: String?, content: String) {
        val nowMs = System.currentTimeMillis()
        dailyLogDao.insertLog(
            DailyLogEntity(
                activityDate = date,
                activityTime = time,
                entryKind = "ACTIVITY_ITEM",
                content = content.trim(),
                createdAt = nowMs,
                updatedAt = nowMs
            )
        )
        checkGrantLogXp(date)
    }

    suspend fun deleteLog(log: DailyLogEntity) {
        dailyLogDao.deleteLog(log)
    }

    private suspend fun checkGrantLogXp(activityDate: String) {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        if (activityDate == todayStr) {
            val sourceId = "LOG_$todayStr"
            val netXp = xpLedgerDao.getNetXpForSource("DAILY_LOG", sourceId)
            if (netXp == 0) {
                val nowMs = System.currentTimeMillis()
                xpLedgerDao.insertLedger(
                    XpLedgerEntity(
                        sourceType = "DAILY_LOG",
                        sourceId = sourceId,
                        xpDelta = 15,
                        reason = "Isi Daily Log Hari Ini",
                        eventDate = todayStr,
                        createdAt = nowMs
                    )
                )
                val current = userProgressDao.getUserProgressDirect() ?: UserProgressEntity()
                val newXp = current.totalXp + 15
                userProgressDao.insertOrUpdate(
                    current.copy(
                        totalXp = newXp,
                        currentLevel = GamificationEngine.calculateLevel(newXp),
                        updatedAt = nowMs
                    )
                )
            }
        }
    }
}
