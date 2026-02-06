package com.renderson.cashflowapp.graphs

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.renderson.cashflowapp.screens.RegisterScreen
import com.renderson.cashflowapp.viewmodel.CashFlowViewModel

fun NavGraphBuilder.detailsNavGraph(
    navController: NavHostController,
    viewModel: CashFlowViewModel
) {
    navigation(
        route = Graph.DETAILS,
        startDestination = DetailsScreen.Register.route
    ) {
        composable(route = DetailsScreen.Register.route) {
            RegisterScreen(
                viewModel = viewModel,
                name = DetailsScreen.Register.title,
                onClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}

sealed class DetailsScreen(val title: String, val route: String) {
    object Register : DetailsScreen(title = "Registro", route = "register")
}