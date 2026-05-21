package com.p_soft.chess.data.repository

import com.p_soft.chess.data.local.GameDao
import com.p_soft.chess.data.local.GameEntity
import com.p_soft.chess.data.local.MoveEntity
import com.p_soft.chess.domain.model.BoardState
import com.p_soft.chess.domain.model.Move
import com.p_soft.chess.domain.model.Piece
import com.p_soft.chess.domain.model.PieceType
import com.p_soft.chess.domain.model.Player
import com.p_soft.chess.domain.model.Square
import com.p_soft.chess.domain.repository.GameRepository

import javax.inject.Inject
import javax.inject.Singleton

import org.json.JSONArray
import org.json.JSONObject

@Singleton
class GameRepositoryImpl @Inject constructor(
    private val gameDao: GameDao
) : GameRepository {

    override suspend fun saveGame(boardState: BoardState) {
        val json = boardStateToJson(boardState)
        val entity = GameEntity(
            boardState = json,
            currentPlayer = boardState.currentPlayer.name,
            status = boardState.toGameStatus().name,
            updatedAt = System.currentTimeMillis()
        )
        gameDao.saveGame(entity)
    }

    override suspend fun loadGame(): BoardState? {
        val entity = gameDao.getLastGame() ?: return null
        return jsonToBoardState(entity.boardState)
    }

    override suspend fun clearGame() {
        gameDao.deleteAllGames()
    }

    override suspend fun saveMove(gameId: String, move: Move, pieceType: PieceType) {
        val entity = MoveEntity(
            gameId = gameId,
            fromRow = move.from.row,
            fromCol = move.from.col,
            toRow = move.to.row,
            toCol = move.to.col,
            pieceType = pieceType.name,
            isCapture = move.promotion != null,
            timestamp = System.currentTimeMillis()
        )
        gameDao.saveMove(entity)
    }

    private fun boardStateToJson(state: BoardState): String {
        val json = JSONObject()

        // Сохраняем доску
        val boardJson = JSONObject()
        state.pieces.forEach { (square, piece) ->
            val key = "${square.row},${square.col}"
            val pieceJson = JSONObject().apply {
                put("type", piece.type.name)
                put("player", piece.player.name)
                put("hasMoved", piece.hasMoved)
            }
            boardJson.put(key, pieceJson)
        }
        json.put("board", boardJson)

        // Сохраняем историю ходов
        val movesJson = JSONArray()
        state.moveHistory.forEach { move ->
            val moveJson = JSONObject().apply {
                put("fromRow", move.from.row)
                put("fromCol", move.from.col)
                put("toRow", move.to.row)
                put("toCol", move.to.col)
                move.promotion?.let { put("promotion", it.name) }
                put("isCastling", move.isCastling)
                put("isEnPassant", move.isEnPassant)
            }
            movesJson.put(moveJson)
        }
        json.put("moveHistory", movesJson)
        json.put("currentPlayer", state.currentPlayer.name)

        val capturedPiecesJson = JSONArray()
        state.capturedPieces.forEach { piece ->
            val pieceJson = JSONObject().apply {
                put("type", piece.type.name)
                put("player", piece.player.name)
                put("hasMoved", piece.hasMoved)
            }
            capturedPiecesJson.put(pieceJson)
        }
        json.put("capturedPieces", capturedPiecesJson)

        return json.toString()
    }

    private fun jsonToBoardState(json: String): BoardState {
        val jsonObj = JSONObject(json)

        // Восстанавливаем доску
        val pieces = mutableMapOf<Square, Piece>()
        val boardJson = jsonObj.getJSONObject("board")
        boardJson.keys().forEach { key ->
            val (row, col) = key.split(",").map { it.toInt() }
            val pieceJson = boardJson.getJSONObject(key)
            val piece = Piece(
                type = PieceType.valueOf(pieceJson.getString("type")),
                player = Player.valueOf(pieceJson.getString("player")),
                hasMoved = pieceJson.getBoolean("hasMoved")
            )
            pieces[Square(row, col)] = piece
        }

        // Восстанавливаем историю ходов
        val movesJson = jsonObj.getJSONArray("moveHistory")
        val moveHistory = mutableListOf<Move>()
        for (i in 0 until movesJson.length()) {
            val moveJson = movesJson.getJSONObject(i)
            val move = Move(
                from = Square(moveJson.getInt("fromRow"), moveJson.getInt("fromCol")),
                to = Square(moveJson.getInt("toRow"), moveJson.getInt("toCol")),
                promotion = if (moveJson.has("promotion"))
                    PieceType.valueOf(moveJson.getString("promotion"))
                else null,
                isCastling = moveJson.getBoolean("isCastling"),
                isEnPassant = moveJson.getBoolean("isEnPassant")
            )
            moveHistory.add(move)
        }

        val currentPlayer = Player.valueOf(jsonObj.getString("currentPlayer"))

        val capturedPiecesJson = jsonObj.getJSONArray("capturedPieces")
        val capturedPieces = mutableListOf<Piece>()
        for (i in 0 until capturedPiecesJson.length()) {
            val pieceJson = capturedPiecesJson.getJSONObject(i)
            val piece = Piece(
                type = PieceType.valueOf(pieceJson.getString("type")),
                player = Player.valueOf(pieceJson.getString("player")),
                hasMoved = pieceJson.getBoolean("hasMoved")
            )
            capturedPieces.add(piece)
        }

        return BoardState(
            pieces = pieces,
            currentPlayer = currentPlayer,
            moveHistory = moveHistory,
            capturedPieces = capturedPieces
        )
    }
}