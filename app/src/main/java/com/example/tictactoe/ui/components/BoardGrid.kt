package com.example.tictactoe.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.tictactoe.model.BoardModel

/**
 * Dibuja el tablero de 3×3 casillas.
 *
 * Reparte [BoardModel.cells] en 3 filas de 3 y delega cada casilla en [CellItemView].
 * Las casillas se desactivan cuando la partida ha terminado.
 *
 * Se usa `Column` + `Row` en lugar de `LazyVerticalGrid` porque el tablero
 * tiene siempre 9 casillas: no hace falta carga perezosa ni desplazamiento.
 *
 * @param board Estado actual de la partida.
 * @param onCellClick Acción que se ejecuta al pulsar una casilla; recibe su posición (0–8).
 * @param modifier Modificador externo para ajustar tamaño o posición desde el padre.
 */
@Composable
fun BoardGrid(
    board: BoardModel,
    onCellClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f) // tablero siempre cuadrado
    ) {
        board.cells.chunked(BoardModel.SIZE).forEach { rowCells ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                rowCells.forEach { cell ->
                    CellItemView(
                        cell = cell,
                        enabled = !board.isGameOver,
                        onClick = { onCellClick(cell.position) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
