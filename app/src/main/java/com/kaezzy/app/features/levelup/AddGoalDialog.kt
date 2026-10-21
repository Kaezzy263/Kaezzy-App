package com.kaezzy.app.features.levelup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kaezzy.app.ui.theme.BackgroundPrimary
import com.kaezzy.app.ui.theme.PrimaryAccent
import com.kaezzy.app.ui.theme.SurfaceLevel1
import com.kaezzy.app.ui.theme.SurfaceLevel2
import com.kaezzy.app.ui.theme.TextPrimary
import com.kaezzy.app.ui.theme.TextSecondary

@Composable
fun AddGoalDialog(
    onDismiss: () -> Unit,
    onSave: (title: String, horizon: String, targetValue: Int, unitLabel: String, targetDate: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var horizon by remember { mutableStateOf("SHORT_TERM") }
    var targetValueInput by remember { mutableStateOf("10") }
    var unitLabel by remember { mutableStateOf("Milestone") }
    var targetDate by remember { mutableStateOf("2026-12-31") }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceLevel1),
            modifier = Modifier.padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Target Baru Gw",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Judul Target") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(text = "Jangka Waktu", fontSize = 12.sp, color = TextSecondary)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    listOf("SHORT_TERM" to "Pendek (<30 Hari)", "LONG_TERM" to "Panjang (>=30 Hari)").forEach { (key, label) ->
                        val isSel = horizon == key
                        Card(
                            onClick = { horizon = key },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSel) PrimaryAccent else SurfaceLevel2
                            )
                        ) {
                            Text(
                                text = label,
                                color = if (isSel) BackgroundPrimary else TextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = targetValueInput,
                        onValueChange = { targetValueInput = it.filter { c -> c.isDigit() } },
                        label = { Text("Target Angka") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = unitLabel,
                        onValueChange = { unitLabel = it },
                        label = { Text("Satuan (Buku, Sesi)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Batal", color = TextSecondary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryAccent),
                        onClick = {
                            val targetVal = targetValueInput.toIntOrNull() ?: 10
                            onSave(title, horizon, targetVal, unitLabel, targetDate)
                        }
                    ) {
                        Text("Simpan", color = BackgroundPrimary)
                    }
                }
            }
        }
    }
}
