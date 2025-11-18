package ee.ut.cs.alarm

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.rememberNavController
import ee.ut.cs.alarm.data.Alarm
import ee.ut.cs.alarm.perm.PermissionManager
import ee.ut.cs.alarm.ui.navigation.AlarmNavigation
import ee.ut.cs.alarm.ui.theme.AlarmTheme

class MainActivity : ComponentActivity() {
    private lateinit var permissionManager: PermissionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        permissionManager = PermissionManager(this)

        if (false) {
            val alarmIntent =
                Intent(this, AlarmActivity::class.java).apply {
                    putExtra("ut.cs.alarm.alarm", Alarm(time = 48u))
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }

            startActivity(alarmIntent)
        }

        enableEdgeToEdge()
        setContent {
            AlarmTheme {
                val navController = rememberNavController()

                LaunchedEffect(Unit) {
                    permissionManager.requestAllPermissions()
                }

                AlarmNavigation(
                    navController = navController,
                )
            }
        }
    }
}
