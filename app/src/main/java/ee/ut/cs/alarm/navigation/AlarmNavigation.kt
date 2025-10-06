package ee.ut.cs.alarm.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import ee.ut.cs.alarm.screens.AboutScreen
import ee.ut.cs.alarm.screens.AddAlarmScreen
import ee.ut.cs.alarm.screens.AlarmsScreen
import ee.ut.cs.alarm.screens.EditAlarmScreen

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
            AlarmsScreen(navController = navController)
        }
        composable(Screen.AddAlarm.route) {
            AddAlarmScreen(navController = navController)
        }
        composable(Screen.EditAlarm.route) { backStackEntry ->
            val alarmId = backStackEntry.arguments?.getString("alarmId")
            EditAlarmScreen(
                alarmId = alarmId ?: "",
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.About.route) {
            AboutScreen()
        }
    }
}

sealed class Screen(val route: String) {
    object Alarms : Screen("alarms")
    object AddAlarm : Screen("add_alarm")
    object EditAlarm : Screen("edit_alarm/{alarmId}")
    object About : Screen("about")
}
