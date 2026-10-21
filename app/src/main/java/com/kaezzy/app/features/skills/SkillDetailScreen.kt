package com.kaezzy.app.features.skills

import android.widget.Toast
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kaezzy.app.core.database.entity.SkillEntity
import com.kaezzy.app.ui.theme.BackgroundPrimary
import com.kaezzy.app.ui.theme.PrimaryAccent
import com.kaezzy.app.ui.theme.SecondaryAccent
import com.kaezzy.app.ui.theme.SurfaceLevel1
import com.kaezzy.app.ui.theme.SurfaceLevel2
import com.kaezzy.app.ui.theme.TextPrimary
import com.kaezzy.app.ui.theme.TextSecondary

@Composable
fun SkillDetailScreen(
    skill: SkillEntity,
    viewModel: SkillViewModel? = null,
    onBack: () -> Unit = {}
) {
    val entries by viewModel?.entries?.collectAsState() ?: remember { mutableStateOf(emptyList()) }
    var showAddEntryDialog by remember { mutableStateOf(false) }
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali", tint = TextPrimary)
                    }
                    Text(
                        text = skill.name,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
                IconButton(onClick = { showAddEntryDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Tambah Catatan/Kode",
                        tint = PrimaryAccent
                    )
                }
            }

            // Header Banner & Study Action Button
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceLevel1),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(text = "Kategori: ${skill.category}", fontSize = 12.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryAccent),
                        onClick = { viewModel?.markStudiedToday(skill.id) }
                    ) {
                        Text("Tandai Belajar Hari Ini (+20 XP)", color = BackgroundPrimary, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Catatan & Snippets",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (entries.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Belum ada catatan/kode di skill ini.", color = TextSecondary, fontSize = 14.sp)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(entries, key = { it.id }) { entry ->
                        val isCode = entry.entryType == "CODE_SNIPPET"

                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceLevel1),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = entry.title,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Row {
                                        if (isCode) {
                                            IconButton(
                                                onClick = {
                                                    clipboardManager.setText(AnnotatedString(entry.content))
                                                    Toast.makeText(context, "Kode disalin!", Toast.LENGTH_SHORT).show()
                                                }
                                            ) {
                                                Icon(Icons.Default.ContentCopy, contentDescription = "Salin Kode", tint = SecondaryAccent)
                                            }
                                        }
                                        IconButton(onClick = { viewModel?.deleteSkillEntry(entry) }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = TextSecondary)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                if (isCode) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(SurfaceLevel2, shape = RoundedCornerShape(6.dp))
                                            .padding(10.dp)
                                    ) {
                                        Text(
                                            text = entry.content,
                                            fontSize = 12.sp,
                                            fontFamily = FontFamily.Monospace,
                                            color = SecondaryAccent
                                        )
                                    }
                                } else {
                                    Text(
                                        text = entry.content,
                                        fontSize = 13.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddEntryDialog) {
        var titleInput by remember { mutableStateOf("") }
        var contentInput by remember { mutableStateOf("") }
        var entryType by remember { mutableStateOf("NOTE") }

        androidx.compose.ui.window.Dialog(onDismissRequest = { showAddEntryDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceLevel1),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Tambah Entry Belajar", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("NOTE" to "Catatan", "CODE_SNIPPET" to "Code Snippet").forEach { (typeKey, typeLabel) ->
                            val isSel = entryType == typeKey
                            Card(
                                onClick = { entryType = typeKey },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSel) PrimaryAccent else SurfaceLevel2
                                )
                            ) {
                                Text(
                                    text = typeLabel,
                                    color = if (isSel) BackgroundPrimary else TextPrimary,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = titleInput,
                        onValueChange = { titleInput = it },
                        label = { Text("Judul Entry") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = contentInput,
                        onValueChange = { contentInput = it },
                        label = { Text(if (entryType == "CODE_SNIPPET") "Potongan Kode" else "Isi Catatan") },
                        maxLines = 6,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextButton(onClick = { showAddEntryDialog = false }) {
                            Text("Batal", color = TextSecondary)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryAccent),
                            onClick = {
                                viewModel?.addSkillEntry(skill.id, entryType, titleInput, contentInput)
                                showAddEntryDialog = false
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
