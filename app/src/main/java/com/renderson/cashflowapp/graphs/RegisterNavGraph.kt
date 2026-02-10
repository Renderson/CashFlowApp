package com.renderson.cashflowapp.graphs

import androidx.annotation.StringRes
import androidx.navigation.NavGraphBuilder
import com.renderson.cashflowapp.R
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
                nameRes = DetailsScreen.Register.titleRes,
                onClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}

sealed class DetailsScreen(@StringRes val titleRes: Int, val route: String) {
    object Register : DetailsScreen(titleRes = R.string.nav_register, route = "register")
}