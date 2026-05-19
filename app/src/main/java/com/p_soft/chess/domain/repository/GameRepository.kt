package com.p_soft.chess.domain.repository

import com.p_soft.chess.domain.model.BoardState
import com.p_soft.chess.domain.model.Move
import com.p_soft.chess.domain.model.PieceType

interface GameRepository {
    suspend fun saveGame(boardState: BoardState)
    suspend fun loadGame(): BoardState?
    suspend fun clearGame()
    suspend fun saveMove(gameId: String, move: Move, pieceType: PieceType)
}
