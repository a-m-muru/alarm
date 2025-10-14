package ee.ut.cs.alarm

import android.app.ActivityManager
import android.content.Context
import android.media.PlaybackParams
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import ee.ut.cs.alarm.gaming.AudioPlayer
import ee.ut.cs.alarm.gaming.JumpingJacks
import ee.ut.cs.alarm.ui.theme.AlarmTheme
import kotlin.random.Random


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

        Log.d("AlarmActivity", "We are alarming you!! Get alarmed")
        enableEdgeToEdge()
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())

        setContent {
            BackHandler(enabled = true) {

            }
            AlarmTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AlarmScreen(this, Modifier.padding(innerPadding))
                }
            }
        }
    }

    @Composable
    fun AlarmScreen(ctx: Context, modifier: Modifier) {
        val timeString = "07:07"
        val dateString = "Sunday Jul 7"

        Column(
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            Text("You have the alarm!", fontSize = 24.sp)
            Text(timeString, fontSize = 84.sp, fontWeight = FontWeight.Bold)
            Text(dateString, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    AudioPlayer.playSound(
                        ctx,
                        R.raw.bump,
                        PlaybackParams().setPitch(Random.nextFloat() + 1.0f)
                    )
                }
            ) { Text("Sneeze 7 min") }
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
        JumpingJacks(
            onNavigateBack = { finish() }
        )
    }

}
