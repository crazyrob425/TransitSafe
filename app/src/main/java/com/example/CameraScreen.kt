package com.example

import android.Manifest
import android.graphics.Bitmap
import androidx.camera.core.ImageCapture
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import androidx.core.content.ContextCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraScreen(
    step: String,
    onBack: () -> Unit,
    onImageCaptured: (Bitmap) -> Unit
) {
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    LaunchedEffect(Unit) {
        if (!cameraPermissionState.status.isGranted) {
            cameraPermissionState.launchPermissionRequest()
        }
    }

    if (cameraPermissionState.status.isGranted) {
        CameraScreenContent(step = step, onBack = onBack, onImageCaptured = onImageCaptured)
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Camera permission is required.")
        }
    }
}

@Composable
fun CameraScreenContent(
    step: String,
    onBack: () -> Unit,
    onImageCaptured: (Bitmap) -> Unit
) {
    val context = LocalContext.current
    val imageCapture = remember { ImageCapture.Builder().build() }
    var isCapturing by remember { mutableStateOf(false) }

    val stepDetails = when(step) {
        "front" -> StepInfo("Step 1 of 4", "Verify Victim Identity", "Position the front of the victim's driver's license within the frame below.", 0.25f, "Scanning front side...")
        "back" -> StepInfo("Step 2 of 4", "Scan Barcode", "Turn the driver's license over and position the back barcode within the frame.", 0.5f, "Scanning back side...")
        "insurance" -> StepInfo("Step 3 of 4", "Proof of Insurance", "Position the insurance policy or card clearly within the frame.", 0.75f, "Scanning insurance...")
        else -> StepInfo("Step 4 of 4", "Secondary ID", "Position a second form of ID showing the victim's address.", 1.0f, "Scanning secondary ID...")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.offset(x = (-8).dp)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
            }
            Column(modifier = Modifier.weight(1f).padding(start = 16.dp)) {
                Text("TransitSafe", fontSize = 18.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onBackground)
                Text("PREMIUM LIABILITY COVERAGE", fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF00FF00)))
                Text("SECURE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            }
        }

        // Progress Tracker
        Box(modifier = Modifier.fillMaxWidth().height(6.dp).background(MaterialTheme.colorScheme.surfaceVariant)) {
            Box(modifier = Modifier.fillMaxHeight().fillMaxWidth(stepDetails.progress).background(MaterialTheme.colorScheme.primary))
        }

        // Main Content
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(20.dp)
        ) {
            // Instructions
            Column(modifier = Modifier.padding(bottom = 16.dp)) {
                Text(stepDetails.stepText, color = MaterialTheme.colorScheme.primary, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(stepDetails.title, fontSize = 24.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground)
                Spacer(modifier = Modifier.height(4.dp))
                Text(stepDetails.desc, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            // Camera Viewfinder
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .border(4.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(24.dp))
                    .background(Color.Black)
            ) {
                CameraPreview(modifier = Modifier.fillMaxSize(), imageCapture = imageCapture)

                if (isCapturing) {
                    // Scan Status Overlay
                    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)), contentAlignment = Alignment.Center) {
                        Row(
                            modifier = Modifier.background(Color.Black.copy(alpha = 0.8f), CircleShape).border(1.dp, Color.White.copy(alpha=0.2f), CircleShape).padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color(0xFF00FF00), strokeWidth = 2.dp)
                            Text("CAPTURING DOCUMENT...", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color.White, letterSpacing = 1.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Security & Trust Badge
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(modifier = Modifier.background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp)).padding(8.dp)) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                }
                Column {
                    Text("AES-256 Military Grade Encryption", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground, letterSpacing = 0.5.sp)
                    Text("Vetted by F-Secure & Trail of Bits. Victim data is fully protected and shared only for insurance claim processing.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 14.sp)
                }
            }
        }

        // Bottom Action Bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            Button(
                onClick = {
                    if (!isCapturing) {
                        isCapturing = true
                        imageCapture.takePicture(
                            context = context,
                            executor = ContextCompat.getMainExecutor(context),
                            onImageCaptured = { bmp ->
                                isCapturing = false
                                onImageCaptured(bmp)
                            },
                            onError = {
                                isCapturing = false
                            }
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(stepDetails.btnText, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
            Text(
                "Managed by Lloyd's Specialized Micro-Mobility Fund",
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.outline,
                letterSpacing = 1.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
            )
        }
    }
}

data class StepInfo(val stepText: String, val title: String, val desc: String, val progress: Float, val btnText: String)
