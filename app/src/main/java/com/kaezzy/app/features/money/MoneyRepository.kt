package com.kaezzy.app.features.money

import com.kaezzy.app.core.copy.KaezzyMicrocopy
import com.kaezzy.app.core.database.dao.MoneyTransactionDao
import com.kaezzy.app.core.database.dao.SavingsGoalDao
import com.kaezzy.app.core.database.entity.MoneyTransactionEntity
import com.kaezzy.app.core.database.entity.SavingsGoalEntity
import kotlinx.coroutines.flow.Flow

class MoneyRepository(
    private val moneyTransactionDao: MoneyTransactionDao,
    private val savingsGoalDao: SavingsGoalDao
) {
    fun getAllTransactions(): Flow<List<MoneyTransactionEntity>> = moneyTransactionDao.getAllTransactions()
    fun getTransactionsForMonth(yearMonth: String): Flow<List<MoneyTransactionEntity>> =
        moneyTransactionDao.getTransactionsForMonth(yearMonth)

    fun getActiveSavingsGoals(): Flow<List<SavingsGoalEntity>> = savingsGoalDao.getActiveSavingsGoals()

    suspend fun addTransaction(transaction: MoneyTransactionEntity): String? {
        if (transaction.amount <= 0) {
            return KaezzyMicrocopy.VALIDATION_AMOUNT_INVALID
        }

        // Validate withdrawal against savings goal balance
        if (transaction.type.uppercase() == "SAVINGS_WITHDRAWAL" && transaction.savingsGoalId != null) {
            val allocated = moneyTransactionDao.getTotalSavingsAllocated(transaction.savingsGoalId)
            val withdrawn = moneyTransactionDao.getTotalSavingsWithdrawn(transaction.savingsGoalId)
            val currentGoalBalance = allocated - withdrawn
            if (transaction.amount > currentGoalBalance) {
                return KaezzyMicrocopy.VALIDATION_SAVINGS_EXCEEDED
            }
        }

        moneyTransactionDao.insertTransaction(transaction)
        return null
    }

    suspend fun deleteTransaction(transaction: MoneyTransactionEntity): String? {
        // Non-negative Savings Guardrail:
        // If deleting a SAVINGS_ALLOCATION, ensure remaining allocated >= withdrawn
        if (transaction.type.uppercase() == "SAVINGS_ALLOCATION" && transaction.savingsGoalId != null) {
            val totalAllocated = moneyTransactionDao.getTotalSavingsAllocated(transaction.savingsGoalId)
            val totalWithdrawn = moneyTransactionDao.getTotalSavingsWithdrawn(transaction.savingsGoalId)
            val remainingAllocated = totalAllocated - transaction.amount
            if (remainingAllocated < totalWithdrawn) {
                return KaezzyMicrocopy.VALIDATION_SAVINGS_DELETE_PREVENTED
            }
        }

        moneyTransactionDao.deleteTransaction(transaction)
        return null
    }

    suspend fun addSavingsGoal(goal: SavingsGoalEntity): String? {
        if (goal.targetAmount <= 0) {
            return KaezzyMicrocopy.VALIDATION_AMOUNT_INVALID
        }
        savingsGoalDao.insertSavingsGoal(goal)
        return null
    }

    suspend fun updateSavingsGoal(goal: SavingsGoalEntity) {
        savingsGoalDao.updateSavingsGoal(goal)
    }
}
