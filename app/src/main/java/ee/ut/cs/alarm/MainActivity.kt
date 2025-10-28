package ee.ut.cs.alarm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import ee.ut.cs.alarm.ui.navigation.AlarmNavigation
import ee.ut.cs.alarm.ui.theme.AlarmTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        enableEdgeToEdge()
        setContent {
            AlarmTheme {

                val navController = rememberNavController()

                AlarmNavigation(
                    navController = navController
                )
            }
        }
    }
}