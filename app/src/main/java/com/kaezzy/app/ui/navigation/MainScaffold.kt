package com.kaezzy.app.ui.navigation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kaezzy.app.R
import com.kaezzy.app.core.database.KaezzyDatabase
import com.kaezzy.app.core.database.entity.SkillEntity
import com.kaezzy.app.features.dailylog.DailyLogRepository
import com.kaezzy.app.features.dailylog.DailyLogScreen
import com.kaezzy.app.features.dailylog.DailyLogViewModel
import com.kaezzy.app.features.home.HomeScreen
import com.kaezzy.app.features.home.QuickAddBottomSheet
import com.kaezzy.app.features.levelup.LevelUpRepository
import com.kaezzy.app.features.levelup.LevelUpScreen
import com.kaezzy.app.features.levelup.LevelUpViewModel
import com.kaezzy.app.features.money.MoneyRepository
import com.kaezzy.app.features.money.MoneyScreen
import com.kaezzy.app.features.money.MoneyViewModel
import com.kaezzy.app.features.more.MoreScreen
import com.kaezzy.app.features.notes.NoteListScreen
import com.kaezzy.app.features.notes.NoteRepository
import com.kaezzy.app.features.notes.NoteViewModel
import com.kaezzy.app.features.settings.SettingsScreen
import com.kaezzy.app.features.settings.SettingsViewModel
import com.kaezzy.app.features.skills.SkillDetailScreen
import com.kaezzy.app.features.skills.SkillListScreen
import com.kaezzy.app.features.skills.SkillRepository
import com.kaezzy.app.features.skills.SkillViewModel
import com.kaezzy.app.features.tasks.TaskRepository
import com.kaezzy.app.features.tasks.TaskScreen
import com.kaezzy.app.features.tasks.TaskViewModel
import com.kaezzy.app.ui.theme.BackgroundPrimary
import com.kaezzy.app.ui.theme.PrimaryAccent
import com.kaezzy.app.ui.theme.SurfaceLevel1
import com.kaezzy.app.ui.theme.TextPrimary
import com.kaezzy.app.ui.theme.TextSecondary

sealed class BottomNavItem(val title: String, val icon: ImageVector) {
    object Home : BottomNavItem("Home", Icons.Default.Home)
    object Tasks : BottomNavItem("Tasks", Icons.Default.CheckCircle)
    object QuickAdd : BottomNavItem("Tambah", Icons.Default.Add)
    object LevelUp : BottomNavItem("LevelUp", Icons.Default.Star)
    object Money : BottomNavItem("Money", Icons.Default.AttachMoney)
}

enum class MainTab { HOME, TASKS, LEVELUP, MONEY, MORE }

enum class MoreSubRoute { HUB, DAILY_LOG, NOTES, SKILLS, SKILL_DETAIL, SETTINGS }

