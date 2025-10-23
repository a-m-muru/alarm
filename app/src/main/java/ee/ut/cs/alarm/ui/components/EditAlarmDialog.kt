package ee.ut.cs.alarm.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import ee.ut.cs.alarm.data.Alarm
import ee.ut.cs.alarm.data.Day
import kotlin.experimental.and
import kotlin.experimental.xor

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun EditAlarmDialog(
    alarm: Alarm,
    onDismissRequest: () -> Unit,
    onSaveRequest: (Alarm) -> Unit
) {
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
                modifier = Modifier.fillMaxSize().padding(padding),
                shape = RoundedCornerShape(0.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    TimePicker(state = timePickerState)
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
                            onSaveRequest(alarm.copy(
                                time = (timePickerState.hour * 3600 + timePickerState.minute * 60).toUInt(),
                                label = alarmLabel.text,
                                days = selectedDays
                            ))
                        }
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}