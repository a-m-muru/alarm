package ee.ut.cs.alarm.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import ee.ut.cs.alarm.screens.AboutScreen
import ee.ut.cs.alarm.screens.AddAlarmScreen
import ee.ut.cs.alarm.screens.AlarmsScreen

@Composable
fun AlarmNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Alarms.route,
        modifier = modifier
    ) {
        composable(Screen.Alarms.route) {
            AlarmsScreen()
        }
        composable(Screen.AddAlarm.route) {
            AddAlarmScreen()
        }
        composable(Screen.About.route) {
            AboutScreen()
        }
    }
}

sealed class Screen(val route: String) {
    object Alarms : Screen("alarms")
    object AddAlarm : Screen("add_alarm")
    object About : Screen("about")
}
