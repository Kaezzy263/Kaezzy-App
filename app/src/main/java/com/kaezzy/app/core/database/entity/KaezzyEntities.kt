package com.kaezzy.app.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "app_settings")
data class AppSettingsEntity(
    @PrimaryKey
    val id: Int = 1,
    val nickname: String = "Brok",
    @ColumnInfo(name = "avatar_id")
    val avatarId: String = "avatar_brok_1",
    @ColumnInfo(name = "is_pin_enabled")
    val isPinEnabled: Int = 0,
    @ColumnInfo(name = "pin_hash")
    val pinHash: String? = null,
    @ColumnInfo(name = "pin_salt")
    val pinSalt: String? = null,
    @ColumnInfo(name = "recovery_hash")
    val recoveryHash: String? = null,
    @ColumnInfo(name = "recovery_salt")
    val recoverySalt: String? = null,
    @ColumnInfo(name = "failed_pin_attempts")
    val failedPinAttempts: Int = 0,
    @ColumnInfo(name = "lockout_until")
    val lockoutUntil: Long? = null,
    @ColumnInfo(name = "is_biometric_enabled")
    val isBiometricEnabled: Int = 0,
    @ColumnInfo(name = "lock_timeout_sec")
    val lockTimeoutSec: Int = 60,
    @ColumnInfo(name = "daily_log_rem_time")
    val dailyLogRemTime: String = "21:00",
    @ColumnInfo(name = "is_daily_log_rem_on")
    val isDailyLogRemOn: Int = 1,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_progress")
data class UserProgressEntity(
    @PrimaryKey
    val id: Int = 1,
    @ColumnInfo(name = "total_xp")
    val totalXp: Int = 0,
    @ColumnInfo(name = "current_level")
    val currentLevel: Int = 1,
    @ColumnInfo(name = "current_streak")
    val currentStreak: Int = 0,
    @ColumnInfo(name = "longest_streak")
    val longestStreak: Int = 0,
    @ColumnInfo(name = "last_active_date")
    val lastActiveDate: String? = null,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "tasks",
    indices = [
        Index(value = ["status", "due_date", "priority"], name = "idx_tasks_status_due"),
        Index(value = ["category"], name = "idx_tasks_category")
    ]
)
data class TaskEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String = "",
    val category: String = "PRIBADI",
    val priority: String = "MEDIUM",
    val status: String = "TODO",
    @ColumnInfo(name = "due_date")
    val dueDate: String? = null,
    @ColumnInfo(name = "due_time")
    val dueTime: String? = null,
    @ColumnInfo(name = "reminder_at")
    val reminderAt: Long? = null,
    @ColumnInfo(name = "completed_at")
    val completedAt: Long? = null,
    @ColumnInfo(name = "cancelled_at")
    val cancelledAt: Long? = null,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "missions",
    indices = [
        Index(value = ["mission_code", "period_key"], unique = true)
    ]
)
data class MissionEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "mission_code")
    val missionCode: String,
    val title: String,
    val cadence: String, // DAILY, WEEKLY
    @ColumnInfo(name = "period_key")
    val periodKey: String, // YYYY-MM-DD or YYYY-Www
    @ColumnInfo(name = "target_count")
    val targetCount: Int,
    @ColumnInfo(name = "current_count")
    val currentCount: Int = 0,
    @ColumnInfo(name = "xp_reward")
    val xpReward: Int,
    @ColumnInfo(name = "is_completed")
    val isCompleted: Int = 0,
    @ColumnInfo(name = "completed_at")
    val completedAt: Long? = null,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "personal_goals",
    indices = [
        Index(value = ["status", "target_date"], name = "idx_goals_status_date")
    ]
)
data class PersonalGoalEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val horizon: String, // SHORT_TERM, LONG_TERM
    @ColumnInfo(name = "metric_type")
    val metricType: String = "NUMERIC", // NUMERIC, CHECKLIST
    @ColumnInfo(name = "target_value")
    val targetValue: Int,
    @ColumnInfo(name = "current_value")
    val currentValue: Int = 0,
    @ColumnInfo(name = "unit_label")
    val unitLabel: String,
    @ColumnInfo(name = "checklist_json")
    val checklistJson: String? = null,
    @ColumnInfo(name = "target_date")
    val targetDate: String,
    val status: String = "ACTIVE", // ACTIVE, COMPLETED, FAILED, ARCHIVED
    @ColumnInfo(name = "completed_at")
    val completedAt: Long? = null,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "xp_ledger",
    indices = [
        Index(value = ["source_id", "source_type"], name = "idx_xp_source"),
        Index(value = ["created_at"], name = "idx_xp_created")
    ]
)
data class XpLedgerEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "source_type")
    val sourceType: String, // TASK, MISSION, GOAL, DAILY_LOG, SKILL_STUDY, REVERSAL
    @ColumnInfo(name = "source_id")
    val sourceId: String,
    @ColumnInfo(name = "xp_delta")
    val xpDelta: Int,
    val reason: String,
    @ColumnInfo(name = "event_date")
    val eventDate: String,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "savings_goals")
