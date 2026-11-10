package com.example.tictactoe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.tictactoe.ui.GameScreen

/**
 * Punto de entrada de la aplicación.
 *
 * Solo prepara el tema y el `Scaffold` y muestra [GameScreen].
 * El ViewModel lo obtiene la propia [GameScreen] con `viewModel()`.
 */
class MainActivity : ComponentActivity() {

    /** Crea la Activity y define su contenido con Compose. */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    GameScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}
