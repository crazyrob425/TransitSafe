package com.example

import android.graphics.Bitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import android.graphics.Color

object BarcodeGenerator {
    fun generateBarcode(content: String, format: BarcodeFormat, width: Int, height: Int): ImageBitmap? {
        if (content.isBlank()) return null
        return try {
            val writer = MultiFormatWriter()
            val bitMatrix: BitMatrix = writer.encode(content, format, width, height)
            val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bmp.setPixel(x, y, if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE)
                }
            }
            bmp.asImageBitmap()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
