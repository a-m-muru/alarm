package ee.ut.cs.alarm

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SensorScreen(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }

    // Attempt to get the accelerometer and light sensor
    val accelerometer = remember { sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) }
    val lightSensor = remember { sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT) }

    // State to hold sensor data strings
    var accelerometerData by remember { mutableStateOf("Waiting for data...") }
    var lightSensorData by remember { mutableStateOf("Waiting for data...") }

    // This effect will register and unregister the sensor listener
    DisposableEffect(sensorManager) {
        val sensorEventListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                when (event?.sensor?.type) {
                    Sensor.TYPE_ACCELEROMETER -> {
                        val x = event.values[0]
                        val y = event.values[1]
                        val z = event.values[2]
                        accelerometerData = String.format("X: %.2f, Y: %.2f, Z: %.2f", x, y, z)
                    }
                    Sensor.TYPE_LIGHT -> {
                        val lux = event.values[0]
                        lightSensorData = "Light: %.1f lx".format(lux)
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                // You can handle accuracy changes here if needed
            }
        }

        // Register listeners for the sensors that exist
        accelerometer?.let {
            sensorManager.registerListener(sensorEventListener, it, SensorManager.SENSOR_DELAY_UI)
        }
        lightSensor?.let {
            sensorManager.registerListener(sensorEventListener, it, SensorManager.SENSOR_DELAY_UI)
        }

        // Cleanup: unregister listener when composable leaves the composition
        onDispose {
            sensorManager.unregisterListener(sensorEventListener)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Sensor Information", fontSize = 24.sp)
        Spacer(modifier = Modifier.height(32.dp))

        Text(text = "Accelerometer:")
        Text(text = if (accelerometer != null) accelerometerData else "Not available", fontSize = 18.sp)

        Spacer(modifier = Modifier.height(24.dp))

        Text(text = "Light Sensor:")
        Text(text = if (lightSensor != null) lightSensorData else "Not available", fontSize = 18.sp)

        Spacer(modifier = Modifier.height(48.dp))

        Button(onClick = onNavigateBack) {
            Text("Go Back")
        }
    }
}
