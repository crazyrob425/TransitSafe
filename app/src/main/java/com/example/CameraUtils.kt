package com.example

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import java.util.concurrent.Executor
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    imageCapture: ImageCapture,
    onCameraReady: () -> Unit = {}
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val previewView = remember { PreviewView(context) }

    LaunchedEffect(Unit) {
        val cameraProvider = context.getCameraProvider()
        val preview = Preview.Builder().build()
        preview.surfaceProvider = previewView.surfaceProvider

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
                imageCapture
            )
            onCameraReady()
        } catch (e: Exception) {
            Log.e("CameraPreview", "Use case binding failed", e)
        }
    }

    Box(modifier = modifier) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )
        // Camera Overlay (Guided UI)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val overlayWidth = width * 0.85f
            val overlayHeight = overlayWidth / 1.58f
            val left = (width - overlayWidth) / 2
            val top = (height - overlayHeight) / 2

            // Dimmed background with clear cutout
            // Simplified using stroke for the border
            drawRoundRect(
                color = Color.White.copy(alpha = 0.4f),
                topLeft = Offset(left, top),
                size = Size(overlayWidth, overlayHeight),
                cornerRadius = CornerRadius(16.dp.toPx()),
                style = Stroke(
                    width = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 20f), 0f)
                )
            )

            // Neon green corner guides
            val cornerLength = 24.dp.toPx()
            val strokeWidth = 4.dp.toPx()
            val greenColor = Color(0xFF00FF00)

            // Top Left
            drawLine(greenColor, Offset(left - strokeWidth/2, top), Offset(left + cornerLength, top), strokeWidth)
            drawLine(greenColor, Offset(left, top - strokeWidth/2), Offset(left, top + cornerLength), strokeWidth)
            
            // Top Right
            drawLine(greenColor, Offset(left + overlayWidth + strokeWidth/2, top), Offset(left + overlayWidth - cornerLength, top), strokeWidth)
            drawLine(greenColor, Offset(left + overlayWidth, top - strokeWidth/2), Offset(left + overlayWidth, top + cornerLength), strokeWidth)

            // Bottom Left
            drawLine(greenColor, Offset(left - strokeWidth/2, top + overlayHeight), Offset(left + cornerLength, top + overlayHeight), strokeWidth)
            drawLine(greenColor, Offset(left, top + overlayHeight + strokeWidth/2), Offset(left, top + overlayHeight - cornerLength), strokeWidth)

            // Bottom Right
            drawLine(greenColor, Offset(left + overlayWidth + strokeWidth/2, top + overlayHeight), Offset(left + overlayWidth - cornerLength, top + overlayHeight), strokeWidth)
            drawLine(greenColor, Offset(left + overlayWidth, top + overlayHeight + strokeWidth/2), Offset(left + overlayWidth, top + overlayHeight - cornerLength), strokeWidth)
        }
    }
}

suspend fun Context.getCameraProvider(): ProcessCameraProvider = suspendCoroutine { continuation ->
    ProcessCameraProvider.getInstance(this).also { cameraProvider ->
        cameraProvider.addListener({
            continuation.resume(cameraProvider.get())
        }, ContextCompat.getMainExecutor(this))
    }
}

fun ImageCapture.takePicture(
    context: Context,
    executor: Executor,
    onImageCaptured: (Bitmap) -> Unit,
    onError: (ImageCaptureException) -> Unit
) {
    this.takePicture(
        executor,
        @OptIn(ExperimentalGetImage::class)
        object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(image: ImageProxy) {
                val bitmap = image.toBitmap()
                // Handle rotation
                val rotationDegrees = image.imageInfo.rotationDegrees
                val matrix = Matrix().apply { postRotate(rotationDegrees.toFloat()) }
                val rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                image.close()
                onImageCaptured(rotatedBitmap)
            }

            override fun onError(exception: ImageCaptureException) {
                onError(exception)
            }
        }
    )
}
