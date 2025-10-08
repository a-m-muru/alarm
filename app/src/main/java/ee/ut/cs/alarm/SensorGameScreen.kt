package ee.ut.cs.alarm

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventCallback
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import kotlin.math.pow
import kotlin.math.round
import kotlin.math.sqrt
import kotlin.time.Duration

data class Vec3(var x: Float, var y: Float, var z: Float) {
    operator fun plus(other: Vec3): Vec3 {
        return Vec3(x + other.x, y + other.y, z + other.z)
    }
    operator fun minus(other: Vec3): Vec3 {
        return Vec3(x - other.x, y - other.y, z - other.z)
    }
    operator fun times(scalar: Float): Vec3 {
        return Vec3(x * scalar, y * scalar, z * scalar)
    }
    operator fun div(scalar: Float): Vec3 {
        return Vec3(x / scalar, y / scalar, z / scalar)
    }
    fun length(): Float {
        return sqrt(x.pow(2) + y.pow(2) + z.pow(2))
    }
    fun normalize(): Vec3 {
        val len = length()
        return Vec3(x / len, y / len, z / len)
    }
    fun toFloatArray(): FloatArray {
        return floatArrayOf(x, y, z)
    }
    override fun toString(): String {
        return "(${round(x * 100.0f) / 100.0f}, ${round(y * 100.0f) / 100.0f}, ${round(z * 100.0f) / 100.0f})"
    }

}


@Composable
fun SensorScreen(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }

    // Attempt to get the accelerometer and light sensor
    //val accelerometer = remember { sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) }
    val linearAccelerometer = remember { sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION) }
    //val gyroscope = remember { sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) }
    val orientation = remember { sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) }



    // State to hold sensor data strings
    //var accelerometerDataString by remember { mutableStateOf("Waiting for data...") }
    var accelerometerData by remember { mutableStateOf(Vec3(0f, 0f, 0f)) }
    var gyroscopeData by remember { mutableStateOf( Vec3(0f, 0f, 0f)) }
    var count by remember { mutableStateOf(0) }
    var log by remember { mutableStateOf("") }
    var doLog by remember { mutableStateOf(false) }
    var paused by remember { mutableStateOf(false) }

    var isUpsideDown by remember { mutableStateOf(false) }
    var nextPos by remember { mutableStateOf(false) }
    var worldUpVector by remember { mutableStateOf(Vec3(0f, 0f, 0f)) }



    //var position by remember { mutableStateOf(Vec3(0f, 0f, 0f)) }
    //var velocity by remember { mutableStateOf(Vec3(0f, 0f, 0f)) }
    var lastTime by remember { mutableStateOf(System.currentTimeMillis()) }




    // This effect will register and unregister the sensor listener
    DisposableEffect(sensorManager) {
        val sensorEventListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (!paused) {
                    val deltaTime = (System.currentTimeMillis() - lastTime) / 1000.0f
                    lastTime = System.currentTimeMillis()

                    when (event?.sensor?.type) {
                        Sensor.TYPE_LINEAR_ACCELERATION -> {
                            val acc = Vec3(event.values[0], event.values[1], event.values[2])
                            accelerometerData = acc

                            //velocity = velocity + acc * deltaTime

                            //position = position + velocity * deltaTime

                            //position = position + getDistance(accelerometerData, gyroscopeData, deltaTime)


                            if (doLog) {
                                log += "ACC: " + acc + " l: " + acc.length() + "\n"
                                print(event.values)
                                //log += "VEL: " + velocity.toString() + "\n"
                                //log += "POS: " + position.toString() + "\n"
                            }
                        }
                        Sensor.TYPE_ROTATION_VECTOR -> {
                            gyroscopeData = Vec3(event.values[0], event.values[1], event.values[2])

                            val rotationMatrix = FloatArray(9)
                            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)

                            val deviceUpVector = floatArrayOf(0f, 1f, 0f)

                            //val worldUpVector = FloatArray(3)


                            worldUpVector.x = rotationMatrix[0] * deviceUpVector[0] + rotationMatrix[1] * deviceUpVector[1] + rotationMatrix[2] * deviceUpVector[2]
                            worldUpVector.y = rotationMatrix[3] * deviceUpVector[0] + rotationMatrix[4] * deviceUpVector[1] + rotationMatrix[5] * deviceUpVector[2]
                            worldUpVector.z = rotationMatrix[6] * deviceUpVector[0] + rotationMatrix[7] * deviceUpVector[1] + rotationMatrix[8] * deviceUpVector[2]


                            isUpsideDown = worldUpVector.z < 0


                            if (doLog) {
                                // You can log the Z-value to see how it changes
                                log += "ORI: " + event.values.toSet() + "\n"
                                log += "World: ${worldUpVector}\n"
                            }

                        }
                    }
                    if (accelerometerData.length() > 30 && (nextPos == isUpsideDown) && (worldUpVector.z > 0.7 || worldUpVector.z < -0.8 )){
                        nextPos = !nextPos
                        count++
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                // You can handle accuracy changes here if needed
            }
        }

        // Register listeners for the sensors that exist
        linearAccelerometer?.let {
            sensorManager.registerListener(sensorEventListener, it, SensorManager.SENSOR_DELAY_UI)
        }
        orientation?.let{
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
        Text(text = if (linearAccelerometer != null) accelerometerData.toString() else "Not available", fontSize = 18.sp)

        Spacer(modifier = Modifier.height(24.dp))
        Text(text = "Orientation:")
        Text(text = if (orientation != null) gyroscopeData.toString() else "Not available", fontSize = 18.sp)

        Spacer(modifier = Modifier.height(24.dp))
        //Text(text = "Position:")
        //Text(text = position.toString(), fontSize = 18.sp)
        Text(text = "World up: ${if (isUpsideDown) "yes" else "no"}", fontSize = 18.sp)
        Text(text = worldUpVector.toString(), fontSize = 18.sp)



        Spacer(modifier = Modifier.height(24.dp))
        Text(text = "Count:")
        Text(text = count.toString(), fontSize = 18.sp)


        Spacer(modifier = Modifier.height(48.dp))

        Button(onClick = onNavigateBack) {
            Text("Go Back")
        }

        Button(onClick = { paused = !paused }) {
            Text("Pause")
        }

        Button(onClick = { doLog = !doLog }) {
            Text("Log")
        }

        //Button(onClick = {
        //    position = Vec3(0f, 0f, 0f)
        //    velocity = Vec3(0f, 0f, 0f)
        //}) {
        //    Text("Reset Position")
        //}

        Spacer(modifier = Modifier.height(48.dp))
        Text(text = "Log:")
        Text(
            text = log,
            fontSize = 9.sp,
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())
        )
    }
}

fun length(values: FloatArray): Float {
    var sumOfSquares = 0.0f
    for (value in values) {
        sumOfSquares += value.pow(2)
    }
    return sqrt(sumOfSquares)
}

fun getDistance(acceleration: Vec3, gyroscopeData: Vec3, deltaTime: Float): Vec3 {
    //val distance = Vec3(0f, 0f, 0f)

    val distance = acceleration * (deltaTime.pow(2)) / 2.0f
    return distance
}

