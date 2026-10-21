package com.kaezzy.app.features.levelup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaezzy.app.core.database.entity.MissionEntity
import com.kaezzy.app.core.database.entity.PersonalGoalEntity
import com.kaezzy.app.core.database.entity.UserProgressEntity
import com.kaezzy.app.core.database.entity.XpLedgerEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class LevelUpSubTab { MISSIONS, GOALS, HISTORY }

class LevelUpViewModel(private val levelUpRepository: LevelUpRepository) : ViewModel() {

    private val _selectedTab = MutableStateFlow(LevelUpSubTab.MISSIONS)
    val selectedTab: StateFlow<LevelUpSubTab> = _selectedTab.asStateFlow()

    private val _missions = MutableStateFlow<List<MissionEntity>>(emptyList())
    val missions: StateFlow<List<MissionEntity>> = _missions.asStateFlow()

    private val _goals = MutableStateFlow<List<PersonalGoalEntity>>(emptyList())
    val goals: StateFlow<List<PersonalGoalEntity>> = _goals.asStateFlow()

    private val _xpLedger = MutableStateFlow<List<XpLedgerEntity>>(emptyList())
    val xpLedger: StateFlow<List<XpLedgerEntity>> = _xpLedger.asStateFlow()

    val userProgress: StateFlow<UserProgressEntity?> = levelUpRepository.getUserProgress().stateInScope(null)

    init {
        loadData()
    }

    fun setTab(tab: LevelUpSubTab) {
        _selectedTab.value = tab
    }

    fun loadData() {
        viewModelScope.launch {
            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val cal = Calendar.getInstance()
            val isoWeekStr = "${cal.get(Calendar.YEAR)}-W${cal.get(Calendar.WEEK_OF_YEAR)}"

            levelUpRepository.getOrGenerateMissions(todayStr, isoWeekStr).collect {
                _missions.value = it
            }
        }
        viewModelScope.launch {
            levelUpRepository.getActiveGoals().collect {
                _goals.value = it
            }
        }
        viewModelScope.launch {
            levelUpRepository.getXpLedger().collect {
                _xpLedger.value = it
            }
        }
    }

    fun addGoal(
        title: String,
        horizon: String,
        targetValue: Int,
        unitLabel: String,
        targetDate: String
    ) {
        viewModelScope.launch {
            val newGoal = PersonalGoalEntity(
                title = title.trim(),
                horizon = horizon,
                targetValue = targetValue,
                unitLabel = unitLabel,
                targetDate = targetDate
            )
            levelUpRepository.addGoal(newGoal)
        }
    }

    fun updateGoalProgress(goalId: String, newValue: Int) {
        viewModelScope.launch {
            levelUpRepository.updateGoalProgress(goalId, newValue)
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
