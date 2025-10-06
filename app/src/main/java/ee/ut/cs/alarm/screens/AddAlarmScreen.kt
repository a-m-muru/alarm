package ee.ut.cs.alarm.screens

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import ee.ut.cs.alarm.navigation.Screen
import ee.ut.cs.alarm.components.ClockTimePicker
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAlarmScreen(navController: NavController? = null) {
    val context = LocalContext.current
    val alarmRepository = remember { AlarmRepository.getInstance(context) }
    val coroutineScope = rememberCoroutineScope()
    
    var selectedHour by remember { mutableStateOf(7) }
    var selectedMinute by remember { mutableStateOf(0) }
    var selectedDays by remember { mutableStateOf(setOf<DayOfWeek>()) }
    var label by remember { mutableStateOf("") }
    var isEnabled by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Add New Alarm",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ClockTimePicker(
                    hour = selectedHour,
                    minute = selectedMinute,
                    is24Hour = false, // Use 12-hour format with AM/PM
                    onHourChange = { selectedHour = it },
                    onMinuteChange = { selectedMinute = it }
                )

                Text(
                    text = "Days",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )

                // Day selection chips
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DayOfWeek.values().forEach { day ->
                        FilterChip(
                            onClick = {
                                selectedDays = if (day in selectedDays) {
                                    selectedDays - day
                                } else {
                                    selectedDays + day
                                }
                            },
                            label = { 
                                Text(
                                    text = day.displayName,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                ) 
                            },
                            selected = day in selectedDays,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                TextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Label (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Enable Alarm",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Switch(
                        checked = isEnabled,
                        onCheckedChange = { isEnabled = it }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { 
                if (selectedDays.isNotEmpty()) {
                    val alarm = Alarm(
                        hour = selectedHour,
                        minute = selectedMinute,
                        days = selectedDays.toList(),
                        enabled = isEnabled,
                        label = label.takeIf { it.isNotBlank() }
                    )
                    coroutineScope.launch {
                        alarmRepository.saveAlarm(alarm)
                        navController?.navigate(Screen.Alarms.route)
                    }
                }
            },
            enabled = selectedDays.isNotEmpty(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Alarm", fontSize = 16.sp)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = { 
                // Test alarm that triggers in 10 seconds
                val testAlarm = Alarm(
                    hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
                    minute = Calendar.getInstance().get(Calendar.MINUTE) + 1, // 1 minute from now
                    days = listOf(DayOfWeek.values()[Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1]),
                    enabled = true,
                    label = "Test Alarm"
                )
                coroutineScope.launch {
                    alarmRepository.saveAlarm(testAlarm)
                    navController?.navigate(Screen.Alarms.route)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary
            )
        ) {
            Text("Test Alarm (1 min)", fontSize = 16.sp)
        }
    }
}
