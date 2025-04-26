package com.venkatesh.spendly

import androidx.compose.ui.graphics.Color
import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.venkatesh.spendly.ui.theme.LightGrayBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(navController: NavController) {
    val context = LocalContext.current
    val expenseViewModel: ExpenseViewModel = viewModel(factory = ViewModelFactory(context.applicationContext as Application))

    var description by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    val categories = listOf(
        "Food", "Travel", "Bills", "Entertainment", "Health",
        "Shopping", "Education", "Work", "Savings", "Other"
    )
    var selectedCategory by remember { mutableStateOf("Other") }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Add Expense") })
        },
        content = { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Select Category:",
                    style = MaterialTheme.typography.labelLarge
                )

                Column(modifier = Modifier.fillMaxWidth()) {
                    categories.chunked(2).forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            rowItems.forEach { category ->
                                Button(
                                    onClick = { selectedCategory = category },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (selectedCategory == category)
                                            MaterialTheme.colorScheme.primary  // Selected: teal
                                        else
                                            LightGrayBackground  // Unselected: light neutral gray
                                    ),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = category,
                                        color = if (selectedCategory == category)
                                            Color.White
                                        else
                                            Color.Black  // Text color black if unselected
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val amt = amount.toDoubleOrNull()
                        if (description.isNotBlank() && amt != null) {
                            expenseViewModel.addExpense(description, amt, selectedCategory)
                            description = ""
                            amount = ""
                            selectedCategory = "Other"
                            navController.popBackStack()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Save Expense")
                }
            }
        }
    )
}
