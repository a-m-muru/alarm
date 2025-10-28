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
import ee.ut.cs.alarm.service.AlarmScheduler
import ee.ut.cs.alarm.ui.screens.AboutScreen
import ee.ut.cs.alarm.ui.screens.AlarmListScreen
import ee.ut.cs.alarm.ui.viewmodel.AlarmListViewModel
import ee.ut.cs.alarm.ui.viewmodel.AlarmListViewModelFactory

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
    val repo: AlarmRepository = remember { AlarmRepositoryImpl.getInstance(context) }
    val vm: AlarmListViewModel = viewModel(factory = AlarmListViewModelFactory(repo))
    val alarmScheduler = remember { AlarmScheduler(context) }

    NavHost(
        navController = navController,
        startDestination = Screen.AlarmList.route
    ) {
        composable(Screen.AlarmList.route) {
            AlarmListScreen(onNavigate, vm, alarmScheduler)
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