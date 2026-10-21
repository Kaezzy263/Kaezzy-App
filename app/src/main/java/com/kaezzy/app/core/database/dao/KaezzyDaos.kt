package com.kaezzy.app.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.kaezzy.app.core.database.entity.AppSettingsEntity
import com.kaezzy.app.core.database.entity.DailyLogEntity
import com.kaezzy.app.core.database.entity.MissionEntity
import com.kaezzy.app.core.database.entity.MoneyTransactionEntity
import com.kaezzy.app.core.database.entity.NoteEntity
import com.kaezzy.app.core.database.entity.PersonalGoalEntity
import com.kaezzy.app.core.database.entity.ReminderEntity
import com.kaezzy.app.core.database.entity.SavingsGoalEntity
import com.kaezzy.app.core.database.entity.SkillEntity
import com.kaezzy.app.core.database.entity.SkillEntryEntity
import com.kaezzy.app.core.database.entity.TaskEntity
import com.kaezzy.app.core.database.entity.UserProgressEntity
import com.kaezzy.app.core.database.entity.XpLedgerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppSettingsDao {
    @Query("SELECT * FROM app_settings WHERE id = 1 LIMIT 1")
    fun getAppSettings(): Flow<AppSettingsEntity?>

    @Query("SELECT * FROM app_settings WHERE id = 1 LIMIT 1")
    suspend fun getAppSettingsDirect(): AppSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(settings: AppSettingsEntity)

    @Query("UPDATE app_settings SET failed_pin_attempts = :attempts, lockout_until = :lockoutUntil WHERE id = 1")
    suspend fun updateLockoutState(attempts: Int, lockoutUntil: Long?)

    @Query("UPDATE app_settings SET failed_pin_attempts = 0, lockout_until = NULL WHERE id = 1")
    suspend fun clearLockout()
}

@Dao
interface UserProgressDao {
    @Query("SELECT * FROM user_progress WHERE id = 1 LIMIT 1")
    fun getUserProgress(): Flow<UserProgressEntity?>

    @Query("SELECT * FROM user_progress WHERE id = 1 LIMIT 1")
    suspend fun getUserProgressDirect(): UserProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(progress: UserProgressEntity)
}

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY CASE WHEN due_date IS NULL THEN 1 ELSE 0 END, due_date ASC, priority DESC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE status = 'TODO' ORDER BY CASE WHEN due_date IS NULL THEN 1 ELSE 0 END, due_date ASC, priority DESC")
    fun getTodoTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE status = 'DONE' ORDER BY completed_at DESC")
    fun getDoneTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :id LIMIT 1")
    suspend fun getTaskById(id: String): TaskEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity)

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTaskById(id: String)
}

@Dao
interface MissionDao {
    @Query("SELECT * FROM missions WHERE period_key = :periodKey")
    fun getMissionsForPeriod(periodKey: String): Flow<List<MissionEntity>>

    @Query("SELECT * FROM missions WHERE mission_code = :code AND period_key = :periodKey LIMIT 1")
    suspend fun getMission(code: String, periodKey: String): MissionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMission(mission: MissionEntity)

    @Update
    suspend fun updateMission(mission: MissionEntity)
}

@Dao
interface PersonalGoalDao {
    @Query("SELECT * FROM personal_goals WHERE status = :status ORDER BY target_date ASC")
    fun getGoalsByStatus(status: String = "ACTIVE"): Flow<List<PersonalGoalEntity>>

    @Query("SELECT * FROM personal_goals WHERE id = :id LIMIT 1")
    suspend fun getGoalById(id: String): PersonalGoalEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: PersonalGoalEntity)

    @Update
    suspend fun updateGoal(goal: PersonalGoalEntity)
}

@Dao
interface XpLedgerDao {
    @Query("SELECT * FROM xp_ledger ORDER BY created_at DESC")
    fun getAllLedgerEntries(): Flow<List<XpLedgerEntity>>

    @Query("SELECT COALESCE(SUM(xp_delta), 0) FROM xp_ledger WHERE source_type = :sourceType AND source_id = :sourceId")
    suspend fun getNetXpForSource(sourceType: String, sourceId: String): Int

    @Query("SELECT COALESCE(SUM(xp_delta), 0) FROM xp_ledger")
    suspend fun getTotalXp(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLedger(entry: XpLedgerEntity)
}

@Dao
interface SavingsGoalDao {
    @Query("SELECT * FROM savings_goals WHERE status = 'ACTIVE' ORDER BY created_at DESC")
    fun getActiveSavingsGoals(): Flow<List<SavingsGoalEntity>>

    @Query("SELECT * FROM savings_goals WHERE id = :id LIMIT 1")
    suspend fun getSavingsGoalById(id: String): SavingsGoalEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavingsGoal(goal: SavingsGoalEntity)