@Composable
fun MainScaffold() {
    val context = LocalContext.current
    val database = remember { KaezzyDatabase.getDatabase(context) }

    val taskRepository = remember {
        TaskRepository(
            taskDao = database.taskDao(),
            xpLedgerDao = database.xpLedgerDao(),
            userProgressDao = database.userProgressDao(),
            reminderDao = database.reminderDao()
        )
    }
    val taskViewModel = remember { TaskViewModel(taskRepository) }

    val levelUpRepository = remember {
        LevelUpRepository(
            missionDao = database.missionDao(),
            personalGoalDao = database.personalGoalDao(),
            xpLedgerDao = database.xpLedgerDao(),
            userProgressDao = database.userProgressDao()
        )
    }
    val levelUpViewModel = remember { LevelUpViewModel(levelUpRepository) }

    val moneyRepository = remember {
        MoneyRepository(
            moneyTransactionDao = database.moneyTransactionDao(),
            savingsGoalDao = database.savingsGoalDao()
        )
    }
    val moneyViewModel = remember { MoneyViewModel(moneyRepository) }

    val dailyLogRepository = remember {
        DailyLogRepository(
            dailyLogDao = database.dailyLogDao(),
            xpLedgerDao = database.xpLedgerDao(),
            userProgressDao = database.userProgressDao()
        )
    }
    val dailyLogViewModel = remember { DailyLogViewModel(dailyLogRepository) }

    val noteRepository = remember { NoteRepository(database.noteDao()) }
    val noteViewModel = remember { NoteViewModel(noteRepository) }

    val skillRepository = remember {
        SkillRepository(
            skillDao = database.skillDao(),
            xpLedgerDao = database.xpLedgerDao(),
            userProgressDao = database.userProgressDao(),
            reminderDao = database.reminderDao()
        )
    }
    val skillViewModel = remember { SkillViewModel(skillRepository) }

    val settingsViewModel = remember { SettingsViewModel(database) }

    val userProgress by database.userProgressDao().getUserProgress().collectAsState(initial = null)

    var currentTab by remember { mutableStateOf(MainTab.HOME) }
    var moreSubRoute by remember { mutableStateOf(MoreSubRoute.HUB) }
    var activeSkill by remember { mutableStateOf<SkillEntity?>(null) }
    var showQuickAddSheet by remember { mutableStateOf(false) }

    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Tasks,
        BottomNavItem.QuickAdd,
        BottomNavItem.LevelUp,
        BottomNavItem.Money
    )

    Scaffold(
        containerColor = BackgroundPrimary,
        topBar = {
            // Top Navigation Bar with App Title & Burger Nav Icon (3 lines)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceLevel1)
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.faviconk),
                        contentDescription = "Kaezzy Logo",
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Kaezzy",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryAccent
                    )
                }

                // Burger Nav Icon (Icon garis 3) -> opens More/Menu Screen
                IconButton(
                    onClick = {
                        currentTab = MainTab.MORE
                        moreSubRoute = MoreSubRoute.HUB
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Menu Navigasi (Fitur Lainnya)",
                        tint = TextPrimary
                    )
                }
            }
        },
        bottomBar = {
            // Centered Bottom Navigation Bar with FAB in the middle
            NavigationBar(
                containerColor = SurfaceLevel1,
                contentColor = TextPrimary
            ) {
                items.forEachIndexed { index, item ->
                    if (item == BottomNavItem.QuickAdd) {
                        // Center FAB button (+)
                        NavigationBarItem(
                            selected = false,
                            onClick = { showQuickAddSheet = true },
                            icon = {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(PrimaryAccent),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Tambah Cepat",
                                        tint = BackgroundPrimary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            },
                            label = { Text(text = "", fontSize = 1.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = SurfaceLevel1
                            )
                        )
                    } else {
                        val isSelected = when (index) {
                            0 -> currentTab == MainTab.HOME
                            1 -> currentTab == MainTab.TASKS
                            3 -> currentTab == MainTab.LEVELUP
                            4 -> currentTab == MainTab.MONEY
                            else -> false
                        }

                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                currentTab = when (index) {
                                    0 -> MainTab.HOME
                                    1 -> MainTab.TASKS
                                    3 -> MainTab.LEVELUP
                                    4 -> MainTab.MONEY
                                    else -> MainTab.HOME
                                }
                            },
                            icon = { Icon(imageVector = item.icon, contentDescription = item.title) },
                            label = { Text(text = item.title) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = BackgroundPrimary,
                                selectedTextColor = PrimaryAccent,
                                indicatorColor = PrimaryAccent,
                                unselectedIconColor = TextSecondary,
                                unselectedTextColor = TextSecondary
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (currentTab) {
                MainTab.HOME -> HomeScreen(
                    userProgress = userProgress,
                    taskViewModel = taskViewModel,
                    moneyViewModel = moneyViewModel,
                    dailyLogViewModel = dailyLogViewModel,
                    noteViewModel = noteViewModel,
                    skillViewModel = skillViewModel,
                    onNavigateToTasks = { currentTab = MainTab.TASKS },
                    onNavigateToMoney = { currentTab = MainTab.MONEY },
                    onNavigateToDailyLog = {
                        currentTab = MainTab.MORE
                        moreSubRoute = MoreSubRoute.DAILY_LOG
                    },
                    onNavigateToNotes = {
                        currentTab = MainTab.MORE
                        moreSubRoute = MoreSubRoute.NOTES
                    },
                    onNavigateToSkills = {
                        currentTab = MainTab.MORE
                        moreSubRoute = MoreSubRoute.SKILLS
                    },
                    onOpenQuickAdd = { showQuickAddSheet = true }
                )
                MainTab.TASKS -> TaskScreen(viewModel = taskViewModel)
                MainTab.LEVELUP -> LevelUpScreen(viewModel = levelUpViewModel)
                MainTab.MONEY -> MoneyScreen(viewModel = moneyViewModel)
                MainTab.MORE -> {
                    when (moreSubRoute) {
                        MoreSubRoute.HUB -> MoreScreen(
                            onNavigateToDailyLog = { moreSubRoute = MoreSubRoute.DAILY_LOG },
                            onNavigateToNotes = { moreSubRoute = MoreSubRoute.NOTES },
                            onNavigateToSkills = { moreSubRoute = MoreSubRoute.SKILLS },
                            onNavigateToSettings = { moreSubRoute = MoreSubRoute.SETTINGS }
                        )
                        MoreSubRoute.DAILY_LOG -> DailyLogScreen(viewModel = dailyLogViewModel)
                        MoreSubRoute.NOTES -> NoteListScreen(viewModel = noteViewModel)
                        MoreSubRoute.SKILLS -> SkillListScreen(
                            viewModel = skillViewModel,
                            onSkillClick = { skill ->
                                activeSkill = skill
                                skillViewModel.selectSkill(skill)
                                moreSubRoute = MoreSubRoute.SKILL_DETAIL
                            }
                        )
                        MoreSubRoute.SKILL_DETAIL -> {
                            val skill = activeSkill
                            if (skill != null) {
                                SkillDetailScreen(
                                    skill = skill,
                                    viewModel = skillViewModel,
                                    onBack = { moreSubRoute = MoreSubRoute.SKILLS }
                                )
                            } else {
                                moreSubRoute = MoreSubRoute.SKILLS
                            }
                        }
                        MoreSubRoute.SETTINGS -> SettingsScreen(
                            viewModel = settingsViewModel,
                            onBack = { moreSubRoute = MoreSubRoute.HUB }
                        )
                    }
                }
            }
        }
    }

    if (showQuickAddSheet) {
        val todayStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
        QuickAddBottomSheet(
            onDismiss = { showQuickAddSheet = false },
            onSaveTask = { title, description, category, priority, dueDate ->
                taskViewModel.addTask(title, description, category, priority, dueDate, null, context)
                showQuickAddSheet = false
            },
            onSaveMoney = { type, amount, category ->
                moneyViewModel.addTransaction(
                    type = type,
                    amount = amount,
                    category = category,
                    description = "Quick Add",
                    transactionDate = todayStr
                )
                showQuickAddSheet = false
            },
            onSaveLog = { content ->
                dailyLogViewModel.addActivity(null, content)
                showQuickAddSheet = false
            },
            onSaveNote = { title, content ->
                noteViewModel.saveNote(title, content, null)
                showQuickAddSheet = false
            }
        )
    }
}
