package ee.ut.cs.alarm.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import ee.ut.cs.alarm.data.repo.AlarmRepository
import ee.ut.cs.alarm.data.repo.AlarmRepositoryImpl
import ee.ut.cs.alarm.data.repo.UserPrefsRepository
import ee.ut.cs.alarm.data.repo.UserPrefsRepositoryImpl
import ee.ut.cs.alarm.service.AlarmScheduler
import ee.ut.cs.alarm.ui.screens.AboutScreen
import ee.ut.cs.alarm.ui.screens.AlarmListScreen
import ee.ut.cs.alarm.ui.screens.SettingsScreen
import ee.ut.cs.alarm.ui.viewmodel.AlarmListViewModel
import ee.ut.cs.alarm.ui.viewmodel.AlarmListViewModelFactory
import ee.ut.cs.alarm.ui.viewmodel.UserPrefsViewModel
import ee.ut.cs.alarm.ui.viewmodel.UserPrefsViewModelFactory

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

    val context = LocalContext.current
    val alarmRepo: AlarmRepository = remember { AlarmRepositoryImpl.getInstance(context) }
    val prefsRepo: UserPrefsRepository = remember { UserPrefsRepositoryImpl.getInstance(context) }
    val alarmVm: AlarmListViewModel = viewModel(factory = AlarmListViewModelFactory(alarmRepo))
    val prefsVm: UserPrefsViewModel = viewModel(factory = UserPrefsViewModelFactory(prefsRepo))
    val alarmScheduler = remember { AlarmScheduler(context) }

    NavHost(
        navController = navController,
        startDestination = Screen.AlarmList.route
    ) {
        composable(Screen.AlarmList.route) {
            AlarmListScreen(onNavigate, alarmVm, alarmScheduler)
        }
        composable(Screen.About.route) {
            AboutScreen(onNavigate)
        }
        composable(Screen.Settings.route) {
            SettingsScreen(prefsVm, onNavigate)
        }
    }
}

sealed class Screen(val route: String) {
    object AlarmList : Screen("alarmList")
    object About : Screen("about")
    object Settings : Screen("settings")

}