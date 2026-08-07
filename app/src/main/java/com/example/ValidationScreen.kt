package com.example

import android.graphics.Bitmap
import android.util.Base64
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

@Composable
fun ValidationScreen(
    bitmap: Bitmap?,
    step: String,
    onSuccess: () -> Unit,
    onRetry: () -> Unit
) {
    var status by remember { mutableStateOf<ValidationStatus>(ValidationStatus.Loading) }

    LaunchedEffect(bitmap) {
        if (bitmap == null) {
            status = ValidationStatus.Error("Failed to capture image.")
            return@LaunchedEffect
        }

        try {
            val result = validateDocument(bitmap, step)
            if (result.startsWith("OK")) {
                status = ValidationStatus.Success
                onSuccess()
            } else {
                status = ValidationStatus.Error(result.removePrefix("REJECT:").trim())
            }
        } catch (e: Exception) {
            status = ValidationStatus.Error("Network error: ${e.message}")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (val s = status) {
            is ValidationStatus.Loading -> {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(64.dp))
                Spacer(modifier = Modifier.height(24.dp))
                Text("AI VALIDATING DOCUMENT...", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Ensuring image is clear and document is valid.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            is ValidationStatus.Success -> {
                Icon(Icons.Default.CheckCircle, contentDescription = "Success", tint = Color(0xFF00FF00), modifier = Modifier.size(64.dp))
                Spacer(modifier = Modifier.height(24.dp))
                Text("Scan Accepted", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            }
            is ValidationStatus.Error -> {
                Icon(Icons.Default.Error, contentDescription = "Error", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(64.dp))
                Spacer(modifier = Modifier.height(24.dp))
                Text("Document Rejected", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                Spacer(modifier = Modifier.height(8.dp))
                Text(s.message, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(32.dp))
                Button(onClick = onRetry) {
                    Text("Retake Photo")
                }
            }
        }
    }
}

sealed class ValidationStatus {
    object Loading : ValidationStatus()
    object Success : ValidationStatus()
    data class Error(val message: String) : ValidationStatus()
}

suspend fun validateDocument(bitmap: Bitmap, step: String): String = withContext(Dispatchers.IO) {
    val apiKey = BuildConfig.GEMINI_API_KEY
    if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
        return@withContext "REJECT: Gemini API Key is missing. Please configure GEMINI_API_KEY in the AI Studio Secrets panel."
    }

    val outputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
    val base64Image = Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)

    val prompt = """
        Analyze this document image. The expected document is: $step.
        Identify any issues like a blurred photo, cropped off important areas, obviously fake design, hard to read text, or expired document.
        If it is acceptable for a formal insurance claim, return exactly 'OK'.
        If it has issues, return exactly 'REJECT: [reason]'.
    """.trimIndent()

    val request = GenerateContentRequest(
        contents = listOf(
            Content(
                parts = listOf(
                    Part(text = prompt),
                    Part(inlineData = InlineData(mimeType = "image/jpeg", data = base64Image))
                )
            )
        )
    )

    try {
        val response = RetrofitClient.service.generateContent(apiKey, request)
        val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "REJECT: No response from AI."
        text.trim()
    } catch (e: Exception) {
        "REJECT: API Error - ${e.message}"
    }
}
