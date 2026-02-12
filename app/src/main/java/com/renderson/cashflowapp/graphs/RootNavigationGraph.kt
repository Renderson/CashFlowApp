package com.renderson.cashflowapp.graphs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.renderson.cashflowapp.data.repository.AuthRepository
import com.renderson.cashflowapp.screens.CreateAccountScreen
import com.renderson.cashflowapp.screens.LoginScreen
import com.renderson.cashflowapp.screens.MainScreen
import com.renderson.cashflowapp.viewmodel.AuthViewModel
import com.renderson.cashflowapp.viewmodel.CashFlowViewModel

@Composable
fun RootNavigationGraph(
    navController: NavHostController,
    cashFlowViewModel: CashFlowViewModel,
    authViewModel: AuthViewModel,
    authRepository: AuthRepository
) {
    val currentUser by authRepository.currentUser.collectAsState(initial = null)

    NavHost(
        navController = navController,
        route = Graph.ROOT,
        startDestination = Graph.AUTH
    ) {
        composable(route = Graph.AUTH) {
            if (currentUser != null) {
                LaunchedEffect(currentUser) {
                    navController.navigate(Graph.HOME) {
                        popUpTo(Graph.AUTH) { inclusive = true }
                    }
                }
            } else {
                LoginScreen(
                    viewModel = authViewModel,
                    onLoginSuccess = {
                        navController.navigate(Graph.HOME) {
                            popUpTo(Graph.AUTH) { inclusive = true }
                        }
                    },
                    onCreateAccountClick = {
                        navController.navigate(Graph.CREATE_ACCOUNT)
                    }
                )
            }
        }

        composable(route = Graph.CREATE_ACCOUNT) {
            CreateAccountScreen(
                viewModel = authViewModel,
                onAccountCreated = {
                    navController.navigate(Graph.HOME) {
                        popUpTo(Graph.AUTH) { inclusive = true }
                    }
                },
                onBackToLogin = {
                    navController.popBackStack()
                }
            )
        }

        composable(route = Graph.HOME) {
            if (currentUser == null) {
                LaunchedEffect(Unit) {
                    navController.navigate(Graph.AUTH) {
                        popUpTo(Graph.HOME) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            } else {
                MainScreen(
                    viewModel = cashFlowViewModel,
                    onFabClick = {
                        cashFlowViewModel.clearTransactionToEdit()
                        navController.navigate(Graph.DETAILS)
                    },
                    onEditTransaction = { transaction ->
                        cashFlowViewModel.setTransactionToEdit(transaction)
                        navController.navigate(Graph.DETAILS)
                    }
                )
            }
        }
        detailsNavGraph(navController = navController, viewModel = cashFlowViewModel)
    }
}

object Graph {
    const val ROOT = "root_graph"
    const val AUTH = "auth"
    const val CREATE_ACCOUNT = "create_account"
    const val HOME = "home_graph"
    const val DETAILS = "details_graph"
}