package com.kaezzy.app.features.levelup

import com.kaezzy.app.core.database.entity.UserProgressEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class GamificationEngineTest {

    @Test
    fun testLevelCalculation() {
        assertEquals(1, GamificationEngine.calculateLevel(0))
        assertEquals(1, GamificationEngine.calculateLevel(99))
        assertEquals(2, GamificationEngine.calculateLevel(100))
        assertEquals(2, GamificationEngine.calculateLevel(299))
        assertEquals(3, GamificationEngine.calculateLevel(300))
        assertEquals(4, GamificationEngine.calculateLevel(600))
        assertEquals(5, GamificationEngine.calculateLevel(1000))
    }

    @Test
    fun testLevelTitle() {
        assertEquals("Level 1 — Pemula Semangat", GamificationEngine.getLevelTitle(1))
        assertEquals("Level 2 — Pejuang Konsisten", GamificationEngine.getLevelTitle(2))
        assertEquals("Level 3 — Perintis Rajin", GamificationEngine.getLevelTitle(3))
        assertEquals("Level 4 — Master Fokus", GamificationEngine.getLevelTitle(4))
        assertEquals("Level 5 — Legenda Produktif", GamificationEngine.getLevelTitle(5))
    }

    @Test
    fun testStreakEvaluation() {
        val today = "2026-10-21"
        val yesterday = "2026-10-20"
        val twoDaysAgo = "2026-10-19"

        val initial = UserProgressEntity(
            currentStreak = 3,
            longestStreak = 5,
            lastActiveDate = yesterday
        )

        // Consecutive day action -> streak +1
        val updated = GamificationEngine.evaluateStreak(initial, today, yesterday)
        assertEquals(4, updated.currentStreak)
        assertEquals(5, updated.longestStreak)

        // Broken streak action -> streak resets to 1
        val brokenInitial = UserProgressEntity(
            currentStreak = 10,
            longestStreak = 10,
            lastActiveDate = twoDaysAgo
        )
        val resetUpdated = GamificationEngine.evaluateStreak(brokenInitial, today, yesterday)
        assertEquals(1, resetUpdated.currentStreak)
        assertEquals(10, resetUpdated.longestStreak)
    }
}
