package ee.ut.cs.alarm.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import ee.ut.cs.alarm.ui.screens.AlarmListScreen

@Composable
fun AlarmNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.AlarmList.route,
        modifier = modifier,
    ) {
        composable(Screen.AlarmList.route) {
            AlarmListScreen()
        }
        composable(Screen.About.route) {
            TODO("Implement about screen")
        }
    }
}

sealed class Screen(val route: String) {
    object AlarmList : Screen("alarmList")
    object About : Screen("about")
}