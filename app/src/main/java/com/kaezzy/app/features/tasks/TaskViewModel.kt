package com.kaezzy.app.features.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaezzy.app.core.copy.KaezzyMicrocopy
import com.kaezzy.app.core.database.entity.TaskEntity
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

enum class TaskFilterTab { TODO, DONE, ALL }

class TaskViewModel(private val taskRepository: TaskRepository) : ViewModel() {

    private val _selectedTab = MutableStateFlow(TaskFilterTab.TODO)
    val selectedTab: StateFlow<TaskFilterTab> = _selectedTab.asStateFlow()

    private val _selectedCategory = MutableStateFlow("Semua")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _snackbarEvent = MutableSharedFlow<String>()
    val snackbarEvent: SharedFlow<String> = _snackbarEvent.asSharedFlow()

    val filteredTasks: StateFlow<List<TaskEntity>> = combine(
        taskRepository.getAllTasks(),
        _selectedTab,
        _selectedCategory,
        _searchQuery
    ) { allTasks, tab, category, query ->
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        allTasks.filter { task ->
            val matchTab = when (tab) {
                TaskFilterTab.ALL -> true
                TaskFilterTab.TODO -> task.status == "TODO"
                TaskFilterTab.DONE -> task.status == "DONE"
            }
            val matchCategory = category == "Semua" || task.category.equals(category, ignoreCase = true)
            val matchQuery = query.isBlank() ||
                    task.title.contains(query, ignoreCase = true) ||
                    task.description.contains(query, ignoreCase = true)

            matchTab && matchCategory && matchQuery
        }.sortedWith { a, b ->
            // Sorting Logic:
            // 1. Overdue tasks (status TODO, due_date < today) come first
            // 2. Nearest due date
            // 3. High priority
            // 4. Tasks without due date come last
            val isAOverdue = a.status == "TODO" && a.dueDate != null && a.dueDate < todayStr
            val isBOverdue = b.status == "TODO" && b.dueDate != null && b.dueDate < todayStr

            when {
                isAOverdue && !isBOverdue -> -1
                !isAOverdue && isBOverdue -> 1
                else -> {
                    if (a.dueDate != null && b.dueDate != null) {
                        val dateCmp = a.dueDate.compareTo(b.dueDate)
                        if (dateCmp != 0) dateCmp else priorityOrder(b.priority) - priorityOrder(a.priority)
                    } else if (a.dueDate != null) -1
                    else if (b.dueDate != null) 1
                    else priorityOrder(b.priority) - priorityOrder(a.priority)
                }
            }
        }
    }.stateInScope(emptyList())

    fun setTab(tab: TaskFilterTab) {
        _selectedTab.value = tab
    }

    fun setCategory(category: String) {
        _selectedCategory.value = category
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun addTask(
        title: String,
        description: String,
        category: String,
        priority: String,
        dueDate: String?,
        dueTime: String?,
        context: android.content.Context? = null
    ): Boolean {
        if (title.trim().isEmpty()) {
            viewModelScope.launch {
                _snackbarEvent.emit(KaezzyMicrocopy.VALIDATION_TITLE_EMPTY)
            }
            return false
        }

        viewModelScope.launch {
            val newTask = TaskEntity(
                title = title.trim(),
                description = description.trim(),
                category = category,
                priority = priority,
                dueDate = dueDate,
                dueTime = if (dueDate != null) dueTime else null
            )
            taskRepository.addTask(newTask)
            if (context != null) {
                com.kaezzy.app.core.notifications.TaskNotificationHelper.scheduleTaskReminder(context, newTask)
            }
        }
        return true
    }

    fun toggleTaskCompletion(task: TaskEntity) {
        viewModelScope.launch {
            val xpGained = taskRepository.toggleTaskCompletion(task)
            if (xpGained > 0) {
                _snackbarEvent.emit(KaezzyMicrocopy.taskDoneSuccess(xpGained))
            }
        }
    }

    fun cancelTask(task: TaskEntity) {
        viewModelScope.launch {
            taskRepository.cancelTask(task)
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            taskRepository.deleteTask(task)
        }
    }

    private fun priorityOrder(p: String): Int = when (p.uppercase()) {
        "HIGH" -> 3
        "MEDIUM" -> 2
        "LOW" -> 1
        else -> 0
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
