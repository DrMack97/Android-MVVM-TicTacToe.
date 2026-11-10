package com.example.tictactoe.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope // Preparado para operaciones asíncronas futuras (no se usa todavía)
import com.example.tictactoe.model.BoardModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel de la partida de Tres en Raya.
 *
 * Responsabilidades:
 * - **Guardar** el estado actual de la partida ([BoardModel]) y conservarlo
 *   aunque la pantalla se recree (por ejemplo, al girar el dispositivo).
 * - **Recibir** las acciones del usuario desde la View.
 * - **Delegar** todas las reglas del juego en el modelo ([BoardModel.makeMove]).
 * - **Exponer** el estado a la View como un [StateFlow] de solo lectura.
 *
 * No contiene ninguna regla del juego: no sabe qué es un movimiento válido,
 * cuándo hay ganador ni de quién es el turno. Todo eso lo decide [BoardModel].
 */
class GameViewModel : ViewModel() {

    /**
     * Estado interno y modificable de la partida.
     *
     * Es privado para que solo este ViewModel pueda cambiarlo
     * (única fuente de verdad del estado).
     */
    private val _boardState = MutableStateFlow(BoardModel.createInitialBoard())

    /**
     * Estado público de la partida, de solo lectura.
     *
     * La View lo observa (con `collectAsState()` en Compose) y se recompone
     * automáticamente cada vez que se emite un [BoardModel] nuevo.
     */
    val boardState: StateFlow<BoardModel> = _boardState.asStateFlow()

    /**
     * Procesa la pulsación del usuario sobre una casilla.
     *
     * Pide al modelo el tablero resultante con [BoardModel.makeMove] y lo publica
     * en [_boardState]. Si el movimiento es inválido (casilla ocupada, posición
     * fuera de rango o partida terminada), el modelo devuelve el mismo tablero
     * y la pantalla no cambia.
     *
     * Estado que modifica: [_boardState].
     *
     * @param position Índice de la casilla pulsada (0–8).
     */
    fun onCellClick(position: Int) {
        _boardState.value = _boardState.value.makeMove(position)
    }

    /**
     * Empieza una partida nueva.
     *
     * Sustituye el estado actual por un tablero inicial creado con
     * [BoardModel.createInitialBoard]: 9 casillas vacías y turno de "X".
     *
     * Estado que modifica: [_boardState].
     */
    fun restartGame() {
        _boardState.value = BoardModel.createInitialBoard()
    }
}
