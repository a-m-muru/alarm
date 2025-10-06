package ee.ut.cs.alarm.screens

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ee.ut.cs.alarm.data.Alarm
import ee.ut.cs.alarm.data.AlarmRepository
import ee.ut.cs.alarm.data.DayOfWeek
import ee.ut.cs.alarm.service.WorkManagerAlarmScheduler
import kotlinx.coroutines.launch
import android.util.Log

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmsScreen(navController: androidx.navigation.NavHostController? = null) {
    val context = LocalContext.current
    val alarmRepository = remember { AlarmRepository.getInstance(context) }
    val alarmScheduler = remember { WorkManagerAlarmScheduler(context) }
    val coroutineScope = rememberCoroutineScope()
    
    val alarms by alarmRepository.alarms.collectAsState(initial = emptyList())

    LaunchedEffect(alarms) {
        Log.d("AlarmsScreen", "Alarms changed: ${alarms.size} alarms")
        alarmScheduler.rescheduleAllAlarms(alarms)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "My Alarms",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (alarms.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No alarms set\nTap + to add your first alarm",
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(alarms) { alarm ->
                    AlarmCard(
                        alarm = alarm,
                        onToggleEnabled = { enabled ->
                            coroutineScope.launch {
                                alarmRepository.updateAlarmEnabled(alarm.id, enabled)
                            }
                        },
                        onEdit = { 
                            navController?.navigate("edit_alarm/${alarm.id}")
                        },
                        onDelete = {
                            coroutineScope.launch {
                                alarmRepository.deleteAlarm(alarm.id)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AlarmCard(
    alarm: Alarm,
    onToggleEnabled: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val timeString = String.format("%02d:%02d", alarm.hour, alarm.minute)
    val daysString = alarm.days.joinToString(", ") { it.displayName }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = timeString,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = daysString,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (alarm.label != null) {
                    Text(
                        text = alarm.label,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit alarm"
                    )
                }
                
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete alarm",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
                
                Switch(
                    checked = alarm.enabled,
                    onCheckedChange = onToggleEnabled
                )
            }
        }
    }
}
