package com.kaezzy.app.features.money

import com.kaezzy.app.core.database.entity.MoneyTransactionEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class MoneyCalculatorTest {

    @Test
    fun testBalanceCalculationsExplicitPRDRules() {
        // Given PRD AC-MONEY-001 scenario:
        // INCOME: Rp 1.000.000
        // EXPENSE: Rp 200.000
        // SAVINGS_ALLOCATION: Rp 300.000
        // Expected:
        // Spendable Balance = 1.000.000 - 200.000 - 300.000 + 0 = 500.000
        // Total Saved = 300.000
        // Total Net Balance = 500.000 + 300.000 = 800.000
        // Expense = 200.000 (Savings Allocation NOT counted as expense)

        val txs = listOf(
            MoneyTransactionEntity(id = "1", type = "INCOME", amount = 1_000_000, category = "Gaji", transactionDate = "2026-10-01"),
            MoneyTransactionEntity(id = "2", type = "EXPENSE", amount = 200_000, category = "Makan", transactionDate = "2026-10-02"),
            MoneyTransactionEntity(id = "3", type = "SAVINGS_ALLOCATION", amount = 300_000, category = "Tabungan", transactionDate = "2026-10-03")
        )

        val summary = MoneyCalculator.calculateBalances(txs, "2026-10")

        assertEquals(500_000L, summary.spendableBalance)
        assertEquals(300_000L, summary.totalSaved)
        assertEquals(800_000L, summary.totalNetBalance)
        assertEquals(1_000_000L, summary.monthlyIncome)
        assertEquals(200_000L, summary.monthlyExpense)
    }

    @Test
    fun testSavingsGoalBalanceCalculation() {
        val goalId = "goal_123"
        val txs = listOf(
            MoneyTransactionEntity(id = "1", type = "SAVINGS_ALLOCATION", amount = 200_000, category = "Tabungan", transactionDate = "2026-10-01", savingsGoalId = goalId),
            MoneyTransactionEntity(id = "2", type = "SAVINGS_WITHDRAWAL", amount = 150_000, category = "Tabungan", transactionDate = "2026-10-02", savingsGoalId = goalId)
        )

        val goalBalance = MoneyCalculator.calculateSavingsGoalBalance(goalId, txs)
        assertEquals(50_000L, goalBalance)
    }
}
