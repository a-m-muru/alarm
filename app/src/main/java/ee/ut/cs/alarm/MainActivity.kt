package ee.ut.cs.alarm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import ee.ut.cs.alarm.alarming.AlarmScheduler
import ee.ut.cs.alarm.ui.screens.AlarmListScreen
import ee.ut.cs.alarm.ui.theme.AlarmTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val alarmScheduler = AlarmScheduler(this)
        enableEdgeToEdge()
        setContent {
            AlarmTheme {
                // A state to manage which screen is currently shown
                var currentScreen by remember { mutableStateOf("main") }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AlarmListScreen(innerPadding)
                }
            }
        }
    }
}
