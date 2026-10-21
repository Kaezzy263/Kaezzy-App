package com.kaezzy.app.features.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kaezzy.app.core.copy.KaezzyMicrocopy
import com.kaezzy.app.core.database.entity.TaskEntity
import com.kaezzy.app.core.database.entity.UserProgressEntity
import com.kaezzy.app.features.dailylog.DailyLogViewModel
import com.kaezzy.app.features.money.BalanceSummary
import com.kaezzy.app.features.money.MoneyCalculator
import com.kaezzy.app.features.money.MoneyViewModel
import com.kaezzy.app.features.notes.NoteViewModel
import com.kaezzy.app.features.skills.SkillViewModel
import com.kaezzy.app.features.tasks.TaskViewModel
import com.kaezzy.app.ui.theme.BackgroundPrimary
import com.kaezzy.app.ui.theme.PrimaryAccent
import com.kaezzy.app.ui.theme.SecondaryAccent
import com.kaezzy.app.ui.theme.SemanticDanger
import com.kaezzy.app.ui.theme.SemanticSuccess
import com.kaezzy.app.ui.theme.SemanticWarning
import com.kaezzy.app.ui.theme.SurfaceLevel1
import com.kaezzy.app.ui.theme.SurfaceLevel2
import com.kaezzy.app.ui.theme.TextPrimary
import com.kaezzy.app.ui.theme.TextSecondary
import java.util.Calendar

