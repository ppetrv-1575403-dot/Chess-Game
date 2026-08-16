package com.p_soft.chess

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.p_soft.chess.presentation.game.ChessBoardScreen
import com.p_soft.chess.presentation.theme.ChessMasterTheme
import dagger.hilt.android.AndroidEntryPoint

// MainActivity.kt
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ChessMasterTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ChessBoardScreen()
                }
            }
        }
    }
}

/*
@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ChessTheme {
        Greeting("Android")
    }
}*/
