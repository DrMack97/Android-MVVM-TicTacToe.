package com.example.tictactoe.model

/**
 * Representa una casilla del tablero de Tres en Raya.
 *
 * Es inmutable: para cambiar su contenido se crea una casilla nueva con [copy].
 *
 * @property id Identificador único de la casilla (0–8).
 * @property position Índice lineal de la casilla en el tablero (0–8), leído
 * de izquierda a derecha y de arriba abajo:
 * ```
 *  0 | 1 | 2
 *  3 | 4 | 5
 *  6 | 7 | 8
 * ```
 * @property symbol Contenido de la casilla: [BoardModel.PLAYER_X], [BoardModel.PLAYER_O]
 * o [BoardModel.EMPTY] si está vacía.
 */
data class CellModel(
    val id: Int,
    val position: Int,
    val symbol: String = BoardModel.EMPTY
) {
    /** `true` si la casilla ya tiene una ficha ("X" u "O"). */
    val isOccupied: Boolean
        get() = symbol.isNotEmpty()

    /** Fila de la casilla (0–2), calculada a partir de [position]. */
    val row: Int
        get() = position / BoardModel.SIZE

    /** Columna de la casilla (0–2), calculada a partir de [position]. */
    val column: Int
        get() = position % BoardModel.SIZE
}

/**
 * Estado completo de una partida de Tres en Raya y sus reglas.
 *
 * Es lógica pura: no depende de Android, Compose ni del ViewModel.
 * Es inmutable: ningún método modifica la instancia actual; todos
 * devuelven un [BoardModel] nuevo creado con [copy].
 *
 * @property cells Lista ordenada de las 9 casillas; el índice coincide con [CellModel.position].
 * @property currentTurn Símbolo del jugador al que le toca mover ([PLAYER_X] o [PLAYER_O]).
 * @property winner Símbolo del ganador, o `null` si todavía no hay ganador.
 * @property isDraw `true` si las 9 casillas están ocupadas y nadie ha ganado.
 * @property isGameOver `true` si la partida terminó por victoria o empate.
 */
data class BoardModel(
    val cells: List<CellModel> = emptyList(),
    val currentTurn: String = PLAYER_X,
    val winner: String? = null,
    val isDraw: Boolean = false,
    val isGameOver: Boolean = false
) {

    /** Constantes del juego y método de fábrica. */
    companion object {
        /** Símbolo del jugador X, que siempre empieza. */
        const val PLAYER_X = "X"

        /** Símbolo del jugador O. */
        const val PLAYER_O = "O"

        /** Contenido de una casilla vacía. */
        const val EMPTY = ""

        /** Número de filas y de columnas del tablero. */
        const val SIZE = 3

        /** Número total de casillas del tablero. */
        const val CELL_COUNT = SIZE * SIZE

        /**
         * Las 8 combinaciones ganadoras, expresadas como posiciones del tablero:
         * 3 filas, 3 columnas y 2 diagonales.
         */
        val WINNING_LINES: List<List<Int>> = listOf(
            listOf(0, 1, 2), listOf(3, 4, 5), listOf(6, 7, 8), // filas
            listOf(0, 3, 6), listOf(1, 4, 7), listOf(2, 5, 8), // columnas
            listOf(0, 4, 8), listOf(2, 4, 6)                   // diagonales
        )

        /**
         * Crea el tablero de una partida nueva.
         *
         * @return Un [BoardModel] con 9 casillas vacías, turno de [PLAYER_X],
         * sin ganador, sin empate y con la partida activa.
         */
        fun createInitialBoard(): BoardModel = BoardModel(
            cells = List(CELL_COUNT) { index -> CellModel(id = index, position = index) }
        )
    }

    /**
     * Coloca la ficha del jugador actual en la casilla indicada.
     *
     * Si el movimiento es válido, coloca [currentTurn] en la casilla, comprueba
     * si hay ganador o empate y, si la partida sigue, pasa el turno al otro jugador.
     *
     * @param position Índice de la casilla (0–8).
     * @return Un [BoardModel] nuevo con el movimiento aplicado. Si el movimiento
     * es inválido (posición fuera de rango, casilla ocupada o partida terminada),
     * devuelve **esta misma instancia sin cambios**.
     */
    fun makeMove(position: Int): BoardModel {
        if (position !in 0 until CELL_COUNT) return this
        if (isGameOver) return this
        if (cells[position].isOccupied) return this

        val updatedCells = cells.map { cell ->
            if (cell.position == position) cell.copy(symbol = currentTurn) else cell
        }
        val boardWithMove = copy(cells = updatedCells)

        val newWinner = boardWithMove.checkWinner()
        val newIsDraw = boardWithMove.checkDraw()
        val gameOver = newWinner != null || newIsDraw

        return boardWithMove.copy(
            winner = newWinner,
            isDraw = newIsDraw,
            isGameOver = gameOver,
            // Si la partida ha terminado, el turno se queda en quien hizo la última jugada
            currentTurn = if (gameOver) currentTurn else nextTurn()
        )
    }

    /**
     * Evalúa las 8 combinaciones ganadoras de [WINNING_LINES].
     *
     * @return [PLAYER_X] o [PLAYER_O] si alguno tiene tres fichas en línea,
     * o `null` si no hay ganador.
     */
    fun checkWinner(): String? {
        for (line in WINNING_LINES) {
            val (a, b, c) = line.map { cells[it].symbol }
            if (a.isNotEmpty() && a == b && b == c) return a
        }
        return null
    }

    /**
     * Comprueba si la partida ha terminado en empate.
     *
     * @return `true` si las 9 casillas están ocupadas y no hay ganador;
     * `false` en cualquier otro caso.
     */
    fun checkDraw(): Boolean = cells.all { it.isOccupied } && checkWinner() == null

    /**
     * Calcula el jugador que mueve después del actual.
     *
     * @return [PLAYER_O] si el turno actual es de [PLAYER_X], y viceversa.
     */
    private fun nextTurn(): String = if (currentTurn == PLAYER_X) PLAYER_O else PLAYER_X
}
