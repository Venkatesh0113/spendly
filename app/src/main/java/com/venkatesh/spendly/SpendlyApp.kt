package com.venkatesh.spendly

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun SpendlyApp() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(navController)
        }
        composable("add_expense") {
            AddExpenseScreen(navController)
        }
        composable("set_limit") {
            SetLimitScreen(navController)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SpendlyAppPreview() {
    SpendlyApp()
}
