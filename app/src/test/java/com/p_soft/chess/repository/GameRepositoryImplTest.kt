package com.p_soft.chess.repository

import com.p_soft.chess.data.local.GameDao
import com.p_soft.chess.data.local.GameEntity
import com.p_soft.chess.data.repository.GameRepositoryImpl
import com.p_soft.chess.domain.model.BoardState
import com.p_soft.chess.domain.model.Move
import com.p_soft.chess.domain.model.PieceType
import com.p_soft.chess.domain.model.Player
import com.p_soft.chess.domain.model.Square
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GameRepositoryImplTest {

    private lateinit var gameDao: GameDao
    private lateinit var repository: GameRepositoryImpl
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        gameDao = mockk(relaxed = true)
        repository = GameRepositoryImpl(gameDao)
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    /*@Test
    fun `saveGame stores board state`() = runTest {
        val boardState = BoardState.initial()

        repository.saveGame(boardState)

        coVerify { gameDao.saveGame(any()) }
    }

    @Test
    fun `loadGame returns saved state`() = runTest {
        val boardState = BoardState.initial()
        val json = """{"board":{},"currentPlayer":"WHITE","moveHistory":[]}"""
        val entity = GameEntity(
            id = "test-id",
            boardState = json,
            currentPlayer = "WHITE",
            status = "ACTIVE"
        )

        coEvery { gameDao.getLastGame() } returns entity

        val result = repository.loadGame()
        assertNotNull(result)
        assertEquals(Player.WHITE, result?.currentPlayer)
    }*/

    @Test
    fun `loadGame returns null when no saved game`() = runTest {
        coEvery { gameDao.getLastGame() } returns null

        val result = repository.loadGame()
        assertNull(result)
    }

    @Test
    fun `clearGame deletes all games`() = runTest {
        repository.clearGame()

        coVerify { gameDao.deleteAllGames() }
    }

    @Test
    fun `saveMove stores move entity`() = runTest {
        val move = Move(
            from = Square(1, 0),
            to = Square(3, 0)
        )

        repository.saveMove("game-1", move, PieceType.PAWN)

        coVerify { gameDao.saveMove(any()) }
    }
}