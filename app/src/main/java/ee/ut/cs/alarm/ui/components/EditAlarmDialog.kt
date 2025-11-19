package ee.ut.cs.alarm.ui.components

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.semantics.SemanticsPropertyReceiver
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import ee.ut.cs.alarm.data.Alarm
import ee.ut.cs.alarm.data.Day
import ee.ut.cs.alarm.service.AlarmScheduler
import kotlinx.coroutines.launch
import kotlin.experimental.and
import kotlin.experimental.xor

object EditAlarmDialogTestTags {
    const val TIME_PICKER = "timePicker"
}

val SetTimeSemanticsKey = SemanticsPropertyKey<(Int, Int) -> Boolean>("SetTimeAction")
var SemanticsPropertyReceiver.setTimeAction by SetTimeSemanticsKey

@Composable
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
fun EditAlarmDialog(
    alarm: Alarm,
    onDismissRequest: () -> Unit,
    onSaveRequest: (Alarm) -> Unit,
    alarmScheduler: AlarmScheduler? = null
) {
    val context = LocalContext.current
    val scheduler = remember(alarmScheduler, context) {
        alarmScheduler ?: AlarmScheduler(context)
    }
    val scope = rememberCoroutineScope()

    val openSettingsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        // This block runs when the user returns from the settings screen.
        // We re-check the permission. If granted, we can proceed with saving.
        // A more advanced implementation could store the alarm and save it here.
        // For simplicity, we can ask the user to click "Save" again.
    }

    val timePickerState = rememberTimePickerState(
        initialHour = (alarm.time / 3600u).toInt(),
        initialMinute = ((alarm.time / 60u) % 60u).toInt(),
        is24Hour = true
    )

    val days = listOf(
        Pair("Mon", Day.MONDAY),
        Pair("Tue", Day.TUESDAY),
        Pair("Wed", Day.WEDNESDAY),
        Pair("Thu", Day.THURSDAY),
        Pair("Fri", Day.FRIDAY),
        Pair("Sat", Day.SATURDAY),
        Pair("Sun", Day.SUNDAY)
    )

    var selectedDays by remember { mutableStateOf(alarm.days) }


    var initialLabel = ""
    if (alarm.label != null)
        initialLabel = alarm.label.toString()

    var alarmLabel by remember { mutableStateOf(TextFieldValue(initialLabel)) }
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = { Text("Add or edit alarm") },
                    navigationIcon = {
                        IconButton(onClick = onDismissRequest) {
                            Icon(Icons.Outlined.Close, contentDescription = "Close dialog")
                        }
                    }
                )
            }
        ) { padding ->
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                shape = RoundedCornerShape(0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    TimePicker(
                        state = timePickerState,
                        modifier = Modifier
                            .testTag(EditAlarmDialogTestTags.TIME_PICKER)
                            .semantics {
                                setTimeAction = { hour, minute ->
                                    timePickerState.hour = hour
                                    timePickerState.minute = minute
                                    true
                                }
                            }
                    )
                    OutlinedTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        value = alarmLabel,
                        onValueChange = { newLabel ->
                            alarmLabel = newLabel
                        },
                        placeholder = { Text("Label") },
                        label = { Text("Alarm label") },
                        singleLine = true
                    )
                    FlowRow(
                        modifier = Modifier.padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        days.forEach { day ->
                            FilterChip(
                                onClick = {
                                    var dayVal = day.second.toUInt()
                                    var n = 0
                                    while (dayVal != 1.toUInt()) {
                                        dayVal = dayVal shr 1
                                        n++
                                    }

                                    selectedDays = selectedDays xor (1 shl n).toByte()
                                },
                                label = {
                                    Text(
                                        day.first,
                                        textAlign = TextAlign.Center,
                                        overflow = TextOverflow.Visible,
                                        maxLines = 1)
                                },
                                selected = (selectedDays and day.second) != 0.toByte(),
                            )
                        }
                    }

                    Button(
                        onClick = {
                            scope.launch {
                                val newAlarm = alarm.copy(
                                    time = (timePickerState.hour * 3600 + timePickerState.minute * 60).toUInt(),
                                    label = alarmLabel.text,
                                    days = selectedDays
                                )

                                if (scheduler.canScheduleExactAlarms()) {
                                    // Permission is granted, proceed with saving.
                                    onSaveRequest(newAlarm)
                                } else {
                                    // Permission is NOT granted.
                                    // Guide the user to the settings screen.
                                    // Consider showing a dialog first to explain why.
                                    val intent =
                                        Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                                    openSettingsLauncher.launch(intent)
                                }
                            }
                        }
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}