package ee.ut.cs.alarm.ui.screens

import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ee.ut.cs.alarm.data.Alarm
import ee.ut.cs.alarm.service.AlarmScheduler
import ee.ut.cs.alarm.ui.components.AlarmCard
import ee.ut.cs.alarm.ui.components.EditAlarmDialog
import ee.ut.cs.alarm.ui.navigation.Screen
import ee.ut.cs.alarm.ui.viewmodel.AlarmListViewModel
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmListScreen(
    onNavigate: (String) -> Unit,
    vm: AlarmListViewModel,
    alarmScheduler: AlarmScheduler,
) {
    val alarms by vm.items.collectAsState()
    val streak by vm.streak.collectAsState()
    val prevStreak by vm.previousStreak.collectAsState()
    val context = LocalContext.current

    /**
     * Show toast with time till alarm
     * @param secondsToAlarm Time till alarm in seconds. Negative values are ignored.
     */
    fun showTimeTillAlarmToast(secondsToAlarm: Long) {
        if (secondsToAlarm < 0) return

        val days = secondsToAlarm / (24 * 3600)
        val hours = (secondsToAlarm % (24 * 3600)) / 3600
        val minutes = (secondsToAlarm % 3600) / 60

        val text =
            listOfNotNull(
                if (days > 0) "$days day${if (days > 1) "s" else ""}" else null,
                if (hours > 0) "$hours hour${if (hours > 1) "s" else ""}" else null,
                if (minutes > 0) "$minutes minute${if (minutes > 1) "s" else ""}" else null,
            ).joinToString(", ").ifEmpty { "less than a minute" }

        Toast.makeText(context, "$text till alarm", Toast.LENGTH_LONG).show()
    }

    var showDialog by remember { mutableStateOf(false) }
    var editableAlarm by remember { mutableStateOf(Alarm()) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            AlarmTopBar(onNavigate, vm)
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editableAlarm =
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            Alarm(time = LocalTime.now().toSecondOfDay().toUInt() + 60u)
                        } else {
                            Alarm(
                                time = System.currentTimeMillis().toUInt() / 1000u % (24u * 3600u) + 60u,
                            )
                        }
                    showDialog = true
                },
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        },
    ) { padding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding),
        ) {
            StreakBox(streak, prevStreak)
            if (alarms.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "No alarms set\nTap + to add your first alarm",
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                LazyColumn(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(bottom = 48.dp),
                ) {
                    items(
                        items = alarms,
                        key = { it.id },
                    ) { alarm ->
                        AlarmCard(
                            alarm = alarm,
                            cardToggled = { enabled ->
                                val al = alarm.copy(enabled = enabled)
                                vm.updateAlarm(al)
                                if (enabled) {
                                    val secondsToAlarm = alarmScheduler.scheduleAlarm(al)
                                    showTimeTillAlarmToast(secondsToAlarm)
                                } else {
                                    alarmScheduler.cancelAlarm(alarm.id)
                                }
                            },
                            onDelete = {
                                alarmScheduler.cancelAlarm(alarm.id)
                                vm.removeAlarm(alarm)
                            },
                            onEdit = {
                                showDialog = true
                                editableAlarm = alarm.copy(enabled = true)
                            },
                        )
                    }
                }
            }

            if (showDialog) {
                EditAlarmDialog(
                    alarm = editableAlarm,
                    onDismissRequest = { showDialog = false },
                    onSaveRequest = { alarmToSave ->
                        if (vm.hasAlarm(alarmToSave)) {
                            alarmScheduler.cancelAlarm(alarmToSave.id)
                            vm.updateAlarm(alarmToSave)
                        } else {
                            vm.addAlarm(alarmToSave)
                        }
                        val secondsToAlarm = alarmScheduler.scheduleAlarm(alarmToSave)
                        showTimeTillAlarmToast(secondsToAlarm)
                        showDialog = false
                    },
                    alarmScheduler = alarmScheduler,
                )
            }
        }
    }
}

// thanks chat geepeetee
@Composable
fun StreakBox(
    streak: Int,
    previousStreak: Int,
    modifier: Modifier = Modifier
) {
    val increased = streak > previousStreak
    val color = when {
        increased -> Color(0xFF4CAF50) // green
        streak == previousStreak -> Color(0xFF2196F3) // blue
        else -> Color(0xFFF44336) // red
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(color = color, shape = RoundedCornerShape(8.dp))
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Streak: $streak", color = Color.White, style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.width(12.dp))
            if (increased) {
                Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Increased", tint = Color.White)
            } else if (streak < previousStreak) {
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Decreased", tint = Color.White)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmTopBar(onNavigate: (String) -> Unit, vm: AlarmListViewModel) {
    var expanded by remember { mutableStateOf(false) }

    TopAppBar(
        title = { Text("All alarms") },
        actions = {
            Box(contentAlignment = Alignment.TopStart) {
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More")
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                ) {
                    DropdownMenuItem(
                        text = { Text("About") },
                        onClick = { onNavigate(Screen.About.route) },
                    )
                    DropdownMenuItem(
                        text = { Text("Settings") },
                        onClick = { onNavigate(Screen.Settings.route) },
                    )
                }
            }
        },
    )
}
