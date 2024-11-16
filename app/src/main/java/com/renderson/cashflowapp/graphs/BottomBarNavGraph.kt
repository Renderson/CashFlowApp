package com.renderson.cashflowapp.graphs

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.List
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.renderson.cashflowapp.screens.ExtractScreen
import com.renderson.cashflowapp.screens.HomeScreen
import com.renderson.cashflowapp.viewmodel.CashFlowViewModel

@Composable
fun BottomNavGraph(
    modifier: Modifier,
    navController: NavHostController) {

    val viewModel: CashFlowViewModel = hiltViewModel()

    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = BottomNavItem.Home.route
    ) {
        composable(route = BottomNavItem.Home.route) {
            HomeScreen(
                viewModel = viewModel,
                name = BottomNavItem.Home.title
            ) {
                navController.navigate(Graph.DETAILS)
            }
        }
        composable(route = BottomNavItem.Extract.route) {
            ExtractScreen(
                viewModel = viewModel,
                name = BottomNavItem.Extract.title
            ) { }
        }
    }
}

sealed class BottomNavItem(
    var title:String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    var route:String
) {
    object Home : BottomNavItem("Home", Icons.Filled.Home, Icons.Outlined.Home,"home")
    object Extract: BottomNavItem("Extrato", Icons.Filled.List, Icons.Outlined.List,"extract")
}