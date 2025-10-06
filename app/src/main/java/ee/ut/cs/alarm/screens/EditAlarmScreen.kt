package ee.ut.cs.alarm.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ee.ut.cs.alarm.data.Alarm
import ee.ut.cs.alarm.data.AlarmRepository
import ee.ut.cs.alarm.data.DayOfWeek
import ee.ut.cs.alarm.components.ClockTimePicker
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditAlarmScreen(
    alarmId: String,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val alarmRepository = remember { AlarmRepository.getInstance(context) }
    val coroutineScope = rememberCoroutineScope()
    
    val alarms by alarmRepository.alarms.collectAsState(initial = emptyList())
    val alarm = remember(alarms, alarmId) { 
        alarms.find { it.id == alarmId }
    }
    
    // Track if we've loaded the initial data
    var hasLoadedInitialData by remember { mutableStateOf(false) }
    
    // Only navigate back if we've loaded data and still can't find the alarm
    LaunchedEffect(alarms, alarmId) {
        if (alarms.isNotEmpty()) {
            hasLoadedInitialData = true
            if (alarm == null && alarmId.isNotEmpty()) {
                onNavigateBack()
            }
        }
    }
    
    // Show loading state while data is loading
    if (!hasLoadedInitialData) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }
    
    // If alarm not found after loading, this will be handled by LaunchedEffect
    if (alarm == null) {
        return
    }
    
    var hour by remember { mutableStateOf(alarm.hour.toString()) }
    var minute by remember { mutableStateOf(alarm.minute.toString()) }
    var label by remember { mutableStateOf(alarm.label ?: "") }
    var selectedDays by remember { mutableStateOf(alarm.days.toSet()) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Edit Alarm",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        
        ClockTimePicker(
            hour = hour.toIntOrNull() ?: 0,
            minute = minute.toIntOrNull() ?: 0,
            is24Hour = false, // Use 12-hour format with AM/PM
            onHourChange = { hour = it.toString() },
            onMinuteChange = { minute = it.toString() }
        )
        
        // Label input
        Text(
            text = "Label (Optional)",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
        
        OutlinedTextField(
            value = label,
            onValueChange = { label = it },
            label = { Text("Alarm label") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        // Day selection
        Text(
            text = "Repeat",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DayOfWeek.values().forEach { day ->
                FilterChip(
                    onClick = {
                        selectedDays = if (selectedDays.contains(day)) {
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
                    selected = selectedDays.contains(day),
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Action buttons
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = onNavigateBack,
                modifier = Modifier.weight(1f)
            ) {
                Text("Cancel")
            }
            
            Button(
                onClick = {
                    val hourInt = hour.toIntOrNull() ?: 0
                    val minuteInt = minute.toIntOrNull() ?: 0
                    
                    if (hourInt in 0..23 && minuteInt in 0..59 && selectedDays.isNotEmpty()) {
                        val updatedAlarm = alarm.copy(
                            hour = hourInt,
                            minute = minuteInt,
                            label = label.takeIf { it.isNotBlank() },
                            days = selectedDays.toList()
                        )
                        
                        coroutineScope.launch {
                            alarmRepository.saveAlarm(updatedAlarm)
                            onNavigateBack()
                        }
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = hour.toIntOrNull()?.let { it in 0..23 } == true &&
                         minute.toIntOrNull()?.let { it in 0..59 } == true &&
                         selectedDays.isNotEmpty()
            ) {
                Text("Save")
            }
        }
    }
}
