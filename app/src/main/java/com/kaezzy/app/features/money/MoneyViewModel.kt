package com.kaezzy.app.features.money

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaezzy.app.core.database.entity.MoneyTransactionEntity
import com.kaezzy.app.core.database.entity.SavingsGoalEntity
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class MoneySubTab { TRANSACTIONS, SAVINGS }

class MoneyViewModel(private val repository: MoneyRepository) : ViewModel() {

    private val _selectedTab = MutableStateFlow(MoneySubTab.TRANSACTIONS)
    val selectedTab: StateFlow<MoneySubTab> = _selectedTab.asStateFlow()

    private val _selectedYearMonth = MutableStateFlow(
        SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
    )
    val selectedYearMonth: StateFlow<String> = _selectedYearMonth.asStateFlow()

    private val _snackbarEvent = MutableSharedFlow<String>()
    val snackbarEvent: SharedFlow<String> = _snackbarEvent.asSharedFlow()

    val allTransactions: StateFlow<List<MoneyTransactionEntity>> = repository.getAllTransactions().stateInScope(emptyList())
    val savingsGoals: StateFlow<List<SavingsGoalEntity>> = repository.getActiveSavingsGoals().stateInScope(emptyList())

    val balanceSummary: StateFlow<BalanceSummary> = combine(
        allTransactions,
        _selectedYearMonth
    ) { txs, month ->
        MoneyCalculator.calculateBalances(txs, month)
    }.stateInScope(BalanceSummary(0, 0, 0, 0, 0))

    fun setSubTab(tab: MoneySubTab) {
        _selectedTab.value = tab
    }

    fun setSelectedYearMonth(yearMonth: String) {
        _selectedYearMonth.value = yearMonth
    }

    fun addTransaction(
        type: String,
        amount: Long,
        category: String,
        description: String?,
        transactionDate: String,
        savingsGoalId: String? = null
    ) {
        viewModelScope.launch {
            val tx = MoneyTransactionEntity(
                type = type,
                amount = amount,
                category = category,
                description = description,
                transactionDate = transactionDate,
                savingsGoalId = savingsGoalId
            )
            val error = repository.addTransaction(tx)
            if (error != null) {
                _snackbarEvent.emit(error)
            }
        }
    }

    fun deleteTransaction(transaction: MoneyTransactionEntity) {
        viewModelScope.launch {
            val error = repository.deleteTransaction(transaction)
            if (error != null) {
                _snackbarEvent.emit(error)
            }
        }
    }

    fun addSavingsGoal(name: String, targetAmount: Long, targetDate: String?) {
        viewModelScope.launch {
            val goal = SavingsGoalEntity(
                name = name.trim(),
                targetAmount = targetAmount,
                targetDate = targetDate
            )
            val error = repository.addSavingsGoal(goal)
            if (error != null) {
                _snackbarEvent.emit(error)
            }
        }
    }

    private fun <T> kotlinx.coroutines.flow.Flow<T>.stateInScope(initialValue: T): StateFlow<T> {
        val flow = this
        val state = MutableStateFlow(initialValue)
        viewModelScope.launch {
            flow.collect { state.value = it }
        }
        return state.asStateFlow()
    }
}
