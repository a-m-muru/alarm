package ee.ut.cs.alarm

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
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
import ee.ut.cs.alarm.gaming.GoIntoTheLight
import ee.ut.cs.alarm.ui.theme.AlarmTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Calendar


class AlarmActivity : ComponentActivity() {

    // this disables switching active apps
    override fun onPause() {
        super.onPause()

        val activityManager = getApplicationContext()
            .getSystemService(ACTIVITY_SERVICE) as ActivityManager

        activityManager.moveTaskToFront(getTaskId(), 0)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("AlarmActivity", "adaisdasdadsgfdsfalökdgjnfsdlkfjdlsfjldsfj")
        enableEdgeToEdge()
        setContent {
            AlarmTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AlarmScreen(this, Modifier.padding(innerPadding))
                }
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    @Composable
    fun AlarmScreen(ctx: Context, modifier: Modifier) {
//        Text("asdasdasd")
//        GameLoob(this)
//        Button(
//            onClick = { AudioPlayer.playSound(this, R.raw.bump) }
//        ) {
//            Text("yeyeyayhs")
//        }
        val alarm = intent.getParcelableExtra<Alarm>("alarm") as Alarm
        val cal = Calendar.getInstance()
        cal.setTimeInMillis(alarm.time.toLong())
        val timeFmt = DateFormat.getTimeInstance(DateFormat.SHORT)
        val dateFmt = SimpleDateFormat("EEEE dd MMMM")
        val timeString = timeFmt.format(cal.time).toString()
        val dateString = dateFmt.format(cal.get(Calendar.DAY_OF_WEEK_IN_MONTH))

        val coroutineScope = rememberCoroutineScope()
        var weather by remember { mutableStateOf(Weather(-1f, "-1", -1f)) }

        LaunchedEffect(Unit) {
            coroutineScope.launch {
                weather = withContext(Dispatchers.IO) {
                    Weather.fromRequest()
                }
            }
        }

        Column(
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Text("You have the alarm!", fontSize = 24.sp)
            Text(timeString, fontSize = 84.sp, fontWeight = FontWeight.Bold)
            Text(dateString, fontSize = 16.sp)
            Text(weather.getString(), fontSize = 12.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Column(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxSize()
                    .background(
                        Brush.sweepGradient(
                            listOf(
                                Color.Magenta,
                                Color.Blue,
                                Color.Black,
                                Color.Blue,
                                Color.Magenta
                            )
                        )
                    ),
                verticalArrangement = Arrangement.Center
            ) {
                MinigameScreen()
            }
        }
    }

    @Composable
    fun MinigameScreen() {
//        JumpingJacks(
//            onNavigateBack = { finish() }
//        )
        GoIntoTheLight(onNavigateBack = { finish() })
    }

}
