package ee.ut.cs.alarm.gaming


import android.graphics.Color
import androidx.compose.ui.graphics.Color as ColorUI
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.Size
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import java.nio.ByteBuffer
import java.util.concurrent.Executors

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect

private val cameraExecutor = Executors.newSingleThreadExecutor()
private val handler = Handler(Looper.getMainLooper())

class CenterPixelAnalyzer(
    private val onColorDetected: (Int) -> Unit
) : ImageAnalysis.Analyzer {

    // Assuming a common low-res target for analysis for performance
    // The actual imageProxy width/height will reflect the resolution set in ImageAnalysis.Builder
    private val bytesPerPixel = 4 // For RGBA_8888

    override fun analyze(imageProxy: ImageProxy) {
        // We use the dimensions of the received imageProxy
        val width = imageProxy.width
        val height = imageProxy.height

        // Calculate the center pixel coordinates
        val centerX = width / 2
        val centerY = height / 2

        val plane = imageProxy.planes[0]
        val buffer: ByteBuffer = plane.buffer
        val rowStride = plane.rowStride


        // Calculate the index for the center pixel
        val centerRowStart = centerY * rowStride
        val centerPixelOffset = centerX * bytesPerPixel
        val pixelIndex = centerRowStart + centerPixelOffset

        var detectedColor: Int? = null

        if (pixelIndex >= 0 && pixelIndex <= buffer.limit() - bytesPerPixel) {
            // Read the R, G, B, A bytes at the calculated index
            // Use .get(index).toInt() and 0xFF to safely convert signed byte to unsigned int (0-255)
            val red = buffer.get(pixelIndex).toInt() and 0xFF
            val green = buffer.get(pixelIndex + 1).toInt() and 0xFF
            val blue = buffer.get(pixelIndex + 2).toInt() and 0xFF
            val alpha = buffer.get(pixelIndex + 3).toInt() and 0xFF

            val colorInt = Color.argb(alpha, red, green, blue)



            // Convert to the Compose Color object
            detectedColor = colorInt //Color.valueOf(colorInt)
        }

        imageProxy.close() // Release the frame

        // Update the state on the Main Thread using the Handler
        if (detectedColor != null) {
            handler.post {
                onColorDetected(detectedColor)
            }
        }


    }
}

@Composable
fun CameraGame(onNavigateBack: () -> Unit) {

    val context = LocalContext.current

    // Check if we already have permission
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    // Create the launcher to ask for permission
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
        }
    )

    // Launch the request when the screen opens (if not already granted)
    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    if (hasCameraPermission) {

        val lifecycleOwner = LocalLifecycleOwner.current


        // game state
        var currentCount by remember { mutableStateOf(0) }
        val neededCount = 4


        // pixel state
        var currentColor by remember { mutableStateOf(ColorUI.White) }

        val onColorDetected: (Int) -> Unit = remember {
            { newColor ->
                // todo check if color is correct

                currentCount = 4

                currentColor = ColorUI(newColor)

            }
        }

        Column(modifier = Modifier.fillMaxSize()) {
            // Camera Preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                AndroidView(
                    factory = { ctx ->
                        // Create the PreviewView
                        val previewView = PreviewView(ctx)

                        // Start CameraX when the view is created
                        val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                        cameraProviderFuture.addListener({
                            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

                            // Configure Preview Use Case
                            val preview = Preview.Builder()
                                .build()
                                .also {
                                    it.setSurfaceProvider(previewView.surfaceProvider)
                                }

                            // Configure ImageAnalysis Use Case
                            val imageAnalyzer = ImageAnalysis.Builder()
                                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                                .setTargetResolution(Size(640, 480))
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .build()
                                .also {
                                    // Bind our custom analyzer with the state update callback
                                    it.setAnalyzer(
                                        cameraExecutor,
                                        CenterPixelAnalyzer(onColorDetected)
                                    )
                                }

                            // Bind all use cases to the lifecycle owner
                            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                            try {
                                cameraProvider.unbindAll()
                                cameraProvider.bindToLifecycle(
                                    lifecycleOwner, cameraSelector, preview, imageAnalyzer
                                )
                            } catch (exc: Exception) {
                                Log.e("CameraGame", "Use case binding failed", exc)
                            }

                        }, ContextCompat.getMainExecutor(ctx))

                        previewView // Return the created view
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // OPTIONAL: Draw a center target (crosshair) over the camera view
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 5.dp.toPx()
                    val targetSize = 20.dp.toPx()
                    val center = Offset(size.width / 2f, size.height / 2f)

                    // Draw a crosshair or circle for the center
                    drawCircle(color = ColorUI.White, radius = 4.dp.toPx(), center = center)
                    drawLine(
                        ColorUI.White,
                        start = center - Offset(targetSize, 0f),
                        end = center - Offset(targetSize / 2, 0f),
                        strokeWidth = strokeWidth
                    )
                    drawLine(
                        ColorUI.White,
                        start = center + Offset(targetSize / 2, 0f),
                        end = center + Offset(targetSize, 0f),
                        strokeWidth = strokeWidth
                    )
                    drawLine(
                        ColorUI.White,
                        start = center - Offset(0f, targetSize),
                        end = center - Offset(0f, targetSize / 2),
                        strokeWidth = strokeWidth
                    )
                    drawLine(
                        ColorUI.White,
                        start = center + Offset(0f, targetSize / 2),
                        end = center + Offset(0f, targetSize),
                        strokeWidth = strokeWidth
                    )
                }
            }



            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(text = "Color Camera", fontSize = 24.sp)
                Text(
                    text = "Point your phone towards color",
                    fontSize = 24.sp,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "Current color (R, G, B): ${currentColor.red}, ${currentColor.green}, ${currentColor.blue}\"",
                    fontSize = 24.sp
                )

                Text(text = "Count: $currentCount/$neededCount", fontSize = 32.sp)
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
    } else {
        // Fallback UI if permission is denied
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Camera access is required for this game.")
            Spacer(modifier = Modifier.height(16.dp))

            // First time asking OR permanently denied.
            Button(onClick = {
                // This launches the system dialog
                launcher.launch(Manifest.permission.CAMERA)

                // Logic: If this button is clicked and launcher does NOT show a dialog
                // (because of permanent denial), the user stays on this screen.
                // We can add a "Open Settings" button as a fallback.
            }) {
                Text("Grant Permission")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(onClick = {
                // Open System Settings (The only way to fix "Don't ask again")
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                }
                context.startActivity(intent)
            }) {
                Text("Open App Settings")
            }
        }
    }
}
