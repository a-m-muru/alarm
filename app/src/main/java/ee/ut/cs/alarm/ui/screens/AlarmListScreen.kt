package ee.ut.cs.alarm.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ee.ut.cs.alarm.data.Alarm
import ee.ut.cs.alarm.service.AlarmScheduler
import ee.ut.cs.alarm.ui.components.AlarmCard
import ee.ut.cs.alarm.ui.components.EditAlarmDialog
import ee.ut.cs.alarm.ui.navigation.Screen
import ee.ut.cs.alarm.ui.viewmodel.AlarmListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmListScreen(
    onNavigate: (String) -> Unit,
    vm: AlarmListViewModel,
    alarmScheduler: AlarmScheduler
) {
    var expanded by remember { mutableStateOf(false) }
    val alarms by vm.items.collectAsState()

    var showDialog by remember { mutableStateOf(false) }
    var editableAlarm by remember { mutableStateOf(Alarm()) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("All alarms") },
                actions = {
                    Box {
                        IconButton(onClick = { expanded = !expanded }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More")
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("About") },
                                onClick = { onNavigate(Screen.About.route) }
                            )
                        }
                    }
                }
            )
         },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editableAlarm = Alarm()
                    showDialog = true
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (alarms.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No alarms set\nTap + to add your first alarm",
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize().padding(bottom = 48.dp)) {
                    items(
                        items = alarms,
                        key = { it.id }
                    ) { alarm ->
                        AlarmCard(
                            alarm = alarm,
                            cardToggled = {
                                enabled ->
                                    val al = alarm.copy(enabled=enabled) // ??? Is creating a copy needed
                                    vm.updateItem(al)
                                    if (enabled) {
                                        alarmScheduler.scheduleAlarm(al)
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
                                editableAlarm = alarm
                            }
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
                            vm.updateItem(alarmToSave)
                        } else {
                            vm.addAlarm(alarmToSave)
                        }
                        alarmScheduler.scheduleAlarm(alarmToSave)
                        showDialog = false
                    },
                    alarmScheduler = alarmScheduler
                )
            }
        }
    }
}