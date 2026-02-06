package com.renderson.cashflowapp.graphs

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.renderson.cashflowapp.screens.MainScreen
import com.renderson.cashflowapp.viewmodel.CashFlowViewModel

@Composable
fun RootNavigationGraph(
    navController: NavHostController,
    viewModel: CashFlowViewModel
) {
    NavHost(
        navController = navController,
        route = Graph.ROOT,
        startDestination = Graph.HOME
    ) {
        composable(route = Graph.HOME) {
            MainScreen(
                viewModel = viewModel,
                onFabClick = {
                    viewModel.clearTransactionToEdit()
                    navController.navigate(Graph.DETAILS)
                },
                onEditTransaction = { transaction ->
                    viewModel.setTransactionToEdit(transaction)
                    navController.navigate(Graph.DETAILS)
                }
            )
        }
        detailsNavGraph(navController = navController, viewModel = viewModel)
    }
}

object Graph {
    const val ROOT = "root_graph"
    const val HOME = "home_graph"
    const val DETAILS = "details_graph"
}