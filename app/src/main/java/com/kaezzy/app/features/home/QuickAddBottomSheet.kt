package com.kaezzy.app.features.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kaezzy.app.ui.theme.BackgroundPrimary
import com.kaezzy.app.ui.theme.PrimaryAccent
import com.kaezzy.app.ui.theme.SurfaceLevel1
import com.kaezzy.app.ui.theme.SurfaceLevel2
import com.kaezzy.app.ui.theme.TextPrimary
import com.kaezzy.app.ui.theme.TextSecondary

enum class QuickAddOption { SELECT_TYPE, TASK, MONEY, LOG, NOTE }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickAddBottomSheet(
    onDismiss: () -> Unit,
    onSaveTask: (title: String, description: String, category: String, priority: String, dueDate: String?) -> Unit,
    onSaveMoney: (type: String, amount: Long, category: String) -> Unit = { _, _, _ -> },
    onSaveLog: (content: String) -> Unit = {},
    onSaveNote: (title: String, content: String) -> Unit = { _, _ -> }
) {
    var activeOption by remember { mutableStateOf(QuickAddOption.SELECT_TYPE) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SurfaceLevel1,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            when (activeOption) {
                QuickAddOption.SELECT_TYPE -> {
                    Text(
                        text = "Tambah Cepat, Brok!",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    QuickOptionRow(
                        title = "Tambah Tugas",
                        subtitle = "Catet deadline kuliah, kerja, atau urusan lu",
                        icon = Icons.Default.CheckCircle,
                        onClick = { activeOption = QuickAddOption.TASK }
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    QuickOptionRow(
                        title = "Catat Uang",
                        subtitle = "Pemasukan, pengeluaran, atau alokasi tabungan",
                        icon = Icons.Default.AttachMoney,
                        onClick = { activeOption = QuickAddOption.MONEY }
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    QuickOptionRow(
                        title = "Spill Daily Log",
                        subtitle = "Catet apa aja yang lu lakuin hari ini",
                        icon = Icons.Default.Book,
                        onClick = { activeOption = QuickAddOption.LOG }
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    QuickOptionRow(
                        title = "Tulis Note Cepat",
                        subtitle = "Simpen ide random biar gak nguap",
                        icon = Icons.Default.Edit,
                        onClick = { activeOption = QuickAddOption.NOTE }
                    )
                }

                QuickAddOption.TASK -> {
                    var title by remember { mutableStateOf("") }
                    var description by remember { mutableStateOf("") }
                    var category by remember { mutableStateOf("Pribadi") }
                    var priority by remember { mutableStateOf("MEDIUM") }

                    val categoryList = listOf("Kuliah", "Kerja", "Pribadi", "Lainnya")

                    Text(
                        text = "Tugas Baru",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

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
                        maxLines = 4,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Category selector
                    Text(text = "Kategori", fontSize = 12.sp, color = TextSecondary)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        categoryList.forEach { cat ->
                            val isSel = category.equals(cat, ignoreCase = true)
                            Card(
                                onClick = { category = cat },
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

                    // Priority selector
                    Text(text = "Prioritas", fontSize = 12.sp, color = TextSecondary)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        listOf("LOW", "MEDIUM", "HIGH").forEach { p ->
                            val isSel = priority == p
                            Card(
                                onClick = { priority = p },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSel) PrimaryAccent else SurfaceLevel2
                                )
                            ) {
                                Text(
                                    text = p,
                                    color = if (isSel) BackgroundPrimary else TextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextButton(onClick = { activeOption = QuickAddOption.SELECT_TYPE }) {
                            Text("Kembali", color = TextSecondary)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryAccent),
                            onClick = {
                                if (title.isNotBlank()) {
                                    onSaveTask(title, description, category, priority, null)
                                    onDismiss()
                                }
                            }
                        ) {
                            Text("Simpan", color = BackgroundPrimary)
                        }
                    }
                }

                QuickAddOption.MONEY -> {
                    var type by remember { mutableStateOf("EXPENSE") }
                    var amountInput by remember { mutableStateOf("") }
                    var category by remember { mutableStateOf("Pengeluaran") }

                    Text(
                        text = "Catat Uang",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("EXPENSE" to "Pengeluaran", "INCOME" to "Pemasukan").forEach { (tKey, tLabel) ->
                            val isSel = type == tKey
                            Card(
                                onClick = { type = tKey },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSel) PrimaryAccent else SurfaceLevel2
                                )
                            ) {
                                Text(
                                    text = tLabel,
                                    color = if (isSel) BackgroundPrimary else TextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = amountInput,
                        onValueChange = { amountInput = it.filter { c -> c.isDigit() } },
                        label = { Text("Jumlah Nominal (Rp)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Kategori (e.g. Makan, Transport, Gaji)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextButton(onClick = { activeOption = QuickAddOption.SELECT_TYPE }) {
                            Text("Kembali", color = TextSecondary)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryAccent),
                            onClick = {
                                val amount = amountInput.toLongOrNull() ?: 0L
                                if (amount > 0) {
                                    onSaveMoney(type, amount, category)
                                    onDismiss()
                                }
                            }
                        ) {
                            Text("Simpan", color = BackgroundPrimary)
                        }
                    }
                }

                QuickAddOption.LOG -> {
                    var contentInput by remember { mutableStateOf("") }

                    Text(
                        text = "Spill Daily Log",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = contentInput,
                        onValueChange = { contentInput = it },
                        label = { Text("Spill cerita/aktivitas lu hari ini...") },
                        maxLines = 4,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextButton(onClick = { activeOption = QuickAddOption.SELECT_TYPE }) {
                            Text("Kembali", color = TextSecondary)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryAccent),
                            onClick = {
                                if (contentInput.isNotBlank()) {
                                    onSaveLog(contentInput)
                                    onDismiss()
                                }
                            }
                        ) {
                            Text("Simpan", color = BackgroundPrimary)
                        }
                    }
                }

                QuickAddOption.NOTE -> {
                    var titleInput by remember { mutableStateOf("") }
                    var contentInput by remember { mutableStateOf("") }

                    Text(
                        text = "Tulis Note Cepat",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = titleInput,
                        onValueChange = { titleInput = it },
                        label = { Text("Judul Catatan") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = contentInput,
                        onValueChange = { contentInput = it },
                        label = { Text("Isi Catatan") },
                        maxLines = 4,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextButton(onClick = { activeOption = QuickAddOption.SELECT_TYPE }) {
                            Text("Kembali", color = TextSecondary)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryAccent),
                            onClick = {
                                if (titleInput.isNotBlank() || contentInput.isNotBlank()) {
                                    onSaveNote(titleInput, contentInput)
                                    onDismiss()
                                }
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

@Composable
private fun QuickOptionRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceLevel2),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = PrimaryAccent,
                modifier = Modifier.padding(end = 12.dp)
            )
            Column {
                Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                Text(text = subtitle, fontSize = 12.sp, color = TextSecondary)
            }
        }
    }
}
