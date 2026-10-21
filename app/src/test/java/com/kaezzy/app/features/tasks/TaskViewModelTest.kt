package com.kaezzy.app.features.tasks

import com.kaezzy.app.core.database.dao.ReminderDao
import com.kaezzy.app.core.database.dao.TaskDao
import com.kaezzy.app.core.database.dao.UserProgressDao
import com.kaezzy.app.core.database.dao.XpLedgerDao
import com.kaezzy.app.core.database.entity.TaskEntity
import com.kaezzy.app.core.database.entity.UserProgressEntity
import com.kaezzy.app.core.database.entity.XpLedgerEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class FakeTaskDao : TaskDao {
    val tasks = mutableListOf<TaskEntity>()
    private val flow = MutableStateFlow<List<TaskEntity>>(emptyList())

    private fun updateFlow() {
        flow.value = tasks.toList()
    }

    override fun getAllTasks(): Flow<List<TaskEntity>> = flow

    override fun getTodoTasks(): Flow<List<TaskEntity>> = flowOf(tasks.filter { it.status == "TODO" })

    override fun getDoneTasks(): Flow<List<TaskEntity>> = flowOf(tasks.filter { it.status == "DONE" })

    override suspend fun getTaskById(id: String): TaskEntity? = tasks.find { it.id == id }

    override suspend fun insertTask(task: TaskEntity) {
        tasks.removeAll { it.id == task.id }
        tasks.add(task)
        updateFlow()
    }

    override suspend fun updateTask(task: TaskEntity) {
        insertTask(task)
    }

    override suspend fun deleteTask(task: TaskEntity) {
        tasks.removeAll { it.id == task.id }
        updateFlow()
    }

    override suspend fun deleteTaskById(id: String) {
        tasks.removeAll { it.id == id }
        updateFlow()
    }
}

class FakeXpLedgerDao : XpLedgerDao {
    val entries = mutableListOf<XpLedgerEntity>()

    override fun getAllLedgerEntries(): Flow<List<XpLedgerEntity>> = flowOf(entries)

    override suspend fun getNetXpForSource(sourceType: String, sourceId: String): Int {
        return entries.filter { it.sourceId == sourceId }.sumOf { it.xpDelta }
    }

    override suspend fun getTotalXp(): Int = entries.sumOf { it.xpDelta }

    override suspend fun insertLedger(entry: XpLedgerEntity) {
        entries.add(entry)
    }
}

class FakeUserProgressDao : UserProgressDao {
    var progress: UserProgressEntity? = UserProgressEntity()

    override fun getUserProgress(): Flow<UserProgressEntity?> = flowOf(progress)

    override suspend fun getUserProgressDirect(): UserProgressEntity? = progress

    override suspend fun insertOrUpdate(progress: UserProgressEntity) {
        this.progress = progress
    }
}

class FakeReminderDao : ReminderDao {
    override fun getActiveFutureReminders(now: Long) = flowOf(emptyList<com.kaezzy.app.core.database.entity.ReminderEntity>())
    override suspend fun getActiveFutureRemindersDirect(now: Long) = emptyList<com.kaezzy.app.core.database.entity.ReminderEntity>()
    override suspend fun getReminderForTarget(targetType: String, targetId: String) = null
    override suspend fun insertReminder(reminder: com.kaezzy.app.core.database.entity.ReminderEntity) {}
    override suspend fun updateReminder(reminder: com.kaezzy.app.core.database.entity.ReminderEntity) {}
    override suspend fun deleteReminderForTarget(targetType: String, targetId: String) {}
}

class TaskViewModelTest {

    private lateinit var taskDao: FakeTaskDao
    private lateinit var xpLedgerDao: FakeXpLedgerDao
    private lateinit var userProgressDao: FakeUserProgressDao
    private lateinit var reminderDao: FakeReminderDao
    private lateinit var repository: TaskRepository

    @Before
    fun setUp() {
        taskDao = FakeTaskDao()
        xpLedgerDao = FakeXpLedgerDao()
        userProgressDao = FakeUserProgressDao()
        reminderDao = FakeReminderDao()
        repository = TaskRepository(taskDao, xpLedgerDao, userProgressDao, reminderDao)
    }

    @Test
    fun testAddTaskAndToggleCompletionXpGrantAndReversal() = runBlocking {
        val task = TaskEntity(
            id = "task_1",
            title = "Revisi Laporan Bab 4",
            priority = "HIGH",
            status = "TODO"
        )
        repository.addTask(task)

        val inserted = repository.getTaskById("task_1")
        assertNotNull(inserted)
        assertEquals("TODO", inserted?.status)

        // Complete task -> should grant +35 XP for HIGH priority
        val xpGained = repository.toggleTaskCompletion(inserted!!)
        assertEquals(35, xpGained)

        val completedTask = repository.getTaskById("task_1")
        assertEquals("DONE", completedTask?.status)

        val progressAfterComplete = userProgressDao.getUserProgressDirect()
        assertEquals(35, progressAfterComplete?.totalXp)

        // Uncheck task -> should reverse -35 XP
        val xpOnUncheck = repository.toggleTaskCompletion(completedTask!!)
        assertEquals(0, xpOnUncheck)

        val uncheckedTask = repository.getTaskById("task_1")
        assertEquals("TODO", uncheckedTask?.status)

        val progressAfterUncheck = userProgressDao.getUserProgressDirect()
        assertEquals(0, progressAfterUncheck?.totalXp)
    }

    @Test
    fun testCancelTask() = runBlocking {
        val task = TaskEntity(
            id = "task_2",
            title = "Tugas Dibatalkan",
            status = "TODO"
        )
        repository.addTask(task)

        repository.cancelTask(task)
        val cancelled = repository.getTaskById("task_2")
        assertEquals("CANCELLED", cancelled?.status)
    }
}
