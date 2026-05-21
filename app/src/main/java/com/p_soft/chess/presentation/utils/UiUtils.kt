package com.p_soft.chess.presentation.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration

// Определяет ориентацию экрана
@Composable
fun isLandscape(): Boolean {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.screenWidthDp > configuration.screenHeightDp
    return isLandscape
}
