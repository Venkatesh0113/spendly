package com.venkatesh.spendly

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.shadow
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import java.text.SimpleDateFormat
import java.util.*

enum class SortOption {
    LATEST,
    AMOUNT_DESC,
    OLDEST
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    val expenseViewModel: ExpenseViewModel = viewModel(factory = ViewModelFactory(context.applicationContext as Application))
    val expenses by expenseViewModel.expenses.collectAsState()

    val totalSpent = expenses.sumOf { it.amount }
    val totalEntries = expenses.size

    // 🔔 Spend Limit Logic
    val sharedPref = context.getSharedPreferences("spendly_prefs", Context.MODE_PRIVATE)
    val limit = sharedPref.getFloat("limit", Float.MAX_VALUE)

    LaunchedEffect(totalSpent) {
        if (totalSpent > limit) {
            sendLimitNotification(context, totalSpent)
        }
    }

    var sortOption by remember { mutableStateOf(SortOption.LATEST) }
    var expanded by remember { mutableStateOf(false) }

    val sortedExpenses = remember(expenses, sortOption) {
        when (sortOption) {
            SortOption.LATEST -> expenses.sortedByDescending { it.timestamp }
            SortOption.OLDEST -> expenses.sortedBy { it.timestamp }
            SortOption.AMOUNT_DESC -> expenses.sortedByDescending { it.amount }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Expenses", fontSize = 20.sp, fontWeight = FontWeight.Bold) },
                actions = {
                    Box {
                        TextButton(onClick = { expanded = true }) {
                            Text("Sort By")
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            DropdownMenuItem(text = { Text("Latest") }, onClick = {
                                sortOption = SortOption.LATEST
                                expanded = false
                            })
                            DropdownMenuItem(text = { Text("Amount: High to Low") }, onClick = {
                                sortOption = SortOption.AMOUNT_DESC
                                expanded = false
                            })
                            DropdownMenuItem(text = { Text("Oldest First") }, onClick = {
                                sortOption = SortOption.OLDEST
                                expanded = false
                            })
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("add") }) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        },
        content = { innerPadding ->
            if (sortedExpenses.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No expenses yet. Tap + to add!", style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .padding(innerPadding)
                        .padding(16.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    "Total Spent: $${String.format("%.2f", totalSpent)}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                                Text("Transactions: $totalEntries")
                            }
                        }
                    }

                    items(sortedExpenses) { expense ->
                        val date = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                            .format(Date(expense.timestamp))

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .shadow(2.dp, shape = MaterialTheme.shapes.medium),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = expense.description,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = "Category: ${expense.category}",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                        Text(
                                            text = date,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.Gray
                                        )
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "$${String.format("%.2f", expense.amount)}",
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold
                                        )
                                        IconButton(
                                            onClick = { expenseViewModel.deleteExpense(expense) }
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}

fun sendLimitNotification(context: Context, amount: Double) {
    val channelId = "spendly_channel"
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            channelId,
            "Spendly Alerts",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Alerts when spending limit is crossed"
        }
        notificationManager.createNotificationChannel(channel)
    }

    val builder = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setContentTitle("💸 Limit Exceeded!")
        .setContentText("You’ve spent $${String.format("%.2f", amount)} — over your limit!")
        .setPriority(NotificationCompat.PRIORITY_HIGH)

    NotificationManagerCompat.from(context).notify(101, builder.build())
}
