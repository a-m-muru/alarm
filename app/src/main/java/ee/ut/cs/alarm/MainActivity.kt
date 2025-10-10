package ee.ut.cs.alarm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ee.ut.cs.alarm.alarming.AlarmScheduler
import ee.ut.cs.alarm.data.Alarm
import ee.ut.cs.alarm.gaming.JumpingJacks
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
                    when (currentScreen) {
                        "main" -> MainScreen(
                            modifier = Modifier.padding(innerPadding),
                            onNavigateToSensors = { currentScreen = "sensors" },
                            onNavigateToJumpingJacks = { currentScreen = "jumpingjacks" },
                            createAlarm = { alarmScheduler.scheduleAlarm(Alarm(1u, 0)) }
                        )

                        "sensors" -> SensorScreen(
                            onNavigateBack = { currentScreen = "main" }
                        )

                        "jumpingjacks" -> JumpingJacks(
                            onNavigateBack = { currentScreen = "main" }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    onNavigateToSensors: () -> Unit,
    onNavigateToJumpingJacks: () -> Unit,
    createAlarm: () -> Unit
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Greeting(name = "Android")
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onNavigateToSensors) {
            Text("Show Sensor Info")
        }

        Button(onClick = onNavigateToJumpingJacks) {
            Text("Jumping Jacks")
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = createAlarm) {
            Text("Give me Alarm....")
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    AlarmTheme {
        Greeting("Android")
    }
}