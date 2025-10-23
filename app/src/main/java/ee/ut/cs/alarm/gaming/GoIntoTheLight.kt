package ee.ut.cs.alarm.gaming

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ee.ut.cs.alarm.data.Weather
import kotlin.coroutines.suspendCoroutine

val WAIT_MINUTES_BEFORE_GRANTING_MERCY_IF_CANT_REACH_REQUIRED_LIGHT_LEVEL = 5

private fun calculateRequiredLightAmount(initialLight: Float, secondsPassed: Float): Float {
    return initialLight * 2f - (secondsPassed / (WAIT_MINUTES_BEFORE_GRANTING_MERCY_IF_CANT_REACH_REQUIRED_LIGHT_LEVEL * 60f))
        .coerceAtLeast(0f)
}

@Composable
fun GoIntoTheLight(onNavigateBack: () -> Unit) {

    val context = LocalContext.current
    val sensorManager =
        remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }

    val lightSensor = remember { sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT) }

    val startTime = remember { System.currentTimeMillis() }
    var lightAmount by remember { mutableFloatStateOf(0f) }
    var initialLightAmount by remember { mutableFloatStateOf(-1f) }
    var targetLightAmount by remember { mutableFloatStateOf(-1f) }
    var victory by remember { mutableStateOf(false) }

    DisposableEffect(sensorManager) {
        val sensorEventListener = object : SensorEventListener {
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                // TODO("Not yet implemented")
            }

            override fun onSensorChanged(event: SensorEvent?) {
                when (event?.sensor?.type) {
                    Sensor.TYPE_LIGHT -> {
                        lightUpdate(event.values[0])
                    }

                    else -> Log.e("ALARM", "Sensor event type incompatible ($event)")
                }
            }

            fun lightUpdate(light: Float) {
                lightAmount = light
                if (initialLightAmount == -1f) {
                    initialLightAmount = lightAmount.coerceAtLeast(5f)
                }
                val timePassed: Float = (System.currentTimeMillis() - startTime) / 1000f
                targetLightAmount = calculateRequiredLightAmount(initialLightAmount, timePassed)


                if (lightAmount >= targetLightAmount) {
                    victory = true
                }
            }

        }
        lightSensor?.let {
            sensorManager.registerListener(
                sensorEventListener,
                it,
                SensorManager.SENSOR_DELAY_GAME
            )
        }
        // Cleanup: unregister listener when composable leaves the composition
        onDispose {
            sensorManager.unregisterListener(sensorEventListener)
        }
    }


    Column(
        modifier = androidx.compose.ui.Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Enter the LIGHT", fontSize = 24.sp)
        Text("Current light: %.2f lux".format(lightAmount))
        Text("You need at least %.2f lux".format(targetLightAmount), fontSize = 18.sp)

        if (victory) {
            Text(text = "Congratulations! You did it!", fontSize = 24.sp)
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onNavigateBack) {
                Text("Go Back")
            }
        }
    }

}