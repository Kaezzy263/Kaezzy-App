package com.kaezzy.app.features.more

import com.kaezzy.app.core.database.dao.DailyLogDao
import com.kaezzy.app.core.database.dao.NoteDao
import com.kaezzy.app.core.database.dao.ReminderDao
import com.kaezzy.app.core.database.dao.SkillDao
import com.kaezzy.app.core.database.dao.UserProgressDao
import com.kaezzy.app.core.database.dao.XpLedgerDao
import com.kaezzy.app.core.database.entity.DailyLogEntity
import com.kaezzy.app.core.database.entity.NoteEntity
import com.kaezzy.app.core.database.entity.ReminderEntity
import com.kaezzy.app.core.database.entity.SkillEntity
import com.kaezzy.app.core.database.entity.SkillEntryEntity
import com.kaezzy.app.core.database.entity.UserProgressEntity
import com.kaezzy.app.core.database.entity.XpLedgerEntity
import com.kaezzy.app.features.dailylog.DailyLogRepository
import com.kaezzy.app.features.notes.NoteRepository
import com.kaezzy.app.features.skills.SkillRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TestDailyLogDao : DailyLogDao {
    val logs = mutableListOf<DailyLogEntity>()

    override fun getLogsForDate(date: String): Flow<List<DailyLogEntity>> =
        flowOf(logs.filter { it.activityDate == date })

    override suspend fun getSummaryForDate(date: String): DailyLogEntity? =
        logs.find { it.activityDate == date && it.entryKind == "DAILY_SUMMARY" }

    override suspend fun getLogById(id: String): DailyLogEntity? = logs.find { it.id == id }

    override suspend fun insertLog(log: DailyLogEntity) {
        logs.removeAll { it.id == log.id }
        logs.add(log)
    }

    override suspend fun updateLog(log: DailyLogEntity) {
        insertLog(log)
    }

    override suspend fun deleteLog(log: DailyLogEntity) {
        logs.removeAll { it.id == log.id }
    }
}

class TestNoteDao : NoteDao {
    val notes = mutableListOf<NoteEntity>()

    override fun getActiveNotes(): Flow<List<NoteEntity>> = flowOf(notes.filter { it.isArchived == 0 })
    override fun getArchivedNotes(): Flow<List<NoteEntity>> = flowOf(notes.filter { it.isArchived == 1 })
    override fun searchNotes(query: String): Flow<List<NoteEntity>> =
        flowOf(notes.filter { it.title.contains(query, true) || it.content.contains(query, true) })

    override suspend fun getNoteById(id: String): NoteEntity? = notes.find { it.id == id }

    override suspend fun insertNote(note: NoteEntity) {
        notes.removeAll { it.id == note.id }
        notes.add(note)
    }

    override suspend fun updateNote(note: NoteEntity) {
        insertNote(note)
    }

    override suspend fun deleteNote(note: NoteEntity) {
        notes.removeAll { it.id == note.id }
    }
}

class TestSkillDao : SkillDao {
    val skills = mutableListOf<SkillEntity>()
    val entries = mutableListOf<SkillEntryEntity>()

    override fun getAllSkills(): Flow<List<SkillEntity>> = flowOf(skills)
    override suspend fun getSkillById(id: String): SkillEntity? = skills.find { it.id == id }

    override suspend fun insertSkill(skill: SkillEntity) {
        skills.removeAll { it.id == skill.id }
        skills.add(skill)
    }

    override suspend fun updateSkill(skill: SkillEntity) {
        insertSkill(skill)
    }

    override suspend fun deleteSkill(skill: SkillEntity) {
        skills.removeAll { it.id == skill.id }
    }

    override fun getEntriesForSkill(skillId: String): Flow<List<SkillEntryEntity>> =
        flowOf(entries.filter { it.skillId == skillId })

    override suspend fun insertSkillEntry(entry: SkillEntryEntity) {
        entries.removeAll { it.id == entry.id }
        entries.add(entry)
    }

    override suspend fun deleteSkillEntry(entry: SkillEntryEntity) {
        entries.removeAll { it.id == entry.id }
    }
}

class TestXpLedgerDao : XpLedgerDao {
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

class TestUserProgressDao : UserProgressDao {
    var progress: UserProgressEntity? = UserProgressEntity()

    override fun getUserProgress(): Flow<UserProgressEntity?> = flowOf(progress)
    override suspend fun getUserProgressDirect(): UserProgressEntity? = progress
    override suspend fun insertOrUpdate(progress: UserProgressEntity) {
        this.progress = progress
    }
}

class TestReminderDao : ReminderDao {
    override fun getActiveFutureReminders(now: Long) = flowOf(emptyList<ReminderEntity>())
    override suspend fun getActiveFutureRemindersDirect(now: Long) = emptyList<ReminderEntity>()
    override suspend fun getReminderForTarget(targetType: String, targetId: String) = null
    override suspend fun insertReminder(reminder: ReminderEntity) {}
    override suspend fun updateReminder(reminder: ReminderEntity) {}
    override suspend fun deleteReminderForTarget(targetType: String, targetId: String) {}
}

class Phase5ViewModelTest {

    private lateinit var dailyLogDao: TestDailyLogDao
    private lateinit var noteDao: TestNoteDao
    private lateinit var skillDao: TestSkillDao
    private lateinit var xpLedgerDao: TestXpLedgerDao
    private lateinit var userProgressDao: TestUserProgressDao
    private lateinit var reminderDao: TestReminderDao

    private lateinit var dailyLogRepo: DailyLogRepository
    private lateinit var noteRepo: NoteRepository
    private lateinit var skillRepo: SkillRepository

    @Before
    fun setUp() {
        dailyLogDao = TestDailyLogDao()
        noteDao = TestNoteDao()
        skillDao = TestSkillDao()
        xpLedgerDao = TestXpLedgerDao()
        userProgressDao = TestUserProgressDao()
        reminderDao = TestReminderDao()

        dailyLogRepo = DailyLogRepository(dailyLogDao, xpLedgerDao, userProgressDao)
        noteRepo = NoteRepository(noteDao)
        skillRepo = SkillRepository(skillDao, xpLedgerDao, userProgressDao, reminderDao)
    }

    @Test
    fun testDailyLogSaveAndXpGrant() = runBlocking {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        dailyLogRepo.saveDailySummary(today, "Hari ini produktif banget!")

        val summary = dailyLogDao.getSummaryForDate(today)
        assertNotNull(summary)
        assertEquals("Hari ini produktif banget!", summary?.content)

        // Verify +15 XP granted
        val xp = userProgressDao.getUserProgressDirect()?.totalXp
        assertEquals(15, xp)
    }

    @Test
    fun testNotesSaveAndPin() = runBlocking {
        noteRepo.saveNote("Ide App", "Bikin app produktivitas offline")
        val note = noteDao.notes.first()
        assertEquals("Ide App", note.title)

        noteRepo.togglePinNote(note)
        assertEquals(1, noteDao.notes.first().isPinned)
    }

    @Test
    fun testSkillStudyXpGrant() = runBlocking {
        skillRepo.addSkill("Kotlin Coroutines", "Programming")
        val skill = skillDao.skills.first()

        skillRepo.addSkillEntry(skill.id, "CODE_SNIPPET", "launch", "viewModelScope.launch { }")

        val updatedSkill = skillDao.getSkillById(skill.id)
        assertEquals("LEARNING", updatedSkill?.status)

        // Verify +20 XP granted
        val xp = userProgressDao.getUserProgressDirect()?.totalXp
        assertEquals(20, xp)
    }
}
