package com.example.tictactoe.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tictactoe.model.BoardModel
import com.example.tictactoe.ui.components.BoardGrid
import com.example.tictactoe.viewmodel.GameViewModel

/**
 * Pantalla principal del Tres en Raya.
 *
 * Es el punto de conexión entre la View y el ViewModel: observa
 * [GameViewModel.boardState] y pasa el estado y las acciones a [GameContent].
 *
 * @param modifier Modificador externo (por ejemplo, el padding del `Scaffold`).
 * @param viewModel ViewModel de la partida. Por defecto se obtiene con `viewModel()`,
 * que devuelve siempre la misma instancia aunque la pantalla se recree.
 */
@Composable
fun GameScreen(
    modifier: Modifier = Modifier,
    viewModel: GameViewModel = viewModel()
) {
    // Cada BoardModel nuevo emitido por el StateFlow provoca una recomposición
    val board by viewModel.boardState.collectAsState()

    GameContent(
        board = board,
        onCellClick = viewModel::onCellClick,
        onRestartClick = viewModel::restartGame,
        modifier = modifier
    )
}

/**
 * Contenido de la pantalla, sin estado propio.
 *
 * Solo pinta el [board] que recibe y avisa de las acciones del usuario.
 * Al no depender del ViewModel, se puede previsualizar con `@Preview`.
 *
 * @param board Estado de la partida que se va a mostrar.
 * @param onCellClick Acción al pulsar una casilla; recibe su posición (0–8).
 * @param onRestartClick Acción al pulsar el botón "Reiniciar".
 * @param modifier Modificador externo.
 */
@Composable
fun GameContent(
    board: BoardModel,
    onCellClick: (Int) -> Unit,
    onRestartClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically),
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(text = "Tres en Raya", style = MaterialTheme.typography.headlineLarge)

        Text(text = statusText(board), style = MaterialTheme.typography.titleLarge)

        BoardGrid(board = board, onCellClick = onCellClick)

        Button(onClick = onRestartClick) {
            Text("Reiniciar")
        }
    }
}

/**
 * Traduce el estado de la partida a un texto para el usuario.
 *
 * Solo elige qué texto mostrar a partir de datos que ya calculó el modelo;
 * no aplica ninguna regla del juego.
 *
 * @param board Estado actual de la partida.
 * @return "¡Ganó X!", "¡Ganó O!", "¡Empate!" o "Turno: X/O" si la partida sigue.
 */
private fun statusText(board: BoardModel): String = when {
    board.winner != null -> "¡Ganó ${board.winner}!"
    board.isDraw -> "¡Empate!"
    else -> "Turno: ${board.currentTurn}"
}

/** Vista previa de una partida en curso, visible en el panel "Split" o "Design". */
@Preview(showBackground = true, widthDp = 360, heightDp = 720)
@Composable
private fun GameContentPreview() {
    val board = BoardModel.createInitialBoard()
        .makeMove(4)
        .makeMove(0)
        .makeMove(8)
    MaterialTheme {
        GameContent(board = board, onCellClick = {}, onRestartClick = {})
    }
}
