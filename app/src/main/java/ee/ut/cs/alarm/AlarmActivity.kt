package ee.ut.cs.alarm

import android.content.Context
import android.media.PlaybackParams
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
import ee.ut.cs.alarm.gaming.AudioPlayer
import ee.ut.cs.alarm.gaming.Vec2
import ee.ut.cs.alarm.gaming.GameLoob
import ee.ut.cs.alarm.gaming.JumpingJacks
import ee.ut.cs.alarm.ui.theme.AlarmTheme
import kotlin.random.Random

class AlarmActivity : ComponentActivity() {

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

    @Composable
    fun AlarmScreen(ctx: Context, modifier: Modifier) {
//        Text("asdasdasd")
//        GameLoob(this)
//        Button(
//            onClick = { AudioPlayer.playSound(this, R.raw.bump) }
//        ) {
//            Text("yeyeyayhs")
//        }
        val timeString = "07:07"
        val dateString = "Sunday Jul 7"

        Column(
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))
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
                minigameScreen()
            }
        }
    }

    @Composable
    fun minigameScreen() {
        JumpingJacks(
            onNavigateBack = { finish() }
        )
    }

}
