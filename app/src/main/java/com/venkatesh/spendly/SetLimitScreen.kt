package com.venkatesh.spendly

import android.app.Application
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetLimitScreen(navController: NavController) {
    val context = LocalContext.current
    val sharedPref = context.getSharedPreferences("spendly_prefs", Context.MODE_PRIVATE)
    var limit by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Set Spending Limit") })
        },
        content = { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedTextField(
                    value = limit,
                    onValueChange = { limit = it },
                    label = { Text("Enter Limit ($)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val limitValue = limit.toDoubleOrNull()
                        if (limitValue != null) {
                            sharedPref.edit().putFloat("limit", limitValue.toFloat()).apply()
                            navController.popBackStack()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save Limit")
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 🔥 New Button: Reset Limit
                Button(
                    onClick = {
                        sharedPref.edit().remove("limit").apply()
                        navController.popBackStack()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Reset Limit", color = MaterialTheme.colorScheme.onErrorContainer)
                }
            }
        }
    )
}
