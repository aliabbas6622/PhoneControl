package com.azzahid.hof.services

import android.graphics.Bitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set
import io.nayuki.qrcodegen.QrCode

interface QRCodeService {
    fun generateQrCode(text: String): ImageBitmap?
    fun generateQrBitmap(text: String, scale: Int = 8): Bitmap?
}

class AndroidQRCodeService : QRCodeService {
    override fun generateQrCode(text: String): ImageBitmap? =
        generateQrBitmap(text, DEFAULT_SCALE)?.asImageBitmap()

    override fun generateQrBitmap(text: String, scale: Int): Bitmap? {
        return try {
            val qr = QrCode.encodeText(text, QrCode.Ecc.MEDIUM)
            val size = qr.size
            val safeScale = scale.coerceIn(MIN_SCALE, MAX_SCALE)
            val border = 4
            val totalSize = (size + border * 2) * safeScale

            val bitmap = createBitmap(totalSize, totalSize)

            for (y in 0 until totalSize) {
                for (x in 0 until totalSize) {
                    val moduleX = (x / safeScale) - border
                    val moduleY = (y / safeScale) - border

                    val isBlack = if (moduleX in 0 until size && moduleY in 0 until size) {
                        qr.getModule(moduleX, moduleY)
                    } else {
                        false
                    }

                    bitmap[x, y] =
                        if (isBlack) android.graphics.Color.BLACK else android.graphics.Color.WHITE
                }
            }

            bitmap
        } catch (e: Exception) {
            null
        }
    }

    private companion object {
        const val DEFAULT_SCALE = 8
        const val MIN_SCALE = 1
        const val MAX_SCALE = 40
    }
}