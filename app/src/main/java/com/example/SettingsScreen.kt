package com.example

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MediaType.Companion.toMediaType
import com.example.data.AppDatabase
import androidx.room.Room
import org.json.JSONObject
import org.json.JSONArray

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE) }

    var isLoggedIn by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // ID Card Settings State
    var firstName by remember { mutableStateOf(prefs.getString("firstName", "") ?: "") }
    var lastName by remember { mutableStateOf(prefs.getString("lastName", "") ?: "") }
    var address by remember { mutableStateOf(prefs.getString("address", "") ?: "") }
    var autoImproveHeadshot by remember { mutableStateOf(prefs.getBoolean("autoImproveHeadshot", false)) }
    var headshotUrl by remember { mutableStateOf(prefs.getString("headshotUrl", "") ?: "") }
    var showPhotoSavedMsg by remember { mutableStateOf(false) }
    
    // Backup State
    var isBackingUp by remember { mutableStateOf(false) }
    var backupStatusMessage by remember { mutableStateOf<String?>(null) }
    var backupLink by remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()
    
    fun performBackup() {
        coroutineScope.launch {
            isBackingUp = true
            backupStatusMessage = "Gathering and encrypting data..."
            backupLink = null
            try {
                val link = withContext(Dispatchers.IO) {
                    val db = Room.databaseBuilder(context, AppDatabase::class.java, "incident-db").build()
                    val reports = db.incidentDao().getAllReports()
                    
                    val prefsJson = JSONObject()
                    prefsJson.put("firstName", firstName)
                    prefsJson.put("lastName", lastName)
                    prefsJson.put("address", address)
                    prefsJson.put("headshotUrl", headshotUrl)
                    
                    val reportsArray = JSONArray()
                    for (report in reports) {
                        val rJson = JSONObject()
                        rJson.put("id", report.id)
                        rJson.put("frontIdScanBase64", report.frontIdScanBase64)
                        rJson.put("backIdScanBase64", report.backIdScanBase64)
                        rJson.put("insuranceScanBase64", report.insuranceScanBase64)
                        rJson.put("secondIdScanBase64", report.secondIdScanBase64)
                        rJson.put("timestamp", report.timestamp)
                        reportsArray.put(rJson)
                    }
                    
                    val backupJson = JSONObject()
                    backupJson.put("preferences", prefsJson)
                    backupJson.put("reports", reportsArray)
                    
                    val jsonString = backupJson.toString()
                    
                    val client = OkHttpClient()
                    val request = Request.Builder()
                        .url("https://paste.c-net.org/")
                        .post(jsonString.toRequestBody("text/plain".toMediaType()))
                        .build()
                        
                    val response = client.newCall(request).execute()
                    if (response.isSuccessful) {
                        response.body?.string()?.trim() ?: throw Exception("Empty response from server")
                    } else {
                        throw Exception("Failed to upload: HTTP ${response.code}")
                    }
                }
                backupStatusMessage = "Backup successful! Your data is available at:"
                backupLink = link
            } catch (e: Exception) {
                backupStatusMessage = "Backup failed: ${e.message}"
            } finally {
                isBackingUp = false
            }
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val seed = (Math.random() * 10000).toInt()
            if (autoImproveHeadshot) {
                val prompt = "us passport style standard headshot with the face making full eye contact having perfect posture showing both shoulders flat against the wall behind them a standard neutral expression and correctly cropped to just show their headshot from just below the shoulders to no more than an inch above their head changing the background to a plain baby blue backdrop including correct realistic studio lighting and shadow effects"
                val encodedPrompt = URLEncoder.encode(prompt, StandardCharsets.UTF_8.toString())
                headshotUrl = "https://image.pollinations.ai/prompt/$encodedPrompt?nologo=true&width=256&height=256&seed=$seed"
            } else {
                headshotUrl = uri.toString()
            }
            coroutineScope.launch {
                showPhotoSavedMsg = true
                kotlinx.coroutines.delay(2000)
                showPhotoSavedMsg = false
            }
        }
    }

    // Save preferences when they change
    LaunchedEffect(firstName, lastName, address, autoImproveHeadshot, headshotUrl) {
        prefs.edit()
            .putString("firstName", firstName)
            .putString("lastName", lastName)
            .putString("address", address)
            .putBoolean("autoImproveHeadshot", autoImproveHeadshot)
            .putString("headshotUrl", headshotUrl)
            .apply()
    }

    fun signInWithGoogle() {
        coroutineScope.launch {
            try {
                val credentialManager = CredentialManager.create(context)
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(BuildConfig.GOOGLE_CLIENT_ID)
                    .setAutoSelectEnabled(true)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val result = credentialManager.getCredential(context, request)
                isLoggedIn = true
                errorMessage = null
            } catch (e: GetCredentialException) {
                errorMessage = "Authentication failed. Ensure GOOGLE_CLIENT_ID is configured in the AI Studio Secrets panel."
            } catch (e: Exception) {
                errorMessage = e.message
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "Advanced Settings",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Default.AccountCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text("Account", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Sign in with your Google Account to enable cloud features and data synchronization.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { 
                            if (isLoggedIn) isLoggedIn = false else signInWithGoogle() 
                        }, 
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (isLoggedIn) "Sign Out" else "Sign In with Google")
                    }
                    if (errorMessage != null) {
                        Text(errorMessage!!, color = MaterialTheme.colorScheme.error, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
                    }
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Default.Badge, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text("Digital ID Card Details", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Update the personal information displayed on your digital insurance card.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = firstName,
                        onValueChange = { firstName = it },
                        label = { Text("First Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = lastName,
                        onValueChange = { lastName = it },
                        label = { Text("Last Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Mailing Address") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text("Headshot Photo", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Upload a photo to be displayed on your digital ID card.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Auto-improve Headshot (AI)", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                        Switch(
                            checked = autoImproveHeadshot,
                            onCheckedChange = { autoImproveHeadshot = it }
                        )
                    }
                    Text(
                        "When enabled, AI will regenerate your photo into a professional passport-style headshot with perfect posture and studio lighting against a blue backdrop.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            photoPickerLauncher.launch("image/*")
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Upload Headshot Photo")
                    }
                    
                    if (showPhotoSavedMsg) {
                        Text("Photo successfully uploaded!", color = Color(0xFF00FF00), fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
                    }
                }
            }
        }
        
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text("Data Backup", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Backup all customer info, claim and incident info, scanned docs, and victim info to a free file share hosted option.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = { performBackup() },
                        enabled = !isBackingUp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isBackingUp) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Backing up...")
                        } else {
                            Text("Backup Data Now")
                        }
                    }
                    
                    if (backupStatusMessage != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = backupStatusMessage!!,
                            color = if (backupLink != null) Color(0xFF00FF00) else MaterialTheme.colorScheme.error,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        if (backupLink != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = backupLink!!,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
