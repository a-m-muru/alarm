package ee.ut.cs.alarm.gaming

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.rotationMatrix
import kotlin.math.round
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
    var holes = mutableStateListOf<Hole>()


    fun startGame() {
        balls.clear()
        holes.clear()

        repeat(8) {
            var ball =
                Ball(
                    pos = Vec2(Random.nextFloat() * 30 + 50, round(Random.nextFloat() * 5) * 20 + 50),
                    radius = Random.nextFloat() * 20 + 10f,
                )
            ball.vel = Vec2(Random.nextFloat() * 20f + -10f, Random.nextFloat() * 20f + -10f)

            balls.add(ball)
        }

        var biggestBallRadious = 0f
        for (ball in balls) {
            if (ball.radius > biggestBallRadious) {
                biggestBallRadious = ball.radius
            }
        }


        val hole =
            Hole(
                pos = Vec2(0f, 0f),
                radius = biggestBallRadious * 1.2f
            )
        hole.pos = Vec2(
            Random.nextFloat() *
                    (screenWidth.value - 2 * hole.radius) + hole.radius,
            Random.nextFloat() *
                    (screenHeight.value - 2 * hole.radius) + hole.radius
        )

        holes.add(hole)


        repeat(3){
            val hole =
                Hole(
                    pos = Vec2(0f, 0f),
                    radius = Random.nextFloat() * biggestBallRadious + 15f,
                )
            hole.pos = Vec2(
                Random.nextFloat() *
                        (screenWidth.value-2*hole.radius) + hole.radius,
                Random.nextFloat() *
                        (screenHeight.value-2*hole.radius) + hole.radius
            )


            holes.add(hole)
        }

    }

    fun update(time: Long) {
        val delta = time - prevTime
        val deltaT = (delta / 1E9).toFloat()
        totalTime += delta
        prevTime = time

        if (frames > 0L) {
            for (ball in balls) {
                ball.update(deltaT, this)

                // check ball-hole collision
                // Iterate over a copy of the list to avoid ConcurrentModificationException
                for (hole in holes.toList()) {
                    val distance = ball.pos.distanceTo(hole.pos)

                    // Check if the ball is inside the hole
                    if (distance < (hole.radius - ball.radius)) {
                        if (ball.vel.length() < 200f) {
                            balls.remove(ball)
                            return
                        }
                    }
                }
            }
        }



        frames += 1
    }

    override fun toString(): String = "balls: " + balls + "; time: ${(totalTime / 1E8).roundToInt() / 10f}, orientation: $rotation"
}

class Hole(
    pos: Vec2,
    radius: Float,
) {
    var pos by mutableStateOf(pos)
    var radius by mutableStateOf(radius)
}

class Ball(
    pos: Vec2,
    radius: Float,
) {
    val BOUNCE_ENERGY_MULT = 0.9f
    val FRICTION = 0.2f

    val ACC_MULT = 100f

    val MAX_VEL = 10000f

    var vel by mutableStateOf(Vec2(30f, 70f))
    var pos by mutableStateOf(pos)
    var radius by mutableStateOf(radius)

    fun update(
        deltaT: Float,
        engine: GameEngine,
    ) {
        // Log.i("ball", "position: " + pos.x + " " + pos.y)
        // Log.i("ball", "delta: " + deltaT)

        //todo if z is negative, go crazy
        if (engine.rotation.z < 0) {
            vel.x = Random.nextFloat() * 10000f - 5000f
            vel.y = Random.nextFloat() * 10000f - 5000f
        }



        vel.x += engine.rotation.x * deltaT * ACC_MULT
        pos.x += vel.x * deltaT

        // Wall collision x
        // Right wall
        if ((pos.x + radius).dp > engine.screenWidth) {
            pos.x = engine.screenWidth.value - radius
            vel.x *= -BOUNCE_ENERGY_MULT
        }
        // Left wall
        if ((pos.x - radius).dp < 0.dp) {
            pos.x = 0.dp.value + radius
            vel.x *= -BOUNCE_ENERGY_MULT
        }

        // Wall collision y
        // Bottom wall
        vel.y += engine.rotation.y * deltaT * ACC_MULT
        pos.y += vel.y * deltaT
        if ((pos.y + radius).dp > engine.screenHeight) {
            pos.y = engine.screenHeight.value - radius
            vel.y *= -BOUNCE_ENERGY_MULT
        }
        // Top wall
        if ((pos.y - radius).dp < 0.dp) {
            pos.y = 0.dp.value + radius
            vel.y *= -BOUNCE_ENERGY_MULT
        }

        pos = Vec2(pos.x, pos.y)
        vel.x -= deltaT * FRICTION * sign(vel.x)
        vel.y -= deltaT * FRICTION * sign(vel.y)
    }

    override fun toString(): String = "" + xOffset + " " + yOffset
}

val Ball.xOffset: Dp get() = pos.x.dp - radius.dp
val Ball.yOffset: Dp get() = pos.y.dp - radius.dp

@Composable
fun BalanceHole(onNavigateBack: () -> Unit) {
    val gameEngine = remember { GameEngine() }
    //gameEngine.startGame()

    val density = LocalDensity.current
    val context = LocalContext.current

    val sensorManager =
        remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    val orientation = remember { sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) }
    val grav = remember { sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY) }

    var gyroscopeData by remember { mutableStateOf(Vec3(0f, 0f, 0f)) }

    DisposableEffect(sensorManager) {
        val sensorEventListener =
            object : SensorEventListener {
                override fun onAccuracyChanged(
                    sensor: Sensor?,
                    accuracy: Int,
                ) {
                    // who car?
                }

                override fun onSensorChanged(event: SensorEvent?) {
                    when (event?.sensor?.type) {
                        Sensor.TYPE_GRAVITY -> {
                            gameEngine.rotation =
                                Vec3(-event.values[0], event.values[1], event.values[2])
                        }
                    }
                }
            }

        grav?.let {
            sensorManager.registerListener(
                sensorEventListener,
                it,
                SensorManager.SENSOR_DELAY_FASTEST,
            )
        }

        onDispose {
            sensorManager.unregisterListener(sensorEventListener)
        }
    }
    LaunchedEffect(Unit) {
        gameEngine.startGame()
    }
    LaunchedEffect(Unit) {
        while (true) {
            withFrameNanos {
                gameEngine.update(it)
            }
        }
    }

    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .clipToBounds()
                .onSizeChanged {
                    with(density) {
                        gameEngine.screenWidth = it.width.toDp()
                        gameEngine.screenHeight = it.height.toDp()
                    }
                },
    ) {
        // Text("" + gameEngine)
        gameEngine.holes.forEach { DrawHole(it) }
        gameEngine.balls.forEach { DrawBall(it) }


        if (gameEngine.balls.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally, // Center items horizontally
                    verticalArrangement = Arrangement.Center // Center items vertically within the column
                ) {
                    Text(text = "Congratulations! You did it!", fontSize = 24.sp)
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(onClick = onNavigateBack) {
                        Text("Go Back")
                    }
                }
            }
        }
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
            .background(Color.Red),
    )
}

@Composable
fun DrawHole(hole: Hole){
    val size = hole.radius.dp
    Box(
        Modifier
            .offset(hole.pos.x.dp - size, hole.pos.y.dp - size)
            .size(size * 2)
            .clip(CircleShape)
            .background(Color.Black),
    )
}
