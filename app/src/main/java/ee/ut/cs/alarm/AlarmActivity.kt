package ee.ut.cs.alarm

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ee.ut.cs.alarm.gaming.AudioPlayer
import ee.ut.cs.alarm.gaming.GameLoob
import ee.ut.cs.alarm.ui.theme.AlarmTheme

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
            Text(timeString, fontSize = 84.sp)
            Text(dateString, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { AudioPlayer.playSound(ctx, R.raw.bump) }
            ) { Text("Sneeze 7 min") }
        }
    }

}
