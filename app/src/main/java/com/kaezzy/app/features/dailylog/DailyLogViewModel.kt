package com.kaezzy.app.features.dailylog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaezzy.app.core.database.entity.DailyLogEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class DailyLogViewModel(private val repository: DailyLogRepository) : ViewModel() {

    private val _selectedDate = MutableStateFlow(
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    )
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    private val _logs = MutableStateFlow<List<DailyLogEntity>>(emptyList())
    val logs: StateFlow<List<DailyLogEntity>> = _logs.asStateFlow()

    init {
        observeLogs()
    }

    fun setSelectedDate(date: String) {
        _selectedDate.value = date
        observeLogs()
    }

    fun navigateDays(daysDelta: Int) {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val current = sdf.parse(_selectedDate.value) ?: Date()
        val cal = Calendar.getInstance()
        cal.time = current
        cal.add(Calendar.DAY_OF_YEAR, daysDelta)
        _selectedDate.value = sdf.format(cal.time)
        observeLogs()
    }

    fun saveSummary(content: String) {
        viewModelScope.launch {
            repository.saveDailySummary(_selectedDate.value, content)
        }
    }

    fun addActivity(time: String?, content: String) {
        viewModelScope.launch {
            repository.addActivityItem(_selectedDate.value, time, content)
        }
    }

    fun deleteLog(log: DailyLogEntity) {
        viewModelScope.launch {
            repository.deleteLog(log)
        }
    }

    private fun observeLogs() {
        viewModelScope.launch {
            repository.getLogsForDate(_selectedDate.value).collect {
                _logs.value = it
            }
        }
    }
}
