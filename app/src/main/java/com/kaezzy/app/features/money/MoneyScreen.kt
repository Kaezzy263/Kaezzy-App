package com.kaezzy.app.features.money

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kaezzy.app.core.copy.KaezzyMicrocopy
import com.kaezzy.app.core.database.entity.MoneyTransactionEntity
import com.kaezzy.app.core.database.entity.SavingsGoalEntity
import com.kaezzy.app.ui.theme.BackgroundPrimary
import com.kaezzy.app.ui.theme.PrimaryAccent
import com.kaezzy.app.ui.theme.SecondaryAccent
import com.kaezzy.app.ui.theme.SemanticDanger
import com.kaezzy.app.ui.theme.SemanticSuccess
import com.kaezzy.app.ui.theme.SurfaceLevel1
import com.kaezzy.app.ui.theme.SurfaceLevel2
import com.kaezzy.app.ui.theme.TextPrimary
import com.kaezzy.app.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MoneyScreen(
    viewModel: MoneyViewModel? = null
) {
    val selectedTab by viewModel?.selectedTab?.collectAsState() ?: remember { mutableStateOf(MoneySubTab.TRANSACTIONS) }
    val transactions by viewModel?.allTransactions?.collectAsState() ?: remember { mutableStateOf(emptyList()) }
    val savingsGoals by viewModel?.savingsGoals?.collectAsState() ?: remember { mutableStateOf(emptyList()) }
    val balanceSummary by viewModel?.balanceSummary?.collectAsState() ?: remember {
        mutableStateOf(BalanceSummary(0, 0, 0, 0, 0))
    }

    var showAddTxSheet by remember { mutableStateOf(false) }
    var showAddGoalDialog by remember { mutableStateOf(false) }
    var selectedSavingsForCicil by remember { mutableStateOf<SavingsGoalEntity?>(null) }
    var selectedSavingsForHistory by remember { mutableStateOf<SavingsGoalEntity?>(null) }

    val todayDateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

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
                    text = "Keuangan Gw",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                IconButton(
                    onClick = {
                        if (selectedTab == MoneySubTab.TRANSACTIONS) {
                            showAddTxSheet = true
                        } else {
                            showAddGoalDialog = true
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Tambah",
                        tint = PrimaryAccent
                    )
                }
            }

            // Main Balance Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceLevel1),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Saldo Lu Sekarang", fontSize = 12.sp, color = TextSecondary)
                    Text(
                        text = MoneyCalculator.formatRupiah(balanceSummary.spendableBalance),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (balanceSummary.spendableBalance >= 0) TextPrimary else SemanticDanger
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text(text = "Masuk Bulan Ini", fontSize = 10.sp, color = TextSecondary)
                            Text(
                                text = MoneyCalculator.formatRupiah(balanceSummary.monthlyIncome),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = SemanticSuccess
                            )
                        }
                        Column {
                            Text(text = "Keluar Bulan Ini", fontSize = 10.sp, color = TextSecondary)
                            Text(
                                text = MoneyCalculator.formatRupiah(balanceSummary.monthlyExpense),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = SemanticDanger
                            )
                        }
                        Column {
                            Text(text = "Total di Tabungan", fontSize = 10.sp, color = TextSecondary)
                            Text(
                                text = MoneyCalculator.formatRupiah(balanceSummary.totalSaved),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = SecondaryAccent
                            )
                        }
                    }
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
                    selected = selectedTab == MoneySubTab.TRANSACTIONS,
                    onClick = { viewModel?.setSubTab(MoneySubTab.TRANSACTIONS) },
                    text = { Text("Riwayat Transaksi", color = if (selectedTab == MoneySubTab.TRANSACTIONS) PrimaryAccent else TextSecondary) }
                )
                Tab(
                    selected = selectedTab == MoneySubTab.SAVINGS,
                    onClick = { viewModel?.setSubTab(MoneySubTab.SAVINGS) },
                    text = { Text("Target Tabungan", color = if (selectedTab == MoneySubTab.SAVINGS) PrimaryAccent else TextSecondary) }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            when (selectedTab) {
                MoneySubTab.TRANSACTIONS -> {
                    if (transactions.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(KaezzyMicrocopy.EMPTY_MONEY, color = TextSecondary, fontSize = 14.sp)
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(transactions, key = { it.id }) { tx ->
                                TransactionCard(
                                    transaction = tx,
                                    onDelete = { viewModel?.deleteTransaction(tx) }
                                )
                            }
                        }
                    }
                }

                MoneySubTab.SAVINGS -> {
                    if (savingsGoals.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Belum ada target tabungan.", color = TextSecondary, fontSize = 14.sp)
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(savingsGoals, key = { it.id }) { goal ->
                                val goalBalance = MoneyCalculator.calculateSavingsGoalBalance(goal.id, transactions)
                                SavingsGoalCard(
                                    goal = goal,
                                    currentBalance = goalBalance,
                                    onCardClick = {
                                        selectedSavingsForHistory = goal
                                    },
                                    onCicilClick = {
                                        selectedSavingsForCicil = goal
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddTxSheet) {
        AddMoneyTransactionSheet(
            savingsGoals = savingsGoals,
            onDismiss = { showAddTxSheet = false },
            onSave = { type, amount, category, description, date, goalId ->
                viewModel?.addTransaction(type, amount, category, description, date, goalId)
                showAddTxSheet = false
            }
        )
    }

    if (showAddGoalDialog) {
        AddSavingsGoalDialog(
            onDismiss = { showAddGoalDialog = false },
            onSave = { name, targetAmount, targetDate ->
                viewModel?.addSavingsGoal(name, targetAmount, targetDate)
                showAddGoalDialog = false
            }
        )
    }

    val cicilGoal = selectedSavingsForCicil
    if (cicilGoal != null) {
        CicilTabunganDialog(
            goal = cicilGoal,
            onDismiss = { selectedSavingsForCicil = null },
            onSave = { amount ->
                viewModel?.addTransaction(
                    type = "SAVINGS_ALLOCATION",
                    amount = amount,
                    category = "Tabungan",
                    description = "Cicil ${cicilGoal.name}",
                    transactionDate = todayDateStr,
                    savingsGoalId = cicilGoal.id
                )
                selectedSavingsForCicil = null
            }
        )
    }

    val historyGoal = selectedSavingsForHistory
    if (historyGoal != null) {
        SavingsGoalHistoryDialog(
            goal = historyGoal,
            transactions = transactions.filter { it.savingsGoalId == historyGoal.id },
            onDismiss = { selectedSavingsForHistory = null }
        )
    }
}

@Composable
private fun TransactionCard(
    transaction: MoneyTransactionEntity,
    onDelete: () -> Unit
) {
    val isIncome = transaction.type.uppercase() == "INCOME"
    val isExpense = transaction.type.uppercase() == "EXPENSE"

    val color = when {
        isIncome -> SemanticSuccess
        isExpense -> SemanticDanger
        else -> SecondaryAccent
    }
    val prefix = when {
        isIncome -> "+"
        isExpense -> "-"
        else -> ""
    }

    Card(
        shape = RoundedCornerShape(12.dp),
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
                    text = transaction.category + if (!transaction.description.isNull_or_blank()) " • ${transaction.description}" else "",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Text(
                    text = transaction.transactionDate,
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "$prefix${MoneyCalculator.formatRupiah(transaction.amount)}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                IconButton(onClick = onDelete) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Hapus", tint = TextSecondary)
                }
            }
        }
    }
}

private fun String?.isNull_or_blank(): Boolean = this.isNullOrBlank()

@Composable
private fun SavingsGoalCard(
    goal: SavingsGoalEntity,
    currentBalance: Long,
    onCardClick: () -> Unit,
    onCicilClick: () -> Unit
) {
    val progress = if (goal.targetAmount > 0) {
        (currentBalance.toFloat() / goal.targetAmount).coerceIn(0f, 1f)
    } else 0f

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceLevel1),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onCardClick)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = goal.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = "Lihat Riwayat",
                        tint = TextSecondary,
                        modifier = Modifier.width(16.dp)
                    )
                }
                Text(
                    text = "${MoneyCalculator.formatRupiah(currentBalance)} / ${MoneyCalculator.formatRupiah(goal.targetAmount)}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SecondaryAccent
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            LinearProgressIndicator(
                progress = { progress },
                color = SecondaryAccent,
                trackColor = SurfaceLevel2,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Ketuk card untuk lihat riwayat",
                    fontSize = 10.sp,
                    color = TextSecondary
                )
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = SecondaryAccent),
                    onClick = onCicilClick
                ) {
                    Text("+ Cicil Tabungan", color = BackgroundPrimary, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
private fun CicilTabunganDialog(
    goal: SavingsGoalEntity,
    onDismiss: () -> Unit,
    onSave: (amount: Long) -> Unit
) {
    var nominalInput by remember { mutableStateOf("") }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceLevel1),
            modifier = Modifier.padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Cicil Tabungan: ${goal.name}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Nominal yang dicicil akan langsung memotong saldo lu sekarang.",
                    fontSize = 12.sp,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = nominalInput,
                    onValueChange = { nominalInput = it.filter { c -> c.isDigit() } },
                    label = { Text("Nominal (Rp)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
                        colors = ButtonDefaults.buttonColors(containerColor = SecondaryAccent),
                        onClick = {
                            val amount = nominalInput.toLongOrNull() ?: 0L
                            if (amount > 0) {
                                onSave(amount)
                            }
                        }
                    ) {
                        Text("Cicil Sekarang", color = BackgroundPrimary)
                    }
                }
            }
        }
    }
}

