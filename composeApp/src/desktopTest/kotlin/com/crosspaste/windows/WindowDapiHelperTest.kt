package com.crosspaste.windows

import com.crosspaste.os.windows.WindowDapiHelper
import com.crosspaste.platform.currentPlatform
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WindowDapiHelperTest {

    @Test
    fun testDapi() {
        val currentPlatform = currentPlatform()
        if (currentPlatform.isWindows()) {
            val str = "test windows dapi"
            val encryptData = WindowDapiHelper.encryptData(str.encodeToByteArray())
            assertTrue { (encryptData?.size ?: 0) > 0 }
            val decryptData = WindowDapiHelper.decryptData(encryptData!!)
            assertTrue { (decryptData?.size ?: 0) > 0 }
            assertEquals(str, String(decryptData!!))
        }
    }
}
