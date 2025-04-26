package com.venkatesh.spendly

import android.app.Application
import android.content.Context
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.venkatesh.spendly.ui.theme.LightGrayBackground
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

enum class SortOption {
    LATEST,
    AMOUNT_DESC,
    AMOUNT_ASC,  // 🔥 Added Low to High
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

    val sharedPref = context.getSharedPreferences("spendly_prefs", Context.MODE_PRIVATE)
    val limit = sharedPref.getFloat("limit", -1f)

    var sortOption by remember { mutableStateOf(SortOption.LATEST) }
    var expanded by remember { mutableStateOf(false) }

    val sortedExpenses = remember(expenses, sortOption) {
        when (sortOption) {
            SortOption.LATEST -> expenses.sortedByDescending { it.timestamp }
            SortOption.OLDEST -> expenses.sortedBy { it.timestamp }
            SortOption.AMOUNT_DESC -> expenses.sortedByDescending { it.amount }
            SortOption.AMOUNT_ASC -> expenses.sortedBy { it.amount }
        }
    }

    var showWelcome by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(5000)
        showWelcome = false
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
                            DropdownMenuItem(text = { Text("Amount: Low to High") }, onClick = {
                                sortOption = SortOption.AMOUNT_ASC
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
            FloatingActionButton(
                onClick = { navController.navigate("add_expense") },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        },
        content = { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(16.dp)
            ) {
                if (showWelcome) {
                    item {
                        Text(
                            text = "Welcome to Spendly",
                            style = MaterialTheme.typography.headlineMedium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "Smart Expense Tracker",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 24.dp),
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }

                item {
                    Button(
                        onClick = { navController.navigate("set_limit") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Set Spending Limit")
                    }
                }

                item {
                    if (limit == -1f) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = LightGrayBackground
                            )
                        ) {
                            Text(
                                "No Spending Limit Set",
                                modifier = Modifier.padding(16.dp),
                                color = Color.Black,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = LightGrayBackground
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    "Current Spending Limit: $${String.format("%.2f", limit)}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = Color.Black
                                )
                            }
                        }
                    }
                }

                if (sortedExpenses.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No expenses yet. Tap + to add!", style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                } else {
                    items(sortedExpenses) { expense ->
                        val date = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                            .format(Date(expense.timestamp))

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
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