data class SavingsGoalEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    @ColumnInfo(name = "target_amount")
    val targetAmount: Long,
    @ColumnInfo(name = "target_date")
    val targetDate: String? = null,
    val status: String = "ACTIVE", // ACTIVE, COMPLETED, ARCHIVED
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "money_transactions",
    foreignKeys = [
        ForeignKey(
            entity = SavingsGoalEntity::class,
            parentColumns = ["id"],
            childColumns = ["savings_goal_id"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["transaction_date"], name = "idx_money_tx_date"),
        Index(value = ["type", "category"], name = "idx_money_type_cat"),
        Index(value = ["savings_goal_id"], name = "idx_money_savings")
    ]
)
data class MoneyTransactionEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val type: String, // INCOME, EXPENSE, SAVINGS_ALLOCATION, SAVINGS_WITHDRAWAL
    val amount: Long,
    val category: String,
    val description: String? = null,
    @ColumnInfo(name = "transaction_date")
    val transactionDate: String,
    @ColumnInfo(name = "savings_goal_id")
    val savingsGoalId: String? = null,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "daily_logs",
    indices = [
        Index(value = ["activity_date", "activity_time"], name = "idx_logs_date_time"),
        Index(value = ["activity_date"], unique = true, name = "ux_daily_logs_summary_per_date")
    ]
)
data class DailyLogEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "activity_date")
    val activityDate: String,
    @ColumnInfo(name = "activity_time")
    val activityTime: String? = null,
    @ColumnInfo(name = "entry_kind")
    val entryKind: String, // DAILY_SUMMARY, ACTIVITY_ITEM
    val content: String,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "notes",
    indices = [
        Index(value = ["is_archived", "is_pinned", "updated_at"], name = "idx_notes_pin_archive_upd")
    ]
)
data class NoteEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val content: String,
    @ColumnInfo(name = "is_pinned")
    val isPinned: Int = 0,
    @ColumnInfo(name = "is_archived")
    val isArchived: Int = 0,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "skills",
    indices = [
        Index(value = ["status", "last_studied_at"], name = "idx_skills_status_study")
    ]
)
data class SkillEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val category: String,
    val status: String = "NOT_STARTED", // NOT_STARTED, LEARNING, MASTERED
    @ColumnInfo(name = "last_studied_at")
    val lastStudiedAt: Long? = null,
    @ColumnInfo(name = "next_review_at")
    val nextReviewAt: Long? = null,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "skill_entries",
    foreignKeys = [
        ForeignKey(
            entity = SkillEntity::class,
            parentColumns = ["id"],
            childColumns = ["skill_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["skill_id", "created_at"], name = "idx_skill_entries_skill")
    ]
)
data class SkillEntryEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "skill_id")
    val skillId: String,
    @ColumnInfo(name = "entry_type")
    val entryType: String, // NOTE, CODE_SNIPPET, REFERENCE_LINK
    val title: String,
    val content: String,
    @ColumnInfo(name = "code_language")
    val codeLanguage: String? = null,
    @ColumnInfo(name = "reference_url")
    val referenceUrl: String? = null,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "reminders",
    indices = [
        Index(value = ["is_enabled", "trigger_at"], name = "idx_reminders_active_trigger"),
        Index(value = ["target_type", "target_id"], name = "idx_reminders_target")
    ]
)
data class ReminderEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "target_type")
    val targetType: String, // TASK, DAILY_LOG, SKILL_REVIEW, GOAL
    @ColumnInfo(name = "target_id")
    val targetId: String? = null,
    val title: String,
    val body: String,
    @ColumnInfo(name = "trigger_at")
    val triggerAt: Long,
    @ColumnInfo(name = "repeat_rule")
    val repeatRule: String = "ONCE", // ONCE, DAILY, WEEKLY
    @ColumnInfo(name = "is_enabled")
    val isEnabled: Int = 1,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)
