package com.crosspaste.ui

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import com.crosspaste.LocalKoinApplication
import com.crosspaste.path.PathProvider

@Composable
fun DebugView() {
    val current = LocalKoinApplication.current
    val pathProvider = current.koin.get<PathProvider>()
    Text(
        text = pathProvider.pasteAppPath.toString(),
    )
}