@Composable
fun HomeScreen(
    userProgress: UserProgressEntity? = null,
    taskViewModel: TaskViewModel? = null,
    moneyViewModel: MoneyViewModel? = null,
    dailyLogViewModel: DailyLogViewModel? = null,
    noteViewModel: NoteViewModel? = null,
    skillViewModel: SkillViewModel? = null,
    onNavigateToTasks: () -> Unit = {},
    onNavigateToMoney: () -> Unit = {},
    onNavigateToDailyLog: () -> Unit = {},
    onNavigateToNotes: () -> Unit = {},
    onNavigateToSkills: () -> Unit = {},
    onOpenQuickAdd: () -> Unit = {}
) {
    val tasks by taskViewModel?.filteredTasks?.collectAsState() ?: remember { mutableStateOf(emptyList()) }
    val balanceSummary by moneyViewModel?.balanceSummary?.collectAsState() ?: remember {
        mutableStateOf(BalanceSummary(0, 0, 0, 0, 0))
    }
    val dailyLogs by dailyLogViewModel?.logs?.collectAsState() ?: remember { mutableStateOf(emptyList()) }
    val notes by noteViewModel?.notes?.collectAsState() ?: remember { mutableStateOf(emptyList()) }
    val skills by skillViewModel?.skills?.collectAsState() ?: remember { mutableStateOf(emptyList()) }

    val spendableBalance = balanceSummary.spendableBalance
    val isLowBalance = spendableBalance < 150_000

    val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = KaezzyMicrocopy.greetingForHour(currentHour)
    val streakDays = userProgress?.currentStreak ?: 0
    val totalXp = userProgress?.totalXp ?: 0
    val level = userProgress?.currentLevel ?: 1

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 1. Header Greetings & Streak
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = greeting,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Ayo libas prioritas lu hari ini, brok!",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                }

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceLevel2)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = "Streak",
                            tint = if (streakDays > 0) SemanticWarning else TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$streakDays Hari",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (streakDays > 0) TextPrimary else TextSecondary
                        )
                    }
                }
            }
        }

        // Level Summary Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceLevel1),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = PrimaryAccent
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Level $level",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                        Text(
                            text = "$totalXp XP",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PrimaryAccent
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val nextLevelXp = 50 * level * (level + 1)
                    val prevLevelXp = 50 * (level - 1) * level
                    val levelProgress = if (nextLevelXp > prevLevelXp) {
                        ((totalXp - prevLevelXp).toFloat() / (nextLevelXp - prevLevelXp)).coerceIn(0f, 1f)
                    } else 0f

                    LinearProgressIndicator(
                        progress = { levelProgress },
                        color = PrimaryAccent,
                        trackColor = SurfaceLevel2,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape)
                    )
                }
            }
        }

        // 2. Saldo (Sedikit perbesar, jika >= 150.000 warna hijau, jika < 150.000 warna merah)
        item {
            val cardBg = if (isLowBalance) SemanticDanger else SemanticSuccess
            val contentColor = Color.White
            val labelColor = Color.White.copy(alpha = 0.85f)

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onNavigateToMoney)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.padding(18.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(
                            imageVector = if (isLowBalance) Icons.Default.Warning else Icons.Default.Wallet,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = "Saldo Lu Sekarang",
                                fontSize = 13.sp,
                                color = labelColor,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = MoneyCalculator.formatRupiah(spendableBalance),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = contentColor
                            )
                            if (isLowBalance) {
                                Text(
                                    text = "Warning: Dana Menipis!",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Buka Keuangan",
                        tint = Color.White
                    )
                }
            }
        }

        // 3. Fokus Hari Ini
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Fokus Hari Ini",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                TextButton(onClick = onNavigateToTasks) {
                    Text("Lihat Semua", color = PrimaryAccent, fontSize = 12.sp)
                }
            }
        }

        val topTasks = tasks.take(3)
        if (topTasks.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceLevel1),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = KaezzyMicrocopy.EMPTY_HOME_TASKS,
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryAccent),
                            onClick = onOpenQuickAdd
                        ) {
                            Text("Bikin tugas pertama lu, brok!", color = BackgroundPrimary, fontSize = 12.sp)
                        }
                    }
                }
            }
        } else {
            items(topTasks, key = { it.id }) { task ->
                HomeTaskCard(
                    task = task,
                    onToggleComplete = { taskViewModel?.toggleTaskCompletion(task) },
                    onClick = onNavigateToTasks
                )
            }
        }

        // 4. Daily Log Highlight
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Daily Log Hari Ini",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                TextButton(onClick = onNavigateToDailyLog) {
                    Text("Spill Log", color = PrimaryAccent, fontSize = 12.sp)
                }
            }

            val summaryLog = dailyLogs.find { it.entryKind == "DAILY_SUMMARY" }
            val activityLogs = dailyLogs.filter { it.entryKind == "ACTIVITY_ITEM" }

            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceLevel1),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onNavigateToDailyLog)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Book,
                            contentDescription = null,
                            tint = PrimaryAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (summaryLog != null) "Cerita Hari Ini" else if (activityLogs.isNotEmpty()) "${activityLogs.size} Aktivitas Dicatat" else "Daily Log",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    if (summaryLog != null && summaryLog.content.isNotBlank()) {
                        Text(
                            text = summaryLog.content.take(120),
                            fontSize = 13.sp,
                            color = TextSecondary,
                            maxLines = 2
                        )
                    } else if (activityLogs.isNotEmpty()) {
                        val latestActivity = activityLogs.last()
                        Text(
                            text = "Terbaru: " + (latestActivity.activityTime?.let { "$it - " } ?: "") + latestActivity.content,
                            fontSize = 13.sp,
                            color = TextSecondary,
                            maxLines = 2
                        )
                    } else {
                        Text(
                            text = "Belum ada cerita hari ini, brok. Spill sekarang!",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        }

        // 5. Notes Gw Highlight
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Notes Gw",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                TextButton(onClick = onNavigateToNotes) {
                    Text("Lihat Semua", color = PrimaryAccent, fontSize = 12.sp)
                }
            }

            val topNotes = notes.take(2)
            if (topNotes.isEmpty()) {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceLevel1),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onNavigateToNotes)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.EditNote,
                            contentDescription = null,
                            tint = SecondaryAccent,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Belum ada catatan. Simpen ide random lu!",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    topNotes.forEach { note ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceLevel1),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(onClick = onNavigateToNotes)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = note.title,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                if (note.content.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = note.content.take(80),
                                        fontSize = 12.sp,
                                        color = TextSecondary,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 6. Skill Library Highlight
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Skill Library",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                TextButton(onClick = onNavigateToSkills) {
                    Text("Lihat Semua", color = PrimaryAccent, fontSize = 12.sp)
                }
            }

            val topSkills = skills.take(2)
            if (topSkills.isEmpty()) {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceLevel1),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onNavigateToSkills)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = null,
                            tint = PrimaryAccent,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Belum ada skill ditambahkan.",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    topSkills.forEach { skill ->
                        val (statusColor, statusText) = when (skill.status) {
                            "MASTERED" -> SemanticSuccess to "Dikuasai"
                            "LEARNING" -> SecondaryAccent to "Dipelajari"
                            else -> TextSecondary to "Belum Mulai"
                        }

                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceLevel1),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(onClick = onNavigateToSkills)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = skill.name,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = skill.category,
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                }
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = statusColor.copy(alpha = 0.2f))
                                ) {
                                    Text(
                                        text = statusText,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = statusColor,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeTaskCard(
    task: TaskEntity,
    onToggleComplete: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceLevel1),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            Checkbox(
                checked = task.status == "DONE",
                onCheckedChange = { onToggleComplete() },
                colors = CheckboxDefaults.colors(
                    checkedColor = SemanticSuccess,
                    uncheckedColor = TextSecondary
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = task.category,
                    fontSize = 11.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    color = PrimaryAccent
                )
            }
        }
    }
}