@Composable
private fun SavingsGoalHistoryDialog(
    goal: SavingsGoalEntity,
    transactions: List<MoneyTransactionEntity>,
    onDismiss: () -> Unit
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceLevel1),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Riwayat: ${goal.name}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Target Total: ${MoneyCalculator.formatRupiah(goal.targetAmount)}",
                    fontSize = 12.sp,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (transactions.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Belum ada riwayat cicilan untuk tabungan ini.",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.height(240.dp)
                    ) {
                        items(transactions, key = { it.id }) { tx ->
                            val isAllocation = tx.type.uppercase() == "SAVINGS_ALLOCATION"
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = SurfaceLevel2),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.padding(12.dp)
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = if (isAllocation) "Cicil Tabungan" else "Penarikan Tabungan",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = tx.transactionDate + if (!tx.description.isNull_or_blank()) " • ${tx.description}" else "",
                                            fontSize = 11.sp,
                                            color = TextSecondary
                                        )
                                    }
                                    Text(
                                        text = (if (isAllocation) "+" else "-") + MoneyCalculator.formatRupiah(tx.amount),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isAllocation) SecondaryAccent else SemanticDanger
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryAccent),
                        onClick = onDismiss
                    ) {
                        Text("Tutup", color = BackgroundPrimary)
                    }
                }
            }
        }
    }
}
