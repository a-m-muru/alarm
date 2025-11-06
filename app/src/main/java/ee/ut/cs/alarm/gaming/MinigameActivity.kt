package ee.ut.cs.alarm.gaming

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.sin


class Vec2(var x: Float, var y: Float) {
    operator fun plus(v: Vec2): Vec2 {
        x += v.x
        y += v.y
        return this
    }

    operator fun times(mul: Float): Vec2 {
        return Vec2(x*mul, y*mul)
    }
}

@Composable
// chat gept
fun GameLoob(ctx: Context) {
    var frame by remember { mutableStateOf(0) }
    var pos by remember { mutableStateOf(Vec2(0f, 0f)) }
    var dir by remember { mutableStateOf(Vec2(0f, 0f)) }
    //val corScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000 / 60)
            frame += 1
            pos += Vec2(0.25f, sin(pos.x * 0.05f) * 10f)
        }
    }

    Column {
        Text(
            text = "freim: $frame, posx: ${pos.x}",
            modifier = Modifier.offset(pos.x.dp, pos.y.dp)
        )

    }
}