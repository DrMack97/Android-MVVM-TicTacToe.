package com.example.tictactoe.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests unitarios del modelo. Se ejecutan sin emulador porque
 * [BoardModel] no depende de Android.
 */
class BoardModelTest {

    /** Aplica una secuencia de movimientos a partir de un tablero nuevo. */
    private fun play(vararg positions: Int): BoardModel =
        positions.fold(BoardModel.createInitialBoard()) { board, p -> board.makeMove(p) }

    @Test
    fun tableroInicialVacioYTurnoDeX() {
        val board = BoardModel.createInitialBoard()
        assertEquals(9, board.cells.size)
        assertTrue(board.cells.none { it.isOccupied })
        assertEquals("X", board.currentTurn)
        assertNull(board.winner)
        assertFalse(board.isGameOver)
    }

    @Test
    fun moverColocaLaFichaYCambiaElTurno() {
        val board = play(4)
        assertEquals("X", board.cells[4].symbol)
        assertEquals("O", board.currentTurn)
    }

    @Test
    fun noSeModificaElTableroOriginal() {
        val original = BoardModel.createInitialBoard()
        original.makeMove(0)
        assertFalse(original.cells[0].isOccupied)
    }

    @Test
    fun casillaOcupadaDevuelveLaMismaInstancia() {
        val board = play(0)
        assertSame(board, board.makeMove(0))
    }

    @Test
    fun posicionFueraDeRangoDevuelveLaMismaInstancia() {
        val board = BoardModel.createInitialBoard()
        assertSame(board, board.makeMove(-1))
        assertSame(board, board.makeMove(9))
    }

    @Test
    fun ganaXPorFila() {
        // X: 0,1,2  O: 3,4
        val board = play(0, 3, 1, 4, 2)
        assertEquals("X", board.winner)
        assertTrue(board.isGameOver)
        assertFalse(board.isDraw)
    }

    @Test
    fun ganaOPorColumna() {
        // X: 0,1,3  O: 2,5,8 (columna derecha)
        val board = play(0, 2, 1, 5, 3, 8)
        assertEquals("O", board.winner)
    }

    @Test
    fun ganaXPorDiagonal() {
        val board = play(0, 1, 4, 2, 8)
        assertEquals("X", board.winner)
    }

    @Test
    fun empate() {
        // X | O | X
        // X | O | O
        // O | X | X
        val board = play(0, 1, 2, 4, 3, 5, 7, 6, 8)
        assertNull(board.winner)
        assertTrue(board.isDraw)
        assertTrue(board.isGameOver)
    }

    @Test
    fun noSePuedeMoverTrasTerminar() {
        val finished = play(0, 3, 1, 4, 2)
        assertSame(finished, finished.makeMove(8))
    }
}
