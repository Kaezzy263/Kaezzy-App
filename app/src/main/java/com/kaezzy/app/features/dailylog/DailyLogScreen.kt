package com.kaezzy.app.features.dailylog

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kaezzy.app.core.copy.KaezzyMicrocopy
import com.kaezzy.app.ui.theme.BackgroundPrimary
import com.kaezzy.app.ui.theme.PrimaryAccent
import com.kaezzy.app.ui.theme.SurfaceLevel1
import com.kaezzy.app.ui.theme.TextPrimary
import com.kaezzy.app.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DailyLogScreen(
    viewModel: DailyLogViewModel? = null
) {
    val selectedDate by viewModel?.selectedDate?.collectAsState() ?: remember {
        mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))
    }
    val logs by viewModel?.logs?.collectAsState() ?: remember { mutableStateOf(emptyList()) }

    var showAddActivityDialog by remember { mutableStateOf(false) }

    val summaryLog = logs.find { it.entryKind == "DAILY_SUMMARY" }
    val activityLogs = logs.filter { it.entryKind == "ACTIVITY_ITEM" }

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
                    text = "Daily Log",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                IconButton(onClick = { showAddActivityDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Tambah Aktivitas",
                        tint = PrimaryAccent
                    )
                }
            }

            // Date Navigator Bar
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceLevel1),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    IconButton(onClick = { viewModel?.navigateDays(-1) }) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Kemarin", tint = PrimaryAccent)
                    }
                    Text(
                        text = selectedDate,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    IconButton(onClick = { viewModel?.navigateDays(1) }) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "Besok", tint = PrimaryAccent)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Daily Summary Card
            var summaryInput by remember(summaryLog) { mutableStateOf(summaryLog?.content ?: "") }
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceLevel1),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Cerita Hari Ini",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = summaryInput,
                        onValueChange = {
                            summaryInput = it
                            viewModel?.saveSummary(it)
                        },
                        placeholder = { Text("Spill cerita lu hari ini, brok...", color = TextSecondary, fontSize = 13.sp) },
                        maxLines = 4,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Timeline Aktivitas",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (activityLogs.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(KaezzyMicrocopy.EMPTY_DAILY_LOG, color = TextSecondary, fontSize = 14.sp)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(activityLogs, key = { it.id }) { log ->
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
                                    if (!log.activityTime.isNull_or_blank()) {
                                        Text(
                                            text = log.activityTime.orEmpty(),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PrimaryAccent
                                        )
                                    }
                                    Text(
                                        text = log.content,
                                        fontSize = 13.sp,
                                        color = TextPrimary
                                    )
                                }
                                IconButton(onClick = { viewModel?.deleteLog(log) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = TextSecondary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddActivityDialog) {
        var timeInput by remember { mutableStateOf("") }
        var contentInput by remember { mutableStateOf("") }

        androidx.compose.ui.window.Dialog(onDismissRequest = { showAddActivityDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceLevel1),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Tambah Aktivitas", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = timeInput,
                        onValueChange = { timeInput = it },
                        label = { Text("Waktu (e.g. 14:30, Opsional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = contentInput,
                        onValueChange = { contentInput = it },
                        label = { Text("Aktivitas") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextButton(onClick = { showAddActivityDialog = false }) {
                            Text("Batal", color = TextSecondary)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryAccent),
                            onClick = {
                                viewModel?.addActivity(timeInput.ifBlank { null }, contentInput)
                                showAddActivityDialog = false
                            }
                        ) {
                            Text("Simpan", color = BackgroundPrimary)
                        }
                    }
                }
            }
        }
    }
}

private fun String?.isNull_or_blank(): Boolean = this.isNullOrBlank()
