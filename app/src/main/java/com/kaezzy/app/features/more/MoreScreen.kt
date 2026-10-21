package com.kaezzy.app.features.more

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kaezzy.app.ui.theme.BackgroundPrimary
import com.kaezzy.app.ui.theme.PrimaryAccent
import com.kaezzy.app.ui.theme.SurfaceLevel1
import com.kaezzy.app.ui.theme.TextPrimary
import com.kaezzy.app.ui.theme.TextSecondary

@Composable
fun MoreScreen(
    onNavigateToDailyLog: () -> Unit = {},
    onNavigateToNotes: () -> Unit = {},
    onNavigateToSkills: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundPrimary)
            .padding(16.dp)
    ) {
        Text(
            text = "Fitur Lainnya",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            modifier = Modifier.padding(top = 16.dp, bottom = 16.dp)
        )

        HubCard(
            title = "Daily Log",
            subtitle = "Rekam cerita & timeline aktivitas harian lu",
            icon = Icons.Default.Book,
            onClick = onNavigateToDailyLog
        )

        Spacer(modifier = Modifier.height(12.dp))

        HubCard(
            title = "Notes Gw",
            subtitle = "Catatan ide, materi, dan draft tulisan",
            icon = Icons.Default.EditNote,
            onClick = onNavigateToNotes
        )

        Spacer(modifier = Modifier.height(12.dp))

        HubCard(
            title = "Skill Library",
            subtitle = "Katalog skill, materi belajar, dan code snippets",
            icon = Icons.Default.School,
            onClick = onNavigateToSkills
        )

        Spacer(modifier = Modifier.height(12.dp))

        HubCard(
            title = "Pengaturan & Keamanan",
            subtitle = "Profil, App Lock PIN, Biometrik & Backup Data",
            icon = Icons.Default.Settings,
            onClick = onNavigateToSettings
        )
    }
}

@Composable
private fun HubCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceLevel1),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = PrimaryAccent,
                modifier = Modifier.padding(end = 16.dp)
            )
            Column {
                Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = subtitle, fontSize = 12.sp, color = TextSecondary)
            }
        }
    }
}
