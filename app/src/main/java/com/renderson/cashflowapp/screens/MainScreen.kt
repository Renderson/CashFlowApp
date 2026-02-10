package com.renderson.cashflowapp.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.res.stringResource
import com.renderson.cashflowapp.R
import com.renderson.cashflowapp.graphs.BottomNavGraph
import com.renderson.cashflowapp.graphs.BottomNavItem
import com.renderson.cashflowapp.model.Transaction
import com.renderson.cashflowapp.viewmodel.CashFlowViewModel

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MainScreen(
    viewModel: CashFlowViewModel,
    onFabClick: () -> Unit,
    onEditTransaction: (Transaction) -> Unit
) {

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val extract by viewModel.extract.observeAsState()
    val hasNoData = extract?.years.isNullOrEmpty()

    val infiniteTransition = rememberInfiniteTransition(label = "fab_pulse")
    val fabScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Scaffold(
        bottomBar = {
            BottomBar(navController = navController)
        },
        floatingActionButton = {
            if (currentDestination?.route != "search") {
                ExtendedFloatingActionButton(
                    onClick = { onFabClick.invoke() },
                    modifier = Modifier.scale(if (hasNoData) fabScale else 1f),
                    icon = { Icon(Icons.Filled.Add, stringResource(R.string.fab_add_records_cd)) },
                    text = { Text(text = stringResource(R.string.fab_add_records)) },
                    expanded = false
                )
            }
        }
    ) { innerPadding ->
        BottomNavGraph(
            modifier = Modifier.padding(innerPadding),
            navController = navController,
            viewModel = viewModel,
            onEditTransaction = onEditTransaction
        )
    }
}

@Composable
fun BottomBar(navController: NavHostController) {
    val screens = listOf(
        BottomNavItem.Home,
        BottomNavItem.Extract
    )
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val bottomBarDestination = screens.any { it.route == currentDestination?.route }
    if (bottomBarDestination) {
        NavigationBar {
            screens.forEach { screen ->
                AddItem(
                    screen = screen,
                    currentDestination = currentDestination,
                    navController = navController
                )
            }
        }
    }
}

@Composable
fun RowScope.AddItem(
    screen: BottomNavItem,
    currentDestination: NavDestination?,
    navController: NavHostController
) {

    val destination = currentDestination?.hierarchy?.any {
        it.route == screen.route
    } == true

    NavigationBarItem(
        label = {
            Text(
                text = stringResource(screen.titleRes),
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        icon = {
            Icon(
                imageVector = if (destination) screen.selectedIcon else screen.unselectedIcon,
                contentDescription = stringResource(screen.titleRes),
                tint = MaterialTheme.colorScheme.onSurface
            )
        },
        selected = destination,
        onClick = {
            navController.navigate(screen.route) {
                popUpTo(navController.graph.findStartDestination().id)
                launchSingleTop = true
            }
        }
    )
}
