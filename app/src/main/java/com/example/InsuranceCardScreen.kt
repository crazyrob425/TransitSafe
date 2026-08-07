package com.example

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.zxing.BarcodeFormat
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap

@Composable
fun InsuranceCardScreen() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE) }
    
    val firstName = prefs.getString("firstName", "") ?: ""
    val lastName = prefs.getString("lastName", "") ?: ""
    val address = prefs.getString("address", "") ?: ""
    val headshotUrl = prefs.getString("headshotUrl", "") ?: ""
    
    val fullName = if (firstName.isNotBlank() || lastName.isNotBlank()) {
        "$firstName $lastName".trim().uppercase()
    } else {
        "PRO MEMBER"
    }

    val signupDate = "08/01/2026"
    val expirationDate = "08/01/2027"
    val memberCode = "PRM-8492-3301-X"

    val barcodeData = "TransitSafe|$fullName|$address|$signupDate|$expirationDate|$memberCode"
    val qrBitmap = remember(barcodeData) {
        BarcodeGenerator.generateBarcode(barcodeData, BarcodeFormat.QR_CODE, 200, 200)
    }
    val pdf417Bitmap = remember(barcodeData) {
        BarcodeGenerator.generateBarcode(barcodeData, BarcodeFormat.PDF_417, 300, 80)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top section: Logo and Company
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(
                            text = "TransitSafe",
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "PREMIUM LIABILITY COVERAGE",
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 10.sp,
                            letterSpacing = 0.5.sp
                        )
                    }
                    Image(
                        painter = androidx.compose.ui.res.painterResource(id = R.drawable.logo),
                        contentDescription = "Logo",
                        modifier = Modifier.size(32.dp),
                        colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(MaterialTheme.colorScheme.onPrimary)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Middle section: Policy Info
                Column {
                    Text(
                        text = "POLICY NUMBER",
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = memberCode,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 2.sp
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "EFFECTIVE DATE",
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = signupDate,
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "EXPIRATION DATE",
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = expirationDate,
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (pdf417Bitmap != null) {
                    Image(
                        bitmap = pdf417Bitmap,
                        contentDescription = "PDF417 Barcode",
                        modifier = Modifier.fillMaxWidth().height(60.dp),
                        contentScale = ContentScale.FillBounds
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bottom section: Member Name & QR
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (headshotUrl.isNotEmpty()) {
                            AsyncImage(
                                model = headshotUrl,
                                contentDescription = "Member Headshot",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                            )
                        }
                        
                        Column(modifier = Modifier.weight(1f, fill = false)) {
                            Text(
                                text = "INSURED DRIVER",
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = fullName,
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 1.sp
                            )
                            if (address.isNotBlank()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = address.uppercase(),
                                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Normal,
                                    maxLines = 2
                                )
                            }
                        }
                    }
                    
                    if (qrBitmap != null) {
                        Image(
                            bitmap = qrBitmap,
                            contentDescription = "QR Code",
                            modifier = Modifier.size(56.dp).clip(RoundedCornerShape(4.dp))
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "Show this digital card to law enforcement or victims to verify your active premium ride-share liability coverage.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                IdCardGenerator.generateAndShare(
                    context = context,
                    fullName = fullName,
                    address = address,
                    qrBitmap = qrBitmap?.asAndroidBitmap(),
                    pdf417Bitmap = pdf417Bitmap?.asAndroidBitmap(),
                    signupDate = signupDate,
                    expirationDate = expirationDate,
                    memberCode = memberCode
                )
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
        ) {
            Icon(Icons.Default.Share, contentDescription = "Share", modifier = Modifier.padding(end = 8.dp))
            Text("Share Virtual ID Card", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}
