package com.kaezzy.app.features.levelup

import com.kaezzy.app.core.database.entity.UserProgressEntity

object GamificationEngine {

    fun calculateLevel(totalXp: Int): Int {
        var level = 1
        while (50 * level * (level + 1) <= totalXp) {
            level++
        }
        return level
    }

    fun getLevelTitle(level: Int): String {
        return when (level) {
            1 -> "Level 1 — Pemula Semangat"
            2 -> "Level 2 — Pejuang Konsisten"
            3 -> "Level 3 — Perintis Rajin"
            4 -> "Level 4 — Master Fokus"
            5 -> "Level 5 — Legenda Produktif"
            else -> "Level $level — Master Produktif Utama"
        }
    }

    fun getXpProgressInLevel(totalXp: Int, currentLevel: Int): Pair<Int, Int> {
        val prevLevelMinXp = 50 * (currentLevel - 1) * currentLevel
        val nextLevelMinXp = 50 * currentLevel * (currentLevel + 1)
        val xpInCurrentLevel = (totalXp - prevLevelMinXp).coerceAtLeast(0)
        val xpRequiredForNext = nextLevelMinXp - prevLevelMinXp
        return Pair(xpInCurrentLevel, xpRequiredForNext)
    }

    fun evaluateStreak(
        currentProgress: UserProgressEntity,
        todayStr: String,
        yesterdayStr: String
    ): UserProgressEntity {
        val lastDate = currentProgress.lastActiveDate
        val newStreak = when {
            lastDate == todayStr -> currentProgress.currentStreak
            lastDate == yesterdayStr -> currentProgress.currentStreak + 1
            else -> 1
        }
        val longest = maxOf(currentProgress.longestStreak, newStreak)

        return currentProgress.copy(
            currentStreak = newStreak,
            longestStreak = longest,
            lastActiveDate = todayStr,
            updatedAt = System.currentTimeMillis()
        )
    }
}
