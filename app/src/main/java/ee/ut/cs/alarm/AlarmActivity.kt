package ee.ut.cs.alarm

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import ee.ut.cs.alarm.ui.theme.AlarmTheme

class AlarmActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("AlarmActivity", "adaisdasdadsgfdsfalökdgjnfsdlkfjdlsfjldsfj")

        setContent {
            AlarmTheme {
                AlarmScreen()
            }
        }
    }
}

@Composable
fun AlarmScreen() {
    Text("asdasdasd")
}