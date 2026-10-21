package com.kaezzy.app.features.levelup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kaezzy.app.core.database.entity.MissionEntity
import com.kaezzy.app.core.database.entity.PersonalGoalEntity
import com.kaezzy.app.core.database.entity.XpLedgerEntity
import com.kaezzy.app.ui.theme.BackgroundPrimary
import com.kaezzy.app.ui.theme.PrimaryAccent
import com.kaezzy.app.ui.theme.SemanticDanger
import com.kaezzy.app.ui.theme.SemanticSuccess
import com.kaezzy.app.ui.theme.SemanticWarning
import com.kaezzy.app.ui.theme.SurfaceLevel1
import com.kaezzy.app.ui.theme.SurfaceLevel2
import com.kaezzy.app.ui.theme.TextPrimary
import com.kaezzy.app.ui.theme.TextSecondary

@Composable
fun LevelUpScreen(
    viewModel: LevelUpViewModel? = null
) {
    val selectedTab by viewModel?.selectedTab?.collectAsState() ?: remember { mutableStateOf(LevelUpSubTab.MISSIONS) }
    val userProgress by viewModel?.userProgress?.collectAsState() ?: remember { mutableStateOf(null) }
    val missions by viewModel?.missions?.collectAsState() ?: remember { mutableStateOf(emptyList()) }
    val goals by viewModel?.goals?.collectAsState() ?: remember { mutableStateOf(emptyList()) }
    val xpLedger by viewModel?.xpLedger?.collectAsState() ?: remember { mutableStateOf(emptyList()) }

    var showAddGoalDialog by remember { mutableStateOf(false) }

    val totalXp = userProgress?.totalXp ?: 0
    val level = userProgress?.currentLevel ?: 1
    val title = GamificationEngine.getLevelTitle(level)
    val streak = userProgress?.currentStreak ?: 0
    val longestStreak = userProgress?.longestStreak ?: 0

    Scaffold(
        containerColor = BackgroundPrimary
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 12.dp)
            ) {
                Text(
                    text = "LevelUp",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                if (selectedTab == LevelUpSubTab.GOALS) {
                    androidx.compose.material3.IconButton(onClick = { showAddGoalDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Tambah Target",
                            tint = PrimaryAccent
                        )
                    }
                }
            }

            // Hero Profile Card
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
                        Column {
                            Text(
                                text = title,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Total XP: $totalXp XP",
                                fontSize = 13.sp,
                                color = PrimaryAccent
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = SemanticWarning)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$streak Hari (Rekor: $longestStreak)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val (xpInLevel, xpForNext) = GamificationEngine.getXpProgressInLevel(totalXp, level)
                    val progress = if (xpForNext > 0) (xpInLevel.toFloat() / xpForNext).coerceIn(0f, 1f) else 0f

                    LinearProgressIndicator(
                        progress = { progress },
                        color = PrimaryAccent,
                        trackColor = SurfaceLevel2,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sub-tabs
            TabRow(
                selectedTabIndex = selectedTab.ordinal,
                containerColor = SurfaceLevel1,
                contentColor = PrimaryAccent
            ) {
                Tab(
                    selected = selectedTab == LevelUpSubTab.MISSIONS,
                    onClick = { viewModel?.setTab(LevelUpSubTab.MISSIONS) },
                    text = { Text("Misi", color = if (selectedTab == LevelUpSubTab.MISSIONS) PrimaryAccent else TextSecondary) }
                )
                Tab(
                    selected = selectedTab == LevelUpSubTab.GOALS,
                    onClick = { viewModel?.setTab(LevelUpSubTab.GOALS) },
                    text = { Text("Target Gw", color = if (selectedTab == LevelUpSubTab.GOALS) PrimaryAccent else TextSecondary) }
                )
                Tab(
                    selected = selectedTab == LevelUpSubTab.HISTORY,
                    onClick = { viewModel?.setTab(LevelUpSubTab.HISTORY) },
                    text = { Text("Riwayat XP", color = if (selectedTab == LevelUpSubTab.HISTORY) PrimaryAccent else TextSecondary) }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            when (selectedTab) {
                LevelUpSubTab.MISSIONS -> {
                    if (missions.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Belum ada misi aktif.", color = TextSecondary, fontSize = 14.sp)
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(missions, key = { it.id }) { mission ->
                                MissionCard(mission = mission)
                            }
                        }
                    }
                }

                LevelUpSubTab.GOALS -> {
                    if (goals.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Belum ada target jangka panjang nih, brok. Pasang satu biar hidup ada kompasnya.", color = TextSecondary, fontSize = 14.sp)
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(goals, key = { it.id }) { goal ->
                                GoalCard(
                                    goal = goal,
                                    onUpdateProgress = { newProgress ->
                                        viewModel?.updateGoalProgress(goal.id, newProgress)
                                    }
                                )
                            }
                        }
                    }
                }

                LevelUpSubTab.HISTORY -> {
                    if (xpLedger.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Belum ada riwayat XP.", color = TextSecondary, fontSize = 14.sp)
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(xpLedger, key = { it.id }) { entry ->
                                XpLedgerCard(entry = entry)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddGoalDialog) {
        AddGoalDialog(
            onDismiss = { showAddGoalDialog = false },
            onSave = { title, horizon, targetValue, unitLabel, targetDate ->
                viewModel?.addGoal(title, horizon, targetValue, unitLabel, targetDate)
                showAddGoalDialog = false
            }
        )
    }
}

@Composable
private fun MissionCard(mission: MissionEntity) {
    val progress = (mission.currentCount.toFloat() / mission.targetCount).coerceIn(0f, 1f)

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceLevel1),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = mission.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Text(
                    text = "+${mission.xpReward} XP",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryAccent
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                LinearProgressIndicator(
                    progress = { progress },
                    color = if (mission.isCompleted == 1) SemanticSuccess else PrimaryAccent,
                    trackColor = SurfaceLevel2,
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${mission.currentCount}/${mission.targetCount}",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun GoalCard(
    goal: PersonalGoalEntity,
    onUpdateProgress: (Int) -> Unit
) {
    val progress = (goal.currentValue.toFloat() / goal.targetValue).coerceIn(0f, 1f)

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceLevel1),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = goal.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Text(
                    text = goal.horizon,
                    fontSize = 10.sp,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                LinearProgressIndicator(
                    progress = { progress },
                    color = if (goal.status == "COMPLETED") SemanticSuccess else PrimaryAccent,
                    trackColor = SurfaceLevel2,
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${goal.currentValue}/${goal.targetValue} ${goal.unitLabel}",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun XpLedgerCard(entry: XpLedgerEntity) {
    val isPositive = entry.xpDelta > 0

    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceLevel1),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.padding(12.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.reason,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
                Text(
                    text = entry.eventDate,
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
            Text(
                text = if (isPositive) "+${entry.xpDelta} XP" else "${entry.xpDelta} XP",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = if (isPositive) SemanticSuccess else SemanticDanger
            )
        }
    }
}
