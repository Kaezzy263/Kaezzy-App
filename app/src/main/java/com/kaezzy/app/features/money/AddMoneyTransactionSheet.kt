package com.kaezzy.app.features.money

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
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
import com.kaezzy.app.core.database.entity.SavingsGoalEntity
import com.kaezzy.app.ui.theme.BackgroundPrimary
import com.kaezzy.app.ui.theme.PrimaryAccent
import com.kaezzy.app.ui.theme.SurfaceLevel1
import com.kaezzy.app.ui.theme.SurfaceLevel2
import com.kaezzy.app.ui.theme.TextPrimary
import com.kaezzy.app.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMoneyTransactionSheet(
    savingsGoals: List<SavingsGoalEntity> = emptyList(),
    onDismiss: () -> Unit,
    onSave: (type: String, amount: Long, category: String, description: String?, date: String, savingsGoalId: String?) -> Unit
) {
    var type by remember { mutableStateOf("EXPENSE") } // INCOME, EXPENSE, SAVINGS_ALLOCATION, SAVINGS_WITHDRAWAL
    var amountInput by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Makan") }
    var description by remember { mutableStateOf("") }
    var transactionDate by remember {
        mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))
    }
    var selectedGoalId by remember { mutableStateOf<String?>(savingsGoals.firstOrNull()?.id) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SurfaceLevel1
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Catat Transaksi Uang",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Transaction Type selector
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf(
                    "EXPENSE" to "Pengeluaran",
                    "INCOME" to "Pemasukan",
                    "SAVINGS_ALLOCATION" to "Alokasi Tabungan",
                    "SAVINGS_WITHDRAWAL" to "Tarik Tabungan"
                ).forEach { (tKey, tLabel) ->
                    val isSel = type == tKey
                    Card(
                        onClick = {
                            type = tKey
                            if (tKey.contains("SAVINGS") && category == "Makan") {
                                category = "Tabungan"
                            }
                        },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSel) PrimaryAccent else SurfaceLevel2
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = tLabel,
                            color = if (isSel) BackgroundPrimary else TextPrimary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = amountInput,
                onValueChange = { amountInput = it.filter { c -> c.isDigit() } },
                label = { Text("Nominal (Rp)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            if (type.contains("SAVINGS") && savingsGoals.isNotEmpty()) {
                Text(text = "Pilih Target Tabungan", fontSize = 12.sp, color = TextSecondary)
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    savingsGoals.forEach { goal ->
                        val isSel = selectedGoalId == goal.id
                        Card(
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSel) PrimaryAccent else SurfaceLevel2
                            ),
                            modifier = Modifier.clickable { selectedGoalId = goal.id }
                        ) {
                            Text(
                                text = goal.name,
                                color = if (isSel) BackgroundPrimary else TextPrimary,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            } else {
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Kategori (Makan, Transport, Gaji, dll)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Catatan / Deskripsi (Opsional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

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
                        val amt = amountInput.toLongOrNull() ?: 0L
                        val goalId = if (type.contains("SAVINGS")) selectedGoalId else null
                        onSave(type, amt, category, description.ifBlank { null }, transactionDate, goalId)
                        onDismiss()
                    }
                ) {
                    Text("Simpan", color = BackgroundPrimary)
                }
            }
        }
    }
}
