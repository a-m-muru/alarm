package ee.ut.cs.alarm.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ee.ut.cs.alarm.data.Alarm
import ee.ut.cs.alarm.data.Day
import kotlin.experimental.and

@Composable
fun AlarmCard(
    alarm: Alarm,
    onToggleEnabled: (Boolean) -> Unit,
    onEdit: (Alarm) -> Unit,
    onDelete: (Alarm) -> Unit
) {
    val timeString = String.format("%02d:%02d", (alarm.time / 3600u).toInt(), ((alarm.time / 60u) % 60u).toInt())
    val days: MutableList<String> = mutableListOf()
    if (alarm.days and Day.MONDAY != 0.toByte())
        days.add("Mon")
    if (alarm.days and Day.TUESDAY != 0.toByte())
        days.add("Tue")
    if (alarm.days and Day.WEDNESDAY != 0.toByte())
        days.add("Wed")
    if (alarm.days and Day.THURSDAY != 0.toByte())
        days.add("Thu")
    if (alarm.days and Day.FRIDAY != 0.toByte())
        days.add("Fri")
    if (alarm.days and Day.SATURDAY != 0.toByte())
        days.add("Sat")
    if (alarm.days and Day.SUNDAY != 0.toByte())
        days.add("Sun")
    val dayString = days.joinToString()

    val swipeToDismissBoxState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            if (it == SwipeToDismissBoxValue.StartToEnd)
                onEdit(alarm)
            else if (it == SwipeToDismissBoxValue.EndToStart)
                onDelete(alarm)

            it != SwipeToDismissBoxValue.StartToEnd
        }
    )

    SwipeToDismissBox(
        state = swipeToDismissBoxState,
        modifier = Modifier.fillMaxWidth(),
        backgroundContent = {
            when (swipeToDismissBoxState.dismissDirection) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit alarm",
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Green)
                            .wrapContentSize(Alignment.CenterStart)
                            .padding(12.dp),
                        tint = Color.White
                    )
                }
                SwipeToDismissBoxValue.EndToStart -> {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete alarm",
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Red)
                            .wrapContentSize(Alignment.CenterEnd)
                            .padding(12.dp),
                        tint = Color.White
                    )
                }
                SwipeToDismissBoxValue.Settled -> {}
            }
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = timeString, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                Text(text = dayString)
            }

            Switch(
                checked = alarm.enabled,
                onCheckedChange = onToggleEnabled
            )
        }
    }
}