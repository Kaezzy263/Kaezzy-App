package com.kaezzy.app.features.settings

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kaezzy.app.core.copy.KaezzyMicrocopy
import com.kaezzy.app.ui.theme.BackgroundPrimary
import com.kaezzy.app.ui.theme.PrimaryAccent
import com.kaezzy.app.ui.theme.SemanticDanger
import com.kaezzy.app.ui.theme.SurfaceLevel1
import com.kaezzy.app.ui.theme.SurfaceLevel2
import com.kaezzy.app.ui.theme.TextPrimary
import com.kaezzy.app.ui.theme.TextSecondary

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel? = null,
    onBack: () -> Unit = {}
) {
    val appSettings by viewModel?.appSettings?.collectAsState() ?: remember { mutableStateOf(null) }
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var showSetupPinDialog by remember { mutableStateOf(false) }
    var showResetDataDialog by remember { mutableStateOf(false) }
    var showRestoreDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = BackgroundPrimary
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = TextPrimary)
                }
                Text(
                    text = "Pengaturan & Keamanan",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            // 1. Profil Pengguna
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceLevel1),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Profil Pengguna", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(10.dp))
                    var nickname by remember(appSettings?.nickname) { mutableStateOf(appSettings?.nickname ?: "Brok") }
                    OutlinedTextField(
                        value = nickname,
                        onValueChange = {
                            nickname = it
                            viewModel?.updateNickname(it)
                        },
                        label = { Text("Nama Panggilan") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. App Lock & Security
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceLevel1),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Keamanan & App Lock", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(12.dp))

                    val isPinEnabled = appSettings?.isPinEnabled == 1

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Kunci PIN 6-Digit", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                            Text("Minta PIN setiap kali membuka aplikasi", fontSize = 12.sp, color = TextSecondary)
                        }
                        Switch(
                            checked = isPinEnabled,
                            onCheckedChange = { checked ->
                                if (checked) {
                                    showSetupPinDialog = true
                                } else {
                                    viewModel?.disablePin()
                                }
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = BackgroundPrimary, checkedTrackColor = PrimaryAccent)
                        )
                    }

                    if (isPinEnabled) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Gunakan Sidik Jari / Biometrik", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                Text("Sensors sidik jari lokal HP", fontSize = 12.sp, color = TextSecondary)
                            }
                            Switch(
                                checked = appSettings?.isBiometricEnabled == 1,
                                onCheckedChange = { checked ->
                                    viewModel?.toggleBiometrics(checked)
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = BackgroundPrimary, checkedTrackColor = PrimaryAccent)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Backup & Restore Data
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceLevel1),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Manajemen Data", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            colors = ButtonDefaults.buttonColors(containerColor = SurfaceLevel2),
                            onClick = {
                                viewModel?.exportBackup { json ->
                                    clipboardManager.setText(AnnotatedString(json))
                                    Toast.makeText(context, KaezzyMicrocopy.BACKUP_SUCCESS + " (Disalin ke Clipboard)", Toast.LENGTH_LONG).show()
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, tint = PrimaryAccent)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Ekspor Backup", color = TextPrimary, fontSize = 11.sp)
                        }

                        Button(
                            colors = ButtonDefaults.buttonColors(containerColor = SurfaceLevel2),
                            onClick = { showRestoreDialog = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Upload, contentDescription = null, tint = PrimaryAccent)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Impor Backup", color = TextPrimary, fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = SemanticDanger.copy(alpha = 0.2f)),
                        onClick = { showResetDataDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Reset Seluruh Data Aplikasi", color = SemanticDanger, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showSetupPinDialog) {
        var pinInput by remember { mutableStateOf("") }
        var recoveryInput by remember { mutableStateOf("") }

        androidx.compose.ui.window.Dialog(onDismissRequest = { showSetupPinDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceLevel1),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Atur PIN & Security Passphrase", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) pinInput = it },
                        label = { Text("PIN 6 Angka") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = recoveryInput,
                        onValueChange = { recoveryInput = it },
                        label = { Text("Recovery Passphrase (Untuk Lupa PIN)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                        TextButton(onClick = { showSetupPinDialog = false }) { Text("Batal", color = TextSecondary) }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryAccent),
                            onClick = {
                                if (pinInput.length == 6 && recoveryInput.isNotBlank()) {
                                    viewModel?.setupPin(pinInput, recoveryInput)
                                    showSetupPinDialog = false
                                }
                            }
                        ) { Text("Aktifkan PIN", color = BackgroundPrimary) }
                    }
                }
            }
        }
    }

    if (showRestoreDialog) {
        var restoreJsonInput by remember { mutableStateOf("") }

        androidx.compose.ui.window.Dialog(onDismissRequest = { showRestoreDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceLevel1),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Impor Backup Data", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = restoreJsonInput,
                        onValueChange = { restoreJsonInput = it },
                        label = { Text("Tempel Isi JSON Backup di Sini") },
                        maxLines = 8,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                        TextButton(onClick = { showRestoreDialog = false }) { Text("Batal", color = TextSecondary) }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryAccent),
                            onClick = {
                                viewModel?.restoreBackup(restoreJsonInput)
                                showRestoreDialog = false
                            }
                        ) { Text("Pulihkan", color = BackgroundPrimary) }
                    }
                }
            }
        }
    }

    if (showResetDataDialog) {
        var confirmText by remember { mutableStateOf("") }

        androidx.compose.ui.window.Dialog(onDismissRequest = { showResetDataDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceLevel1),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Reset Seluruh Data Aplikasi", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = SemanticDanger)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(KaezzyMicrocopy.CONFIRM_RESET_ALL_DATA, fontSize = 12.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = confirmText,
                        onValueChange = { confirmText = it },
                        label = { Text("Ketik 'HAPUS' di sini") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                        TextButton(onClick = { showResetDataDialog = false }) { Text("Batal", color = TextSecondary) }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            colors = ButtonDefaults.buttonColors(containerColor = SemanticDanger),
                            onClick = {
                                viewModel?.resetAllData(confirmText)
                                showResetDataDialog = false
                            }
                        ) { Text("HAPUS SEMUA", color = TextPrimary) }
                    }
                }
            }
        }
    }
}
