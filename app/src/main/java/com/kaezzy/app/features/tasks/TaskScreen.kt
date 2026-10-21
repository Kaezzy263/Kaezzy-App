package com.kaezzy.app.features.tasks

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kaezzy.app.core.copy.KaezzyMicrocopy
import com.kaezzy.app.core.database.entity.TaskEntity
import com.kaezzy.app.ui.theme.BackgroundPrimary
import com.kaezzy.app.ui.theme.PrimaryAccent
import com.kaezzy.app.ui.theme.SemanticDanger
import com.kaezzy.app.ui.theme.SemanticSuccess
import com.kaezzy.app.ui.theme.SemanticWarning
import com.kaezzy.app.ui.theme.SurfaceLevel1
import com.kaezzy.app.ui.theme.SurfaceLevel2
import com.kaezzy.app.ui.theme.TextPrimary
import com.kaezzy.app.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TaskScreen(
    viewModel: TaskViewModel
) {
    val tasks by viewModel.filteredTasks.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    var showAddDialog by remember { mutableStateOf(false) }

    val categories = listOf("Semua", "Kuliah", "Kerja", "Pribadi", "Lainnya")
    val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

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
                    text = "Tugas Gw",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                IconButton(onClick = { showAddDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Tambah Tugas",
                        tint = PrimaryAccent
                    )
                }
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text("Cari tugas...", color = TextSecondary) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = SurfaceLevel2,
                    unfocusedContainerColor = SurfaceLevel2,
                    focusedBorderColor = PrimaryAccent,
                    unfocusedBorderColor = Color.Transparent
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            val selectedTabIndex = when (selectedTab) {
                TaskFilterTab.TODO -> 0
                TaskFilterTab.DONE -> 1
                TaskFilterTab.ALL -> 2
            }

            // Tabs
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = SurfaceLevel1,
                contentColor = PrimaryAccent
            ) {
                Tab(
                    selected = selectedTab == TaskFilterTab.TODO,
                    onClick = { viewModel.setTab(TaskFilterTab.TODO) },
                    text = { Text("Belum Dikerjakan", color = if (selectedTab == TaskFilterTab.TODO) PrimaryAccent else TextSecondary) }
                )
                Tab(
                    selected = selectedTab == TaskFilterTab.DONE,
                    onClick = { viewModel.setTab(TaskFilterTab.DONE) },
                    text = { Text("Selesai", color = if (selectedTab == TaskFilterTab.DONE) PrimaryAccent else TextSecondary) }
                )
                Tab(
                    selected = selectedTab == TaskFilterTab.ALL,
                    onClick = { viewModel.setTab(TaskFilterTab.ALL) },
                    text = { Text("Semua", color = if (selectedTab == TaskFilterTab.ALL) PrimaryAccent else TextSecondary) }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Category Pills
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { cat ->
                    val isSel = selectedCategory.equals(cat, ignoreCase = true)
                    Card(
                        onClick = { viewModel.setCategory(cat) },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSel) PrimaryAccent else SurfaceLevel2
                        )
                    ) {
                        Text(
                            text = cat,
                            color = if (isSel) BackgroundPrimary else TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (tasks.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = KaezzyMicrocopy.EMPTY_TASKS,
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(tasks, key = { it.id }) { task ->
                        TaskItemCard(
                            task = task,
                            todayStr = todayStr,
                            onToggleComplete = { viewModel.toggleTaskCompletion(task) },
                            onCancel = { viewModel.cancelTask(task) },
                            onDelete = { viewModel.deleteTask(task) }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddTaskDialog(
            onDismiss = { showAddDialog = false },
            onSave = { title, description, category, priority, dueDate ->
                viewModel.addTask(title, description, category, priority, dueDate, null, context)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun TaskItemCard(
    task: TaskEntity,
    todayStr: String,
    onToggleComplete: () -> Unit,
    onCancel: () -> Unit,
    onDelete: () -> Unit
) {
    val isOverdue = task.status == "TODO" && task.dueDate != null && task.dueDate < todayStr
    var showMenu by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceLevel1),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth()
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
                // Judul (*Bold*)
                Text(
                    text = task.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (task.status == "DONE") TextSecondary else TextPrimary,
                    textDecoration = if (task.status == "DONE") TextDecoration.LineThrough else TextDecoration.None
                )

                // Kategori (_Italics_)
                Text(
                    text = task.category,
                    fontSize = 12.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    color = PrimaryAccent,
                    fontWeight = FontWeight.Medium
                )

                // Isi / Detail Tugas (Multi-line)
                if (task.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = task.description,
                        fontSize = 13.sp,
                        color = TextSecondary,
                        lineHeight = 18.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Priority Badge
                    val prioColor = when (task.priority.uppercase()) {
                        "HIGH" -> SemanticDanger
                        "MEDIUM" -> SemanticWarning
                        else -> TextSecondary
                    }
                    Card(
                        colors = CardDefaults.cardColors(containerColor = prioColor.copy(alpha = 0.2f))
                    ) {
                        Text(
                            text = task.priority,
                            fontSize = 10.sp,
                            color = prioColor,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    // Due Date Label
                    if (task.dueDate != null) {
                        Text(
                            text = if (isOverdue) "Lewat Deadline (${task.dueDate})" else task.dueDate,
                            fontSize = 11.sp,
                            color = if (isOverdue) SemanticDanger else TextSecondary,
                            fontWeight = if (isOverdue) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Menu", tint = TextSecondary)
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(SurfaceLevel2)
                ) {
                    DropdownMenuItem(
                        text = { Text("Batalkan Tugas", color = TextPrimary) },
                        onClick = {
                            showMenu = false
                            onCancel()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Hapus Permanen", color = SemanticDanger) },
                        onClick = {
                            showMenu = false
                            onDelete()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AddTaskDialog(
    onDismiss: () -> Unit,
    onSave: (title: String, description: String, category: String, priority: String, dueDate: String?) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Pribadi") }
    var priority by remember { mutableStateOf("MEDIUM") }

    val categoryList = listOf("Kuliah", "Kerja", "Pribadi", "Lainnya")
    val priorityList = listOf("LOW", "MEDIUM", "HIGH")

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceLevel1),
            modifier = Modifier.padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Tambah Tugas Baru",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Judul Tugas") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Isi / Detail Tugas (opsional)") },
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(text = "Kategori", fontSize = 12.sp, color = TextSecondary)
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categoryList) { cat ->
                        val isSel = category.equals(cat, ignoreCase = true)
                        Card(
                            onClick = { category = cat },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSel) PrimaryAccent else SurfaceLevel2
                            )
                        ) {
                            Text(
                                text = cat,
                                color = if (isSel) BackgroundPrimary else TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(text = "Prioritas", fontSize = 12.sp, color = TextSecondary)
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    priorityList.forEach { prio ->
                        val isSel = priority.equals(prio, ignoreCase = true)
                        Card(
                            onClick = { priority = prio },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSel) PrimaryAccent else SurfaceLevel2
                            )
                        ) {
                            Text(
                                text = prio,
                                color = if (isSel) BackgroundPrimary else TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    androidx.compose.material3.TextButton(onClick = onDismiss) {
                        Text("Batal", color = TextSecondary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    androidx.compose.material3.Button(
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = PrimaryAccent),
                        onClick = { onSave(title, description, category, priority, null) }
                    ) {
                        Text("Simpan", color = BackgroundPrimary)
                    }
                }
            }
        }
    }
}
