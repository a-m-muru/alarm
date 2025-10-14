package ee.ut.cs.alarm.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import ee.ut.cs.alarm.ui.screens.AboutScreen
import ee.ut.cs.alarm.ui.screens.AlarmListScreen

@Composable
fun AlarmNavigation(
    navController: NavHostController,
) {
    // Callback function to call from screens in order to change
    // the navigation
    val onNavigate = { route: String ->
        navController.navigate(route) {
            popUpTo(navController.graph.startDestinationId) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    NavHost(
        navController = navController,
        startDestination = Screen.AlarmList.route
    ) {
        composable(Screen.AlarmList.route) {
            AlarmListScreen(onNavigate)
        }
        composable(Screen.About.route) {
            AboutScreen(onNavigate)
        }
    }
}

sealed class Screen(val route: String) {
    object AlarmList : Screen("alarmList")
    object About : Screen("about")
}