package com.example

import android.graphics.Bitmap
import android.util.Base64
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import com.example.data.AppDatabase
import com.example.data.IncidentReport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import androidx.camera.core.ImageCapture
import androidx.core.content.ContextCompat

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

@Composable
fun AppNavigation() {
    val context = LocalContext.current
    val db = remember { Room.databaseBuilder(context, AppDatabase::class.java, "incident-db").build() }
    val scope = rememberCoroutineScope()
    
    val navController = rememberNavController()
    // A simple state holder for scanned bitmaps
    var currentBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var currentStep by remember { mutableStateOf("front") }

    val capturedImages = remember { mutableStateMapOf<String, String>() }

    fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }

    NavHost(
        navController = navController, 
        startDestination = "main",
        enterTransition = {
            fadeIn(animationSpec = tween(400)) + slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(400))
        },
        exitTransition = {
            fadeOut(animationSpec = tween(400)) + slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(400))
        },
        popEnterTransition = {
            fadeIn(animationSpec = tween(400)) + slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(400))
        },
        popExitTransition = {
            fadeOut(animationSpec = tween(400)) + slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(400))
        }
    ) {
        composable("main") {
            MainScreen(
                onStartIncident = {
                    currentStep = "front"
                    navController.navigate("camera")
                }
            )
        }
        composable("camera") {
            CameraScreen(
                step = currentStep,
                onBack = { navController.popBackStack() },
                onImageCaptured = { bmp ->
                    currentBitmap = bmp
                    navController.navigate("validation")
                }
            )
        }
        composable("validation") {
            ValidationScreen(
                bitmap = currentBitmap,
                step = currentStep,
                onSuccess = {
                    currentBitmap?.let { bmp ->
                        capturedImages[currentStep] = bitmapToBase64(bmp)
                    }
                    when (currentStep) {
                        "front" -> currentStep = "back"
                        "back" -> currentStep = "insurance"
                        "insurance" -> currentStep = "second_id"
                        "second_id" -> {
                            // Save to Room and simulate sync
                            scope.launch(Dispatchers.IO) {
                                val report = IncidentReport(
                                    frontIdScanBase64 = capturedImages["front"],
                                    backIdScanBase64 = capturedImages["back"],
                                    insuranceScanBase64 = capturedImages["insurance"],
                                    secondIdScanBase64 = capturedImages["second_id"]
                                )
                                db.incidentDao().insertReport(report)
                            }
                            navController.navigate("summary") {
                                popUpTo("start") { inclusive = false }
                            }
                            return@ValidationScreen
                        }
                    }
                    navController.navigate("camera") {
                        popUpTo("camera") { inclusive = true }
                    }
                },
                onRetry = {
                    navController.popBackStack()
                }
            )
        }
        composable("summary") {
            SummaryScreen(
                onDone = {
                    navController.popBackStack("start", inclusive = false)
                }
            )
        }
    }
}

@Composable
fun DashboardScreen(onStartIncident: () -> Unit, onShowInsuranceCard: () -> Unit = {}) {
    val haptic = LocalHapticFeedback.current
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(800)) + slideInVertically(
            initialOffsetY = { 100 },
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
        Image(
            painter = androidx.compose.ui.res.painterResource(id = R.drawable.logo),
            contentDescription = "TransitSafe Liability Shield Logo",
            modifier = Modifier.size(240.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Premium Liability Coverage",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 2.sp
        )
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "To process the insurance claim efficiently, we need to collect essential information from the victim. This process is fully encrypted and secure.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(48.dp))
        Button(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onStartIncident()
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("Begin Identity Verification", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onShowInsuranceCard()
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(28.dp)
        ) {
            Text("View Digital ID Card", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
    }
    }
}
