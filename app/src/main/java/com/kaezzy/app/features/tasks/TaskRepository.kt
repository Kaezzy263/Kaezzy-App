package com.kaezzy.app.features.tasks

import com.kaezzy.app.core.database.dao.ReminderDao
import com.kaezzy.app.core.database.dao.TaskDao
import com.kaezzy.app.core.database.dao.UserProgressDao
import com.kaezzy.app.core.database.dao.XpLedgerDao
import com.kaezzy.app.core.database.entity.TaskEntity
import com.kaezzy.app.core.database.entity.UserProgressEntity
import com.kaezzy.app.core.database.entity.XpLedgerEntity
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TaskRepository(
    private val taskDao: TaskDao,
    private val xpLedgerDao: XpLedgerDao,
    private val userProgressDao: UserProgressDao,
    private val reminderDao: ReminderDao
) {
    fun getAllTasks(): Flow<List<TaskEntity>> = taskDao.getAllTasks()
    fun getTodoTasks(): Flow<List<TaskEntity>> = taskDao.getTodoTasks()
    fun getDoneTasks(): Flow<List<TaskEntity>> = taskDao.getDoneTasks()

    suspend fun getTaskById(id: String): TaskEntity? = taskDao.getTaskById(id)

    suspend fun addTask(task: TaskEntity) {
        taskDao.insertTask(task)
    }

    suspend fun updateTask(task: TaskEntity) {
        taskDao.updateTask(task.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun toggleTaskCompletion(task: TaskEntity): Int {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val nowMs = System.currentTimeMillis()

        if (task.status == "DONE") {
            // Uncheck task -> TODO
            val updatedTask = task.copy(
                status = "TODO",
                completedAt = null,
                updatedAt = nowMs
            )
            taskDao.updateTask(updatedTask)

            // State-based XP Reversal: check if XP was granted
            val netXp = xpLedgerDao.getNetXpForSource("TASK", task.id)
            if (netXp > 0) {
                // Insert reversal
                xpLedgerDao.insertLedger(
                    XpLedgerEntity(
                        sourceType = "REVERSAL",
                        sourceId = task.id,
                        xpDelta = -netXp,
                        reason = "Batal selesaikan tugas: ${task.title}",
                        eventDate = todayStr
                    )
                )

                // Deduct total XP
                val currentProgress = userProgressDao.getUserProgressDirect() ?: UserProgressEntity()
                val newXp = (currentProgress.totalXp - netXp).coerceAtLeast(0)
                val newLevel = calculateLevel(newXp)
                userProgressDao.insertOrUpdate(
                    currentProgress.copy(
                        totalXp = newXp,
                        currentLevel = newLevel,
                        updatedAt = nowMs
                    )
                )
            }
            return 0
        } else {
            // Complete task -> DONE
            val updatedTask = task.copy(
                status = "DONE",
                completedAt = nowMs,
                updatedAt = nowMs
            )
            taskDao.updateTask(updatedTask)

            // Disable active reminder for completed task
            reminderDao.deleteReminderForTarget("TASK", task.id)

            // State-based XP Grant: check if net XP for task is 0
            val netXp = xpLedgerDao.getNetXpForSource("TASK", task.id)
            if (netXp == 0) {
                val xpAmount = when (task.priority.uppercase()) {
                    "HIGH" -> 35
                    "LOW" -> 10
                    else -> 20 // MEDIUM
                }

                xpLedgerDao.insertLedger(
                    XpLedgerEntity(
                        sourceType = "TASK",
                        sourceId = task.id,
                        xpDelta = xpAmount,
                        reason = "Selesaikan tugas: ${task.title}",
                        eventDate = todayStr
                    )
                )

                // Update Total XP & Level
                val currentProgress = userProgressDao.getUserProgressDirect() ?: UserProgressEntity()
                val newXp = currentProgress.totalXp + xpAmount
                val newLevel = calculateLevel(newXp)

                // Evaluate Streak
                val lastDate = currentProgress.lastActiveDate
                var newStreak = currentProgress.currentStreak
                if (lastDate == null || lastDate != todayStr) {
                    val yesterdayStr = getYesterdayString()
                    newStreak = if (lastDate == yesterdayStr) {
                        currentProgress.currentStreak + 1
                    } else {
                        1
                    }
                }
                val longestStreak = maxOf(currentProgress.longestStreak, newStreak)

                userProgressDao.insertOrUpdate(
                    currentProgress.copy(
                        totalXp = newXp,
                        currentLevel = newLevel,
                        currentStreak = newStreak,
                        longestStreak = longestStreak,
                        lastActiveDate = todayStr,
                        updatedAt = nowMs
                    )
                )
                return xpAmount
            }
            return 0
        }
    }

    suspend fun cancelTask(task: TaskEntity) {
        val nowMs = System.currentTimeMillis()

        // If previously DONE, perform XP Reversal
        if (task.status == "DONE") {
            val netXp = xpLedgerDao.getNetXpForSource("TASK", task.id)
            if (netXp > 0) {
                val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                xpLedgerDao.insertLedger(
                    XpLedgerEntity(
                        sourceType = "REVERSAL",
                        sourceId = task.id,
                        xpDelta = -netXp,
                        reason = "Batal tugas: ${task.title}",
                        eventDate = todayStr
                    )
                )
                val currentProgress = userProgressDao.getUserProgressDirect() ?: UserProgressEntity()
                val newXp = (currentProgress.totalXp - netXp).coerceAtLeast(0)
                userProgressDao.insertOrUpdate(
                    currentProgress.copy(
                        totalXp = newXp,
                        currentLevel = calculateLevel(newXp),
                        updatedAt = nowMs
                    )
                )
            }
        }

        val cancelledTask = task.copy(
            status = "CANCELLED",
            cancelledAt = nowMs,
            updatedAt = nowMs
        )
        taskDao.updateTask(cancelledTask)
        reminderDao.deleteReminderForTarget("TASK", task.id)
    }

    suspend fun deleteTask(task: TaskEntity) {
        val nowMs = System.currentTimeMillis()
        // If completed less than 24 hours ago, do XP reversal
        if (task.status == "DONE" && task.completedAt != null) {
            val diffMs = nowMs - task.completedAt
            val diffHours = diffMs / (1000 * 60 * 60)
            if (diffHours < 24) {
                val netXp = xpLedgerDao.getNetXpForSource("TASK", task.id)
                if (netXp > 0) {
                    val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                    xpLedgerDao.insertLedger(
                        XpLedgerEntity(
                            sourceType = "REVERSAL",
                            sourceId = task.id,
                            xpDelta = -netXp,
                            reason = "Hapus tugas: ${task.title}",
                            eventDate = todayStr
                        )
                    )
                    val currentProgress = userProgressDao.getUserProgressDirect() ?: UserProgressEntity()
                    val newXp = (currentProgress.totalXp - netXp).coerceAtLeast(0)
                    userProgressDao.insertOrUpdate(
                        currentProgress.copy(
                            totalXp = newXp,
                            currentLevel = calculateLevel(newXp),
                            updatedAt = nowMs
                        )
                    )
                }
            }
        }
        taskDao.deleteTask(task)
        reminderDao.deleteReminderForTarget("TASK", task.id)
    }

    private fun calculateLevel(totalXp: Int): Int {
        // PRD Formula: Level L min XP = 50 * (L - 1) * L
        // L=1: 0..99, L=2: 100..299, L=3: 300..599, L=4: 600..999, L=5: 1000..1499
        var level = 1
        while (50 * level * (level + 1) <= totalXp) {
            level++
        }
        return level
    }

    private fun getYesterdayString(): String {
        val cal = java.util.Calendar.getInstance()
        cal.add(java.util.Calendar.DAY_OF_YEAR, -1)
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
    }
}
