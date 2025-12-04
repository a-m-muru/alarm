package ee.ut.cs.alarm

import android.content.Intent
import android.os.Bundle
import android.util.Log
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

    override fun onResume() {
        super.onResume()

        Log.d("MAINACTIVITY", "resumed")
        tryGoToAlarmActivity()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        permissionManager = PermissionManager(this)

        if (false) { // debug
            val alarmIntent =
                Intent(this, AlarmActivity::class.java).apply {
                    putExtra(ALARM_INTENT_EXTRA_ALARM, Alarm(time = 48u))
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
            startActivity(alarmIntent)
            finish()
        }

        if (tryGoToAlarmActivity()) return

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

    private fun tryGoToAlarmActivity(): Boolean {
        Log.d("MAINACTIVITY", "singleton alarm is " + AlarmApplication.singletonAlarm)
        if (AlarmApplication.singletonAlarm != null) {
            val alarmIntent =
                Intent(this, AlarmActivity::class.java).apply {
                    putExtra(ALARM_INTENT_EXTRA_ALARM, AlarmApplication.singletonAlarm)
                    putExtra(ALARM_INTENT_EXTRA_MINIGAME_ID, AlarmApplication.singletonMinigameId)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
            startActivity(alarmIntent)
            finish()
            return true
        }
        return false
    }
}
