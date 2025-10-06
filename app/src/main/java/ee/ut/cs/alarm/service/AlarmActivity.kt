package ee.ut.cs.alarm.service

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ee.ut.cs.alarm.ui.theme.AlarmTheme

class AlarmActivity : ComponentActivity() {
    
    private val tag = "AlarmActivity"
    private var alarmId: String? = null
    private var alarmHour: Int = 0
    private var alarmMinute: Int = 0
    private var alarmLabel: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        Log.d(tag, "AlarmActivity onCreate called")
        
        alarmId = intent.getStringExtra("alarm_id")
        alarmHour = intent.getIntExtra("alarm_hour", 0)
        alarmMinute = intent.getIntExtra("alarm_minute", 0)
        alarmLabel = intent.getStringExtra("alarm_label")
        
        Log.d(tag, "AlarmActivity created - ID: $alarmId, Time: $alarmHour:$alarmMinute, Label: $alarmLabel")
        
        // If no valid alarm data, finish immediately
        if (alarmId == null || alarmId == "unknown" || (alarmHour == 0 && alarmMinute == 0)) {
            Log.w(tag, "No valid alarm data, finishing activity immediately")
            finish()
            return
        }
        
        // Make sure the activity appears on top and turns screen on
        window.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )
        
        Log.d(tag, "Setting up AlarmActivity UI with valid data")
        
        setContent {
            AlarmTheme {
                AlarmScreen(
                    hour = alarmHour,
                    minute = alarmMinute,
                    label = alarmLabel,
                    onDismiss = { dismissAlarm() },
                    onSnooze = { snoozeAlarm() }
                )
            }
        }
    }

    private fun dismissAlarm() {
        Log.d(tag, "Dismissing alarm")
        // Stop the alarm service
        val stopIntent = Intent(this, AlarmService::class.java)
        stopIntent.action = "STOP_ALARM"
        startService(stopIntent)
        
        finish()
    }

    private fun snoozeAlarm() {
        Log.d(tag, "Snoozing alarm")
        // Stop the alarm service
        val stopIntent = Intent(this, AlarmService::class.java)
        stopIntent.action = "STOP_ALARM"
        startService(stopIntent)
        
        Toast.makeText(this, "Alarm snoozed for 5 minutes", Toast.LENGTH_SHORT).show()
        // TODO: Implement snooze functionality
        finish()
    }
}

@Composable
fun AlarmScreen(
    hour: Int,
    minute: Int,
    label: String?,
    onDismiss: () -> Unit,
    onSnooze: () -> Unit
) {
    val timeString = String.format("%02d:%02d", hour, minute)
    
    // Add some debugging
    LaunchedEffect(Unit) {
        android.util.Log.d("AlarmScreen", "AlarmScreen composable created - Time: $timeString, Label: $label")
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            Text(
                text = "ALARM",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )
            
            Text(
                text = timeString,
                fontSize = 72.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            if (label != null && label.isNotBlank()) {
                Text(
                    text = label,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Button(
                    onClick = onSnooze,
                    modifier = Modifier.size(width = 120.dp, height = 60.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text("Snooze", fontSize = 18.sp)
                }
                
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.size(width = 120.dp, height = 60.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Dismiss", fontSize = 18.sp)
                }
            }
        }
    }
}