    @Update
    suspend fun updateSavingsGoal(goal: SavingsGoalEntity)
}

@Dao
interface MoneyTransactionDao {
    @Query("SELECT * FROM money_transactions ORDER BY transaction_date DESC, created_at DESC")
    fun getAllTransactions(): Flow<List<MoneyTransactionEntity>>

    @Query("SELECT * FROM money_transactions WHERE transaction_date LIKE :yearMonth || '%' ORDER BY transaction_date DESC")
    fun getTransactionsForMonth(yearMonth: String): Flow<List<MoneyTransactionEntity>>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM money_transactions WHERE type = :type")
    suspend fun getTotalForType(type: String): Long

    @Query("SELECT COALESCE(SUM(amount), 0) FROM money_transactions WHERE type = 'SAVINGS_ALLOCATION' AND savings_goal_id = :goalId")
    suspend fun getTotalSavingsAllocated(goalId: String): Long

    @Query("SELECT COALESCE(SUM(amount), 0) FROM money_transactions WHERE type = 'SAVINGS_WITHDRAWAL' AND savings_goal_id = :goalId")
    suspend fun getTotalSavingsWithdrawn(goalId: String): Long

    @Query("SELECT * FROM money_transactions WHERE id = :id LIMIT 1")
    suspend fun getTransactionById(id: String): MoneyTransactionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: MoneyTransactionEntity)

    @Update
    suspend fun updateTransaction(transaction: MoneyTransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: MoneyTransactionEntity)
}

@Dao
interface DailyLogDao {
    @Query("SELECT * FROM daily_logs WHERE activity_date = :date ORDER BY CASE WHEN entry_kind = 'DAILY_SUMMARY' THEN 0 ELSE 1 END, activity_time ASC, created_at ASC")
    fun getLogsForDate(date: String): Flow<List<DailyLogEntity>>

    @Query("SELECT * FROM daily_logs WHERE activity_date = :date AND entry_kind = 'DAILY_SUMMARY' LIMIT 1")
    suspend fun getSummaryForDate(date: String): DailyLogEntity?

    @Query("SELECT * FROM daily_logs WHERE id = :id LIMIT 1")
    suspend fun getLogById(id: String): DailyLogEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: DailyLogEntity)

    @Update
    suspend fun updateLog(log: DailyLogEntity)

    @Delete
    suspend fun deleteLog(log: DailyLogEntity)
}

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes WHERE is_archived = 0 ORDER BY is_pinned DESC, updated_at DESC")
    fun getActiveNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE is_archived = 1 ORDER BY updated_at DESC")
    fun getArchivedNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE is_archived = 0 AND (title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%') ORDER BY is_pinned DESC, updated_at DESC")
    fun searchNotes(query: String): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE id = :id LIMIT 1")
    suspend fun getNoteById(id: String): NoteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity)

    @Update
    suspend fun updateNote(note: NoteEntity)

    @Delete
    suspend fun deleteNote(note: NoteEntity)
}

@Dao
interface SkillDao {
    @Query("SELECT * FROM skills ORDER BY last_studied_at DESC, created_at DESC")
    fun getAllSkills(): Flow<List<SkillEntity>>

    @Query("SELECT * FROM skills WHERE id = :id LIMIT 1")
    suspend fun getSkillById(id: String): SkillEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSkill(skill: SkillEntity)

    @Update
    suspend fun updateSkill(skill: SkillEntity)

    @Delete
    suspend fun deleteSkill(skill: SkillEntity)

    @Query("SELECT * FROM skill_entries WHERE skill_id = :skillId ORDER BY created_at DESC")
    fun getEntriesForSkill(skillId: String): Flow<List<SkillEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSkillEntry(entry: SkillEntryEntity)

    @Delete
    suspend fun deleteSkillEntry(entry: SkillEntryEntity)
}

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders WHERE is_enabled = 1 AND trigger_at > :now ORDER BY trigger_at ASC")
    fun getActiveFutureReminders(now: Long): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE is_enabled = 1 AND trigger_at > :now")
    suspend fun getActiveFutureRemindersDirect(now: Long): List<ReminderEntity>

    @Query("SELECT * FROM reminders WHERE target_type = :targetType AND target_id = :targetId LIMIT 1")
    suspend fun getReminderForTarget(targetType: String, targetId: String): ReminderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: ReminderEntity)

    @Update
    suspend fun updateReminder(reminder: ReminderEntity)

    @Query("DELETE FROM reminders WHERE target_type = :targetType AND target_id = :targetId")
    suspend fun deleteReminderForTarget(targetType: String, targetId: String)
}
