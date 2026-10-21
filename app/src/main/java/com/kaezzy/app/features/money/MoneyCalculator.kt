package com.kaezzy.app.features.money

import com.kaezzy.app.core.database.entity.MoneyTransactionEntity
import java.text.NumberFormat
import java.util.Locale

data class BalanceSummary(
    val spendableBalance: Long,
    val totalSaved: Long,
    val totalNetBalance: Long,
    val monthlyIncome: Long,
    val monthlyExpense: Long
)

object MoneyCalculator {

    fun calculateBalances(
        allTransactions: List<MoneyTransactionEntity>,
        currentMonthYear: String? = null
    ): BalanceSummary {
        var totalIncome = 0L
        var totalExpense = 0L
        var totalAllocated = 0L
        var totalWithdrawn = 0L

        var monthlyIncome = 0L
        var monthlyExpense = 0L

        for (tx in allTransactions) {
            when (tx.type.uppercase()) {
                "INCOME" -> {
                    totalIncome += tx.amount
                    if (currentMonthYear != null && tx.transactionDate.startsWith(currentMonthYear)) {
                        monthlyIncome += tx.amount
                    }
                }
                "EXPENSE" -> {
                    totalExpense += tx.amount
                    if (currentMonthYear != null && tx.transactionDate.startsWith(currentMonthYear)) {
                        monthlyExpense += tx.amount
                    }
                }
                "SAVINGS_ALLOCATION" -> {
                    totalAllocated += tx.amount
                }
                "SAVINGS_WITHDRAWAL" -> {
                    totalWithdrawn += tx.amount
                }
            }
        }

        val spendableBalance = totalIncome - totalExpense - totalAllocated + totalWithdrawn
        val totalSaved = (totalAllocated - totalWithdrawn).coerceAtLeast(0L)
        val totalNetBalance = spendableBalance + totalSaved

        return BalanceSummary(
            spendableBalance = spendableBalance,
            totalSaved = totalSaved,
            totalNetBalance = totalNetBalance,
            monthlyIncome = monthlyIncome,
            monthlyExpense = monthlyExpense
        )
    }

    fun calculateSavingsGoalBalance(
        goalId: String,
        allTransactions: List<MoneyTransactionEntity>
    ): Long {
        var allocated = 0L
        var withdrawn = 0L
        for (tx in allTransactions) {
            if (tx.savingsGoalId == goalId) {
                if (tx.type.uppercase() == "SAVINGS_ALLOCATION") {
                    allocated += tx.amount
                } else if (tx.type.uppercase() == "SAVINGS_WITHDRAWAL") {
                    withdrawn += tx.amount
                }
            }
        }
        return (allocated - withdrawn).coerceAtLeast(0L)
    }

    fun formatRupiah(amount: Long): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale.Builder().setLanguage("id").setRegion("ID").build())
        formatter.maximumFractionDigits = 0
        return formatter.format(amount).replace("Rp", "Rp ").trim()
    }
}
