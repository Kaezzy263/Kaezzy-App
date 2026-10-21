package com.kaezzy.app

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.fragment.app.FragmentActivity
import com.kaezzy.app.core.database.KaezzyDatabase
import com.kaezzy.app.core.notifications.ReminderScheduler
import com.kaezzy.app.core.notifications.TaskNotificationHelper
import com.kaezzy.app.features.settings.LockScreen
import com.kaezzy.app.ui.navigation.MainScaffold
import com.kaezzy.app.ui.theme.KaezzyTheme
import kotlinx.coroutines.launch

class MainActivity : FragmentActivity() {

    private val isUnlockedState = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        ReminderScheduler.createNotificationChannel(applicationContext)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }

        val database = KaezzyDatabase.getDatabase(applicationContext)
        val appSettingsDao = database.appSettingsDao()

        setContent {
            KaezzyTheme {
                val appSettings by appSettingsDao.getAppSettings().collectAsState(initial = null)
                val coroutineScope = rememberCoroutineScope()

                LaunchedEffect(isUnlockedState.value) {
                    if (isUnlockedState.value) {
                        coroutineScope.launch {
                            database.taskDao().getTodoTasks().collect { todoTasks ->
                                if (todoTasks.isNotEmpty()) {
                                    TaskNotificationHelper.showUncompletedTasksNotification(applicationContext, todoTasks)
                                }
                            }
                        }
                    }
                }

                LaunchedEffect(appSettings) {
                    val settings = appSettings
                    if (settings != null) {
                        if (settings.isPinEnabled == 1) {
                            window.setFlags(
                                WindowManager.LayoutParams.FLAG_SECURE,
                                WindowManager.LayoutParams.FLAG_SECURE
                            )
                        } else {
                            window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
                            isUnlockedState.value = true
                        }
                    }
                }

                val settings = appSettings
                val requiresLock = settings != null && settings.isPinEnabled == 1 && !isUnlockedState.value

                if (requiresLock) {
                    LockScreen(
                        appSettings = settings,
                        onUnlockSuccess = {
                            isUnlockedState.value = true
                        },
                        onLockoutStateChanged = { attempts, lockoutUntil ->
                            coroutineScope.launch {
                                appSettingsDao.updateLockoutState(attempts, lockoutUntil)
                            }
                        },
                        onResetPinConfirmed = {
                            coroutineScope.launch {
                                appSettingsDao.insertOrUpdate(
                                    settings.copy(
                                        isPinEnabled = 0,
                                        pinHash = null,
                                        pinSalt = null,
                                        recoveryHash = null,
                                        recoverySalt = null,
                                        failedPinAttempts = 0,
                                        lockoutUntil = null
                                    )
                                )
                            }
                            isUnlockedState.value = true
                        }
                    )
                } else {
                    MainScaffold()
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        isUnlockedState.value = false
    }
}
