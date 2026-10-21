package com.kaezzy.app.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.kaezzy.app.core.database.dao.AppSettingsDao
import com.kaezzy.app.core.database.dao.DailyLogDao
import com.kaezzy.app.core.database.dao.MissionDao
import com.kaezzy.app.core.database.dao.MoneyTransactionDao
import com.kaezzy.app.core.database.dao.NoteDao
import com.kaezzy.app.core.database.dao.PersonalGoalDao
import com.kaezzy.app.core.database.dao.ReminderDao
import com.kaezzy.app.core.database.dao.SavingsGoalDao
import com.kaezzy.app.core.database.dao.SkillDao
import com.kaezzy.app.core.database.dao.TaskDao
import com.kaezzy.app.core.database.dao.UserProgressDao
import com.kaezzy.app.core.database.dao.XpLedgerDao
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

@Database(
    entities = [
        AppSettingsEntity::class,
        UserProgressEntity::class,
        TaskEntity::class,
        MissionEntity::class,
        PersonalGoalEntity::class,
        XpLedgerEntity::class,
        SavingsGoalEntity::class,
        MoneyTransactionEntity::class,
        DailyLogEntity::class,
        NoteEntity::class,
        SkillEntity::class,
        SkillEntryEntity::class,
        ReminderEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class KaezzyDatabase : RoomDatabase() {
    abstract fun appSettingsDao(): AppSettingsDao
    abstract fun userProgressDao(): UserProgressDao
    abstract fun taskDao(): TaskDao
    abstract fun missionDao(): MissionDao
    abstract fun personalGoalDao(): PersonalGoalDao
    abstract fun xpLedgerDao(): XpLedgerDao
    abstract fun savingsGoalDao(): SavingsGoalDao
    abstract fun moneyTransactionDao(): MoneyTransactionDao
    abstract fun dailyLogDao(): DailyLogDao
    abstract fun noteDao(): NoteDao
    abstract fun skillDao(): SkillDao
    abstract fun reminderDao(): ReminderDao

    companion object {
        @Volatile
        private var INSTANCE: KaezzyDatabase? = null

        fun getDatabase(context: Context): KaezzyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    KaezzyDatabase::class.java,
                    "kaezzy.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
