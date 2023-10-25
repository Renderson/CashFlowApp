package com.renderson.cashflowapp.graphs

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.renderson.cashflowapp.screens.RegisterScreen

fun NavGraphBuilder.detailsNavGraph(navController: NavHostController) {
    navigation(
        route = Graph.DETAILS,
        startDestination = DetailsScreen.Register.route
    ) {
        composable(route = DetailsScreen.Register.route) {
            RegisterScreen(
                name = DetailsScreen.Register.route,
                onClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}

sealed class DetailsScreen(val route: String) {
    object Register : DetailsScreen(route = "REGISTER")
}