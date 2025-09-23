package com.azzahid.hof.services

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set
import io.nayuki.qrcodegen.QrCode

interface QRCodeService {
    fun generateQrCode(text: String): ImageBitmap?
}

class AndroidQRCodeService : QRCodeService {
    override fun generateQrCode(text: String): ImageBitmap? {
        return try {
            val qr = QrCode.encodeText(text, QrCode.Ecc.MEDIUM)
            val size = qr.size
            val scale = 8
            val border = 4
            val totalSize = (size + border * 2) * scale

            val bitmap = createBitmap(totalSize, totalSize)

            for (y in 0 until totalSize) {
                for (x in 0 until totalSize) {
                    val moduleX = (x / scale) - border
                    val moduleY = (y / scale) - border

                    val isBlack = if (moduleX in 0 until size && moduleY in 0 until size) {
                        qr.getModule(moduleX, moduleY)
                    } else {
                        false
                    }

                    bitmap[x, y] =
                        if (isBlack) android.graphics.Color.BLACK else android.graphics.Color.WHITE
                }
            }

            bitmap.asImageBitmap()
        } catch (e: Exception) {
            null
        }
    }
}