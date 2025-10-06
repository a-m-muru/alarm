package ee.ut.cs.alarm.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.*

@Composable
fun ClockTimePicker(
    hour: Int,
    minute: Int,
    is24Hour: Boolean = false,
    onHourChange: (Int) -> Unit,
    onMinuteChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var isSelectingHour by remember { mutableStateOf(true) }
    var isAM by remember { mutableStateOf(hour < 12) }
    
    val displayHour = if (is24Hour) hour else {
        when {
            hour == 0 -> 12
            hour > 12 -> hour - 12
            else -> hour
        }
    }
    
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // AM/PM selector for 12-hour format
        if (!is24Hour) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    onClick = { 
                        isAM = true
                        // Trigger hour update when switching to AM
                        val newHour = if (hour >= 12) hour - 12 else hour
                        onHourChange(newHour)
                    },
                    label = { Text("AM") },
                    selected = isAM,
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    onClick = { 
                        isAM = false
                        // Trigger hour update when switching to PM
                        val newHour = if (hour < 12) hour + 12 else hour
                        onHourChange(newHour)
                    },
                    label = { Text("PM") },
                    selected = !isAM,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        // Hour/Minute selector buttons
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { isSelectingHour = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSelectingHour) MaterialTheme.colorScheme.primary 
                    else MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.weight(1f)
            ) {
                Text("Hour")
            }
            Button(
                onClick = { isSelectingHour = false },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (!isSelectingHour) MaterialTheme.colorScheme.primary 
                    else MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.weight(1f)
            ) {
                Text("Minute")
            }
        }
        
        // Clock face
        Box(
            modifier = Modifier
                .size(300.dp)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            ClockFace(
                hour = displayHour,
                minute = minute,
                isSelectingHour = isSelectingHour,
                onHourChange = { newHour ->
                    val actualHour = if (is24Hour) newHour else {
                        if (isAM) {
                            if (newHour == 12) 0 else newHour
                        } else {
                            if (newHour == 12) 12 else newHour + 12
                        }
                    }
                    onHourChange(actualHour)
                },
                onMinuteChange = onMinuteChange
            )
        }
        
        // Current time display
        Text(
            text = String.format("%02d:%02d", hour, minute),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun ClockFace(
    hour: Int,
    minute: Int,
    isSelectingHour: Boolean,
    onHourChange: (Int) -> Unit,
    onMinuteChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(isSelectingHour) {
                detectDragGestures(
                    onDragStart = { offset ->
                        val canvasSize = size
                        val center = Offset(canvasSize.width / 2f, canvasSize.height / 2f)
                        
                        // Calculate angle from center to drag start point
                        val dx = offset.x - center.x
                        val dy = offset.y - center.y
                        val angle = atan2(dy, dx) * 180 / PI
                        val normalizedAngle = (angle + 90 + 360) % 360
                        
                        if (isSelectingHour) {
                            // Convert angle to hour (1-12)
                            val newHour = ((normalizedAngle / 30) + 1).toInt().let { h ->
                                if (h > 12) h - 12 else h
                            }
                            onHourChange(newHour)
                        } else {
                            // Convert angle to minute (0-59)
                            val newMinute = ((normalizedAngle / 6) + 0.5).toInt() % 60
                            onMinuteChange(newMinute)
                        }
                    },
                    onDrag = { change, _ ->
                        val canvasSize = size
                        val center = Offset(canvasSize.width / 2f, canvasSize.height / 2f)
                        
                        // Calculate angle from center to current finger position
                        val dx = change.position.x - center.x
                        val dy = change.position.y - center.y
                        val angle = atan2(dy, dx) * 180 / PI
                        val normalizedAngle = (angle + 90 + 360) % 360
                        
                        if (isSelectingHour) {
                            // Convert angle to hour (1-12)
                            val newHour = ((normalizedAngle / 30) + 1).toInt().let { h ->
                                if (h > 12) h - 12 else h
                            }
                            onHourChange(newHour)
                        } else {
                            // Convert angle to minute (0-59)
                            val newMinute = ((normalizedAngle / 6) + 0.5).toInt() % 60
                            onMinuteChange(newMinute)
                        }
                    }
                )
            }
    ) {
        val center = size.center
        val radius = minOf(size.width, size.height) / 2 - 20.dp.toPx()
        
        // Draw clock face
        drawCircle(
            color = surfaceColor,
            radius = radius,
            center = center,
            style = Stroke(width = 4.dp.toPx())
        )
        
        // Draw hour markers
        for (i in 1..12) {
            val angle = (i * 30 - 90) * (PI / 180)
            val markerRadius = radius - 30.dp.toPx()
            val x = center.x + cos(angle).toFloat() * markerRadius
            val y = center.y + sin(angle).toFloat() * markerRadius
            
            drawCircle(
                color = onSurfaceColor,
                radius = 8.dp.toPx(),
                center = Offset(x, y)
            )
        }
        
        // Draw minute markers
        for (i in 0..59) {
            if (i % 5 != 0) { // Skip positions where hour markers are
                val angle = (i * 6 - 90) * (PI / 180)
                val markerRadius = radius - 20.dp.toPx()
                val x = center.x + cos(angle).toFloat() * markerRadius
                val y = center.y + sin(angle).toFloat() * markerRadius
                
                drawCircle(
                    color = onSurfaceColor.copy(alpha = 0.5f),
                    radius = 3.dp.toPx(),
                    center = Offset(x, y)
                )
            }
        }
        
        // Draw hour hand
        val hourAngle = if (isSelectingHour) {
            (hour * 30 - 90) * (PI / 180)
        } else {
            ((hour + minute / 60.0) * 30 - 90) * (PI / 180)
        }
        val hourHandLength = radius * 0.6f
        val hourHandEndX = center.x + cos(hourAngle).toFloat() * hourHandLength
        val hourHandEndY = center.y + sin(hourAngle).toFloat() * hourHandLength
        
        drawLine(
            color = primaryColor,
            start = center,
            end = Offset(hourHandEndX, hourHandEndY),
            strokeWidth = 6.dp.toPx()
        )
        
        // Draw minute hand
        val minuteAngle = if (isSelectingHour) {
            (minute * 6 - 90) * (PI / 180)
        } else {
            (minute * 6 - 90) * (PI / 180)
        }
        val minuteHandLength = radius * 0.8f
        val minuteHandEndX = center.x + cos(minuteAngle).toFloat() * minuteHandLength
        val minuteHandEndY = center.y + sin(minuteAngle).toFloat() * minuteHandLength
        
        drawLine(
            color = if (isSelectingHour) onSurfaceColor.copy(alpha = 0.5f) 
            else secondaryColor,
            start = center,
            end = Offset(minuteHandEndX, minuteHandEndY),
            strokeWidth = 4.dp.toPx()
        )
        
        // Draw center dot
        drawCircle(
            color = primaryColor,
            radius = 8.dp.toPx(),
            center = center
        )
    }
}