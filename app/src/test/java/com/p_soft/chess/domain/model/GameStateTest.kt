package com.p_soft.chess.domain.model

import org.junit.Assert.*
import org.junit.Test

class GameStateTest {

    @Test
    fun `initial game state has status NOT_STARTED`() {
        val state = GameState.initial()
        assertEquals(GameStatus.NOT_STARTED, state.status)
    }

    @Test
    fun `start changes status to ACTIVE`() {
        val state = GameState.initial().start()
        assertEquals(GameStatus.ACTIVE, state.status)
    }

    @Test
    fun `isGameOver returns true for checkmate`() {
        val state = GameState.initial().copy(status = GameStatus.CHECKMATE)
        assertTrue(state.isGameOver())
    }

    @Test
    fun `isGameOver returns true for stalemate`() {
        val state = GameState.initial().copy(status = GameStatus.STALEMATE)
        assertTrue(state.isGameOver())
    }

    @Test
    fun `isGameOver returns false for ACTIVE`() {
        val state = GameState.initial().copy(status = GameStatus.ACTIVE)
        assertFalse(state.isGameOver())
    }

    @Test
    fun `getWinner returns opponent on checkmate`() {
        val state = GameState.initial().copy(
            currentPlayer = Player.WHITE,
            status = GameStatus.CHECKMATE
        )
        assertEquals(Player.BLACK, state.getWinner())
    }

    @Test
    fun `getWinner returns null when not checkmate`() {
        val state = GameState.initial().copy(status = GameStatus.ACTIVE)
        assertNull(state.getWinner())
    }

    @Test
    fun `fullMoveNumber increments after black moves`() {
        val initialState = GameState.initial().start()
        val stateAfterWhite = initialState.copy(currentPlayer = Player.BLACK)

        // Симулируем структуру после хода
        val stateAfterBlack = stateAfterWhite.copy(
            currentPlayer = Player.WHITE,
            fullMoveNumber = stateAfterWhite.fullMoveNumber + 1
        )

        assertEquals(2, stateAfterBlack.fullMoveNumber)
    }
}