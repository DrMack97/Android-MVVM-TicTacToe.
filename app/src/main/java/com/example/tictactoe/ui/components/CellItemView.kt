package com.example.tictactoe.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tictactoe.model.BoardModel
import com.example.tictactoe.model.CellModel

/** Color del símbolo "X". */
private val ColorX = Color(0xFF1E88E5)

/** Color del símbolo "O". */
private val ColorO = Color(0xFFE53935)

/**
 * Dibuja una casilla del tablero.
 *
 * Componente sin estado: solo pinta la [cell] que recibe y avisa del clic
 * mediante [onClick]. No decide si el movimiento es válido; eso lo hace el modelo.
 *
 * @param cell Casilla que se va a pintar.
 * @param enabled `false` cuando la partida ha terminado; en ese caso la casilla no responde.
 * @param onClick Acción que se ejecuta al pulsar la casilla. Solo se puede pulsar
 * si [enabled] es `true` y la casilla está vacía.
 * @param modifier Modificador externo para ajustar tamaño o posición desde el padre.
 */
@Composable
fun CellItemView(
    cell: CellModel,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(8.dp)

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .aspectRatio(1f) // casilla siempre cuadrada
            .clip(shape)
            .border(width = 2.dp, color = MaterialTheme.colorScheme.outline, shape = shape)
            .clickable(enabled = enabled && cell.symbol.isEmpty(), onClick = onClick)
    ) {
        Text(
            text = cell.symbol,
            fontSize = 56.sp,
            fontWeight = FontWeight.Bold,
            color = if (cell.symbol == BoardModel.PLAYER_X) ColorX else ColorO
        )
    }
}
