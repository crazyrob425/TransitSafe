package com.example

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

object IdCardGenerator {
    fun generateAndShare(
        context: Context,
        fullName: String,
        address: String,
        qrBitmap: Bitmap?,
        pdf417Bitmap: Bitmap?,
        signupDate: String,
        expirationDate: String,
        memberCode: String
    ) {
        val width = 1011
        val height = 638 // Standard CR80 ID card ratio
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        // Background
        val bgPaint = Paint().apply { color = Color.parseColor("#1B3A5A") } // Primary color
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)
        
        val textPaint = Paint().apply {
            color = Color.WHITE
            textSize = 40f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        
        // Shield Logo from drawable
        val logoDrawable = ContextCompat.getDrawable(context, R.drawable.logo)
        logoDrawable?.let {
            it.setTint(Color.WHITE)
            it.setBounds(40, 40, 140, 140)
            it.draw(canvas)
        }
        
        // App Name and Shield Logo (simulated by text for now)
        canvas.drawText("TransitSafe Liability Shield", 160f, 100f, textPaint)
        
        textPaint.textSize = 24f
        textPaint.color = Color.parseColor("#CCCCCC")
        canvas.drawText("ACTIVE PAID PRO MEMBER", 160f, 135f, textPaint)
        
        // Info
        textPaint.color = Color.WHITE
        textPaint.textSize = 32f
        canvas.drawText("Name: $fullName", 40f, 240f, textPaint)
        canvas.drawText("Address: $address", 40f, 290f, textPaint)
        
        textPaint.textSize = 24f
        textPaint.color = Color.parseColor("#AAAAAA")
        canvas.drawText("Sign Up: $signupDate  |  Exp: $expirationDate", 40f, 350f, textPaint)
        canvas.drawText("Pro Member Code: $memberCode", 40f, 390f, textPaint)
        
        textPaint.textSize = 20f
        textPaint.color = Color.parseColor("#ECA315") // Gold
        canvas.drawText("Fully backed and financially protected by", 40f, 450f, textPaint)
        canvas.drawText("the personal liability insurance policy of our lenders.", 40f, 480f, textPaint)
        
        // Barcodes
        qrBitmap?.let {
            canvas.drawBitmap(it, null, RectF(width - 220f, 40f, width - 40f, 220f), null)
        }
        pdf417Bitmap?.let {
            canvas.drawBitmap(it, null, RectF(width - 440f, height - 160f, width - 40f, height - 40f), null)
        }
        
        // Save and share
        try {
            val file = File(context.cacheDir, "virtual_id_card.png")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )
            
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "My TransitSafe Liability Shield ID Card")
                putExtra(Intent.EXTRA_TEXT, "Here is my active premium liability coverage ID card.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Share Virtual ID Card"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
