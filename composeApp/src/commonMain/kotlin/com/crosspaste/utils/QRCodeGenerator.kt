package com.crosspaste.utils

import androidx.compose.ui.graphics.ImageBitmap

interface QRCodeGenerator {

    fun generateQRCode(
        width: Int,
        height: Int,
        token: CharArray,
    ): ImageBitmap
}
