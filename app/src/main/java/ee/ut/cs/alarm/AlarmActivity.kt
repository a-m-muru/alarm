package ee.ut.cs.alarm

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ee.ut.cs.alarm.data.Alarm
import ee.ut.cs.alarm.data.Weather
import ee.ut.cs.alarm.gaming.BalanceHole
import ee.ut.cs.alarm.gaming.GoIntoTheLight
import ee.ut.cs.alarm.gaming.JumpingJacks
import ee.ut.cs.alarm.service.AlarmForegroundService
import ee.ut.cs.alarm.ui.theme.AlarmTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Calendar

class AlarmActivity : ComponentActivity() {
    companion object {
        const val MAX_MINIGAMES = 5 // == 1 + maximum minigame id
    }

    private fun isDarkModeOn(): Boolean {
        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return currentNightMode == Configuration.UI_MODE_NIGHT_YES
    }

    // this disables switching active apps
    override fun onPause() {
        super.onPause()

        val activityManager =
            getApplicationContext()
                .getSystemService(ACTIVITY_SERVICE) as ActivityManager

        activityManager.moveTaskToFront(getTaskId(), 0)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("ALARM ACTIVITY", "creating alarm activity instance")
        Log.d("ALARM ACTIVITY", intent.toString())
        val alarm =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(ALARM_INTENT_EXTRA_ALARM, Alarm::class.java)
            } else {
                intent.getParcelableExtra<Alarm>(ALARM_INTENT_EXTRA_ALARM)
            }
        val gameId = intent.getIntExtra(ALARM_INTENT_EXTRA_MINIGAME_ID, -1)
        for (a in intent.extras?.keySet()!!) Log.d("ALARM ACTIVITY", "gthere is exta " + a)
        Log.d("ALARM ACTIVITY", "got minigame id " + gameId)
        Log.i("ALARM ACTIVITY", "" + alarm)

        val serviceIntent = Intent(this, AlarmForegroundService::class.java)
        serviceIntent.setAction(ACTION_STOP_VIBRATION)
        startService(serviceIntent)

        enableEdgeToEdge()
        setContent {
            AlarmTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AlarmScreen(Modifier.padding(innerPadding), gameId)
                }
            }
        }
    }

    fun end() {
        val serviceIntent = Intent(this, AlarmForegroundService::class.java)
        AlarmApplication.singletonAlarm = null
        AlarmApplication.singletonMinigameId = null
        serviceIntent.setAction(ACTION_STOP_ALARM) // Set the action to stop the alarm
        stopService(serviceIntent) // Stop the service
        finish()
    }

    @SuppressLint("SimpleDateFormat")
    @Composable
    fun AlarmScreen(
        modifier: Modifier,
        gameId: Int,
    ) {
//        Text("asdasdasd")
//        GameLoob(this)
//        Button(
//            onClick = { AudioPlayer.playSound(this, R.raw.bump) }
//        ) {
//            Text("yeyeyayhs")
//        }

        val cal = Calendar.getInstance()
        val timeFmt = DateFormat.getTimeInstance(DateFormat.SHORT)
        val dateFmt = SimpleDateFormat("EEEE dd MMMM")
        val timeString = timeFmt.format(cal.time).toString()
        val dateString = dateFmt.format(cal.time)

        val coroutineScope = rememberCoroutineScope()
        var weatherText by remember { mutableStateOf("Fetching weather...") }

        LaunchedEffect(Unit) {
            coroutineScope.launch {
                val weather =
                    withContext(Dispatchers.IO) {
                        Weather.fromRequest()
                    }
                weatherText = weather.getString()
            }
        }

        Column(
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Text("You have the alarm!", fontSize = 24.sp)
            Text(timeString, fontSize = 84.sp, fontWeight = FontWeight.Bold)
            Text(dateString, fontSize = 16.sp)
            Text(weatherText, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(16.dp))
            val colorList =
                if (isDarkModeOn()) {
                    listOf(
                        Color.Magenta,
                        Color.Blue,
                        Color.Black,
                        Color.Blue,
                        Color.Magenta,
                    )
                } else {
                    listOf(Color.White, Color.Yellow, Color.White, Color.Magenta, Color.White)
                }
            Column(
                modifier =
                    Modifier
                        .padding(8.dp)
                        .fillMaxSize()
                        .background(
                            Brush.sweepGradient(
                                colorList,
                            ),
                        ),
                verticalArrangement = Arrangement.Center,
            ) {
                MinigameScreen(gameId)
            }
        }
    }

    @Composable
    fun MinigameScreen(id: Int) {
        val navback = { end() }

        // var id = id
        // id = 2 // debug

        when (id) {
            0 -> JumpingJacks(onNavigateBack = navback)
            1 -> JumpingJacks(onNavigateBack = navback)
            2 -> GoIntoTheLight(onNavigateBack = navback)
            3 -> GoIntoTheLight(onNavigateBack = navback)
            4 -> BalanceHole(onNavigateBack = navback)
            else -> throw IllegalArgumentException("no minigame with id " + id)
        }
    }
}
