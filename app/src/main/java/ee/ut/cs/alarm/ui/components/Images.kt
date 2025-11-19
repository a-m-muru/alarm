package ee.ut.cs.alarm.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalResources

// thanks GHAT GEPT
// this is not satisfactory TODO make work
@Composable
fun FilteredImage(
    id: Int,
    modifier: Modifier,
) {
    val bitmap: Bitmap =
        BitmapFactory.decodeResource(
            LocalResources.current,
            id,
        )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val paint = Paint()
        paint.isAntiAlias = false

        drawContext.canvas.drawImage(bitmap.asImageBitmap(), Offset.Zero, paint)
    }
}
