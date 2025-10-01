package ee.ut.cs.alarm.ui.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.unit.Dp
import androidx.window.layout.WindowMetricsCalculator
import ee.ut.cs.alarm.ui.screens.AlarmNotificationScreen
import ee.ut.cs.alarm.ui.theme.AlarmTheme
import kotlin.math.min

class WakeUpActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AlarmTheme {
                AlarmNotificationScreen(calculateRelativeWidth(0.75f))
            }
        }
    }

    private fun calculateRelativeWidth(rel: Float): Dp {
        var deviceWidth = 0f
        var deviceHeight = 0f

        WindowMetricsCalculator.Companion
            .getOrCreate()
            .computeCurrentWindowMetrics(this)
            .bounds.let {
                deviceWidth = it.width().toFloat()
                deviceHeight = it.height().toFloat()
            }
        val density = this.resources.displayMetrics.density

        return Dp(value = (rel * min(deviceWidth, deviceHeight)) / density)
    }
}