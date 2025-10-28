package ee.ut.cs.alarm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.rememberNavController
import ee.ut.cs.alarm.perm.PermissionManager
import ee.ut.cs.alarm.ui.navigation.AlarmNavigation
import ee.ut.cs.alarm.ui.theme.AlarmTheme

class MainActivity : ComponentActivity() {
    private lateinit var permissionManager: PermissionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        permissionManager = PermissionManager(this)

        enableEdgeToEdge()
        setContent {
            AlarmTheme {
                val navController = rememberNavController()

                LaunchedEffect(Unit) {
                    permissionManager.requestAllPermissions()
                }

                AlarmNavigation(
                    navController = navController
                )
            }
        }
    }
}