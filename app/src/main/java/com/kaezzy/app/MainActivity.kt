package com.kaezzy.app

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.kaezzy.app.core.database.KaezzyDatabase
import com.kaezzy.app.core.notifications.ReminderScheduler
import com.kaezzy.app.core.notifications.TaskNotificationHelper
import com.kaezzy.app.features.settings.LockScreen
import com.kaezzy.app.ui.navigation.MainScaffold
import com.kaezzy.app.ui.theme.BackgroundPrimary
import com.kaezzy.app.ui.theme.KaezzyTheme
import com.kaezzy.app.ui.theme.TextPrimary
import kotlinx.coroutines.delay
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
                var isSplashVisible by remember { mutableStateOf(true) }

                LaunchedEffect(Unit) {
                    delay(800)
                    isSplashVisible = false
                }

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

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(BackgroundPrimary)
                ) {
                    Crossfade(
                        targetState = isSplashVisible,
                        animationSpec = tween(durationMillis = 600),
                        label = "SplashToAppTransition"
                    ) { showingSplash ->
                        if (showingSplash) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(BackgroundPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.faviconk),
                                        contentDescription = "Kaezzy Logo",
                                        modifier = Modifier
                                            .size(120.dp)
                                            .clip(CircleShape)
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "Kaezzy",
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                }
                            }
                        } else {
                            AnimatedContent(
                                targetState = requiresLock,
                                transitionSpec = {
                                    fadeIn(animationSpec = tween(600)) togetherWith fadeOut(animationSpec = tween(600))
                                },
                                label = "LockToMainTransition"
                            ) { isLocked ->
                                if (isLocked) {
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
                                                if (settings != null) {
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
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        isUnlockedState.value = false
    }
}
