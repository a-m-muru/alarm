package ee.ut.cs.alarm.gaming

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
import ee.ut.cs.alarm.Vec3
import kotlin.math.pow
import kotlin.math.round
import kotlin.math.sqrt


@Composable
fun JumpingJacks(onNavigateBack: () -> Unit){
    val context = LocalContext.current
    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }

    // Sensors
    val linearAccelerometer = remember { sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION) }
    val orientation = remember { sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) }

    // sensor data
    var accelerometerData by remember { mutableStateOf(Vec3(0f, 0f, 0f)) }

    //
    var currentCount by remember { mutableStateOf(0) }
    val neededCount = 8
    val victory by remember { mutableStateOf(false) }


    // rotation
    var isUpsideDown by remember { mutableStateOf(false) }
    var nextPos by remember { mutableStateOf(false) }
    var worldUpVector by remember { mutableStateOf(Vec3(0f, 0f, 0f)) }


    DisposableEffect(sensorManager){
        val sensorEventListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                when (event?.sensor?.type) {
                    Sensor.TYPE_LINEAR_ACCELERATION -> {
                        val acc = Vec3(event.values[0], event.values[1], event.values[2])
                        accelerometerData = acc
                    }

                    Sensor.TYPE_ROTATION_VECTOR -> {

                        val rotationMatrix = FloatArray(9)
                        SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                        val deviceUpVector = floatArrayOf(0f, 1f, 0f)

                        worldUpVector.x =
                            rotationMatrix[0] * deviceUpVector[0] + rotationMatrix[1] * deviceUpVector[1] + rotationMatrix[2] * deviceUpVector[2]
                        worldUpVector.y =
                            rotationMatrix[3] * deviceUpVector[0] + rotationMatrix[4] * deviceUpVector[1] + rotationMatrix[5] * deviceUpVector[2]
                        worldUpVector.z =
                            rotationMatrix[6] * deviceUpVector[0] + rotationMatrix[7] * deviceUpVector[1] + rotationMatrix[8] * deviceUpVector[2]

                        isUpsideDown = worldUpVector.z < 0
                    }
                }
                if (accelerometerData.length() > 40 && (nextPos == isUpsideDown) && (worldUpVector.z > 0.7 || worldUpVector.z < -0.8 )){
                    nextPos = !nextPos
                    currentCount++
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                // You can handle accuracy changes here if needed
            }
        }
        linearAccelerometer?.let {
            sensorManager.registerListener(sensorEventListener, it, SensorManager.SENSOR_DELAY_UI)
        }
        orientation?.let {
            sensorManager.registerListener(sensorEventListener, it, SensorManager.SENSOR_DELAY_UI)
        }
        // Cleanup: unregister listener when composable leaves the composition
        onDispose {
            sensorManager.unregisterListener(sensorEventListener)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Jumping Jacks", fontSize = 24.sp)
        Spacer(modifier = Modifier.height(32.dp))

        Text(text = "Count: $currentCount/$neededCount")
        Spacer(modifier = Modifier.height(24.dp))


        if (currentCount >= neededCount) {
            Text(text = "Congratulations! You did it!", fontSize = 24.sp)
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onNavigateBack) {
                Text("Go Back")
            }
        }

    }
}