package ee.ut.cs.alarm.gaming

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import kotlin.math.sign
import kotlin.random.Random

// some tutorials that helped
// https://proandroiddev.com/classic-snake-game-with-jetpack-compose-2b78f4892ca
// https://medium.com/autodesk-tlv/how-to-write-games-for-android-and-ios-with-kotlin-in-jetpack-compose-b9ac35514238

class GameEngine {

    private var totalTime by mutableStateOf(0L)
    private var prevTime = 0L
    private var frames = 0L

    var rotation by mutableStateOf(Vec3(0f, 0f, 0f))

    var screenWidth by mutableStateOf(0.dp)
    var screenHeight by mutableStateOf(0.dp)

    var balls = mutableStateListOf<Ball>()

    fun startGame() {
        balls.clear()

        repeat(8) {
            var ball = Ball(pos = Vec2(Random.nextFloat() * 30 + 50, Random.nextFloat() * 30 + 50), radius = Random.nextFloat() * 20 + 10f)
            ball.vel = Vec2(Random.nextFloat() * 20f + -10f, Random.nextFloat() * 20f + -10f)

            balls.add(ball)
        }
    }

    fun update(time: Long) {
        val delta = time - prevTime
        val deltaT = (delta / 1E8).toFloat()
        totalTime += delta
        prevTime = time

        if (frames > 0L) {
            for (ball in balls) ball.update(deltaT, this)
        }
        frames += 1
    }

    override fun toString(): String {
        return "balls: " + balls + "; time: ${(totalTime / 1E8).roundToInt() / 10f}, orientation: $rotation"
    }
}

class Ball(
    pos: Vec2,
    radius: Float,
) {
    val BOUNCE_ENERGY_MULT = 0.9f
    val FRICTION = 0.2f;

    var vel by mutableStateOf(Vec2(30f, 70f))
    var pos by mutableStateOf(pos)
    var radius by mutableStateOf(radius)

    fun update(deltaT: Float, engine: GameEngine) {
        //Log.i("ball", "position: " + pos.x + " " + pos.y)
        //Log.i("ball", "delta: " + deltaT)
        pos.x += vel.x * deltaT
        vel.x += engine.rotation.y * deltaT * 10f
        vel.y += engine.rotation.x * deltaT * 10f
        if ((pos.x + radius).dp > engine.screenWidth) {
            pos.x = engine.screenWidth.value - radius
            vel.x *= -BOUNCE_ENERGY_MULT
        }
        if ((pos.x - radius).dp < 0.dp) {
            pos.x = 0.dp.value + radius
            vel.x *= -BOUNCE_ENERGY_MULT
        }
        pos.y += vel.y * deltaT
        if ((pos.y + radius).dp > engine.screenHeight) {
            pos.y = engine.screenHeight.value - radius
            vel.y *= -BOUNCE_ENERGY_MULT
        }
        if ((pos.y - radius).dp < 0.dp) {
            pos.y = 0.dp.value + radius
            vel.y *= -BOUNCE_ENERGY_MULT
        }
        pos = Vec2(pos.x, pos.y)
        vel.x -= deltaT * FRICTION * sign(vel.x)
        vel.y -= deltaT * FRICTION * sign(vel.y)
    }

    override fun toString(): String {
        return "" + xOffset + " " + yOffset
    }
}

val Ball.xOffset: Dp get() = pos.x.dp - radius.dp
val Ball.yOffset: Dp get() = pos.y.dp - radius.dp


@Composable
fun BalanceHole(onNavigateBack: () -> Unit) {
    val gameEngine = remember { GameEngine() }
    gameEngine.startGame()

    val density = LocalDensity.current
    val context = LocalContext.current

    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    val orientation = remember { sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) }

    var gyroscopeData by remember { mutableStateOf( Vec3(0f, 0f, 0f)) }

    DisposableEffect(sensorManager) {
        val sensorEventListener = object : SensorEventListener {
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                // who car?
            }

            override fun onSensorChanged(event: SensorEvent?) {
               when (event?.sensor?.type) {
                   Sensor.TYPE_ROTATION_VECTOR -> {
                       gameEngine.rotation = Vec3(event.values[0], event.values[1], event.values[2])
                   }
               }
            }
        }

        orientation?.let{
            sensorManager.registerListener(sensorEventListener, it, SensorManager.SENSOR_DELAY_FASTEST)
        }

        onDispose {
            sensorManager.unregisterListener(sensorEventListener)
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            withFrameNanos {
                gameEngine.update(it)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .clipToBounds()
            .onSizeChanged {
                with(density) {
                    gameEngine.screenWidth = it.width.toDp()
                    gameEngine.screenHeight = it.height.toDp()
                }
            }) {
        //Text("" + gameEngine)
        gameEngine.balls.forEach { DrawBall(it) }
    }
}

@Composable
fun DrawBall(ball: Ball) {
    val size = ball.radius.dp
    Box(
        Modifier
            .offset(ball.xOffset, ball.yOffset)
            .size(size * 2)
            .clip(CircleShape)
            .background(Color.Red)
    )
}
