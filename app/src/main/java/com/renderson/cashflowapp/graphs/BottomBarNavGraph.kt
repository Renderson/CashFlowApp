package com.renderson.cashflowapp.graphs

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.Home
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.renderson.cashflowapp.R
import com.renderson.cashflowapp.model.Transaction
import com.renderson.cashflowapp.screens.ExtractScreen
import com.renderson.cashflowapp.screens.HomeScreen
import com.renderson.cashflowapp.screens.SearchScreen
import com.renderson.cashflowapp.viewmodel.CashFlowViewModel

@Composable
fun BottomNavGraph(
    modifier: Modifier,
    navController: NavHostController,
    viewModel: CashFlowViewModel,
    onEditTransaction: (Transaction) -> Unit
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = BottomNavItem.Home.route
    ) {
        composable(route = BottomNavItem.Home.route) {
            HomeScreen(
                viewModel = viewModel,
                name = stringResource(BottomNavItem.Home.titleRes)
            )
        }
        composable(route = BottomNavItem.Extract.route) {
            ExtractScreen(
                viewModel = viewModel,
                name = stringResource(BottomNavItem.Extract.titleRes),
                onEditTransaction = onEditTransaction,
                onSearchClick = { navController.navigate("search") }
            )
        }
        composable(route = "search") {
            SearchScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onEditTransaction = { transaction ->
                    navController.popBackStack()
                    onEditTransaction(transaction)
                }
            )
        }
    }
}

sealed class BottomNavItem(
    @StringRes val titleRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val route: String
) {
    object Home : BottomNavItem(
        R.string.nav_home,
        Icons.Filled.Home,
        Icons.Outlined.Home,
        "home"
    )

    object Extract : BottomNavItem(
        R.string.nav_extract,
        Icons.AutoMirrored.Filled.List,
        Icons.AutoMirrored.Outlined.List,
        "extract"
    )
}