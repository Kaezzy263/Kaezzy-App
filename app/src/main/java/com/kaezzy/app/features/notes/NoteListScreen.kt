package com.kaezzy.app.features.notes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kaezzy.app.core.copy.KaezzyMicrocopy
import com.kaezzy.app.core.database.entity.NoteEntity
import com.kaezzy.app.ui.theme.BackgroundPrimary
import com.kaezzy.app.ui.theme.PrimaryAccent
import com.kaezzy.app.ui.theme.SemanticDanger
import com.kaezzy.app.ui.theme.SurfaceLevel1
import com.kaezzy.app.ui.theme.SurfaceLevel2
import com.kaezzy.app.ui.theme.TextPrimary
import com.kaezzy.app.ui.theme.TextSecondary

@Composable
fun NoteListScreen(
    viewModel: NoteViewModel? = null,
    onNoteClick: (NoteEntity?) -> Unit = {}
) {
    val notes by viewModel?.notes?.collectAsState() ?: remember { mutableStateOf(emptyList()) }
    val searchQuery by viewModel?.searchQuery?.collectAsState() ?: remember { mutableStateOf("") }
    val isArchivedView by viewModel?.isArchivedView?.collectAsState() ?: remember { mutableStateOf(false) }

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
                    text = if (isArchivedView) "Arsip Catatan" else "Catatan Gw",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { onNoteClick(null) }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Catatan Baru",
                            tint = PrimaryAccent
                        )
                    }
                    TextButton(onClick = { viewModel?.toggleArchivedView() }) {
                        Text(
                            text = if (isArchivedView) "Lihat Aktif" else "Lihat Arsip",
                            color = PrimaryAccent,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel?.setSearchQuery(it) },
                placeholder = { Text("Cari catatan...", color = TextSecondary) },
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

            Spacer(modifier = Modifier.height(16.dp))

            if (notes.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(KaezzyMicrocopy.EMPTY_NOTES, color = TextSecondary, fontSize = 14.sp)
                }
            } else {
                val pinnedNotes = notes.filter { it.isPinned == 1 }
                val otherNotes = notes.filter { it.isPinned == 0 }

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (pinnedNotes.isNotEmpty()) {
                        item {
                            Text(
                                text = "Disematkan",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryAccent
                            )
                        }
                        items(pinnedNotes, key = { it.id }) { note ->
                            NoteCard(
                                note = note,
                                onClick = { onNoteClick(note) },
                                onTogglePin = { viewModel?.togglePin(note) },
                                onToggleArchive = { viewModel?.toggleArchive(note) },
                                onDelete = { viewModel?.deleteNote(note) }
                            )
                        }
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Catatan Lainnya",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary
                            )
                        }
                    }

                    items(otherNotes, key = { it.id }) { note ->
                        NoteCard(
                            note = note,
                            onClick = { onNoteClick(note) },
                            onTogglePin = { viewModel?.togglePin(note) },
                            onToggleArchive = { viewModel?.toggleArchive(note) },
                            onDelete = { viewModel?.deleteNote(note) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NoteCard(
    note: NoteEntity,
    onClick: () -> Unit,
    onTogglePin: () -> Unit,
    onToggleArchive: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceLevel1),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = note.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f)
                )

                if (note.isPinned == 1) {
                    Icon(
                        imageVector = Icons.Default.PushPin,
                        contentDescription = "Pinned",
                        tint = PrimaryAccent,
                        modifier = Modifier.padding(end = 4.dp)
                    )
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
                            text = { Text(if (note.isPinned == 1) "Lepas Pin" else "Sematkan", color = TextPrimary) },
                            onClick = {
                                showMenu = false
                                onTogglePin()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(if (note.isArchived == 1) "Batalkan Arsip" else "Arsipkan", color = TextPrimary) },
                            onClick = {
                                showMenu = false
                                onToggleArchive()
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

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = note.content.take(100),
                fontSize = 13.sp,
                color = TextSecondary,
                maxLines = 2
            )
        }
    }
}
