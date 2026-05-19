package com.p_soft.chess.data.local

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import java.util.UUID

@Database(
    entities = [GameEntity::class, MoveEntity::class],
    version = 1,
    exportSchema = false
)
abstract class GameDatabase : RoomDatabase() {
    abstract fun gameDao(): GameDao
}

@Entity(tableName = "games")
data class GameEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val boardState: String, // JSON
    val currentPlayer: String,
    val status: String, // ACTIVE, CHECKMATE, STALEMATE
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "moves")
data class MoveEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val gameId: String,
    val fromRow: Int,
    val fromCol: Int,
    val toRow: Int,
    val toCol: Int,
    val pieceType: String,
    val isCapture: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface GameDao {
    @Query("SELECT * FROM games ORDER BY updatedAt DESC LIMIT 1")
    suspend fun getLastGame(): GameEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveGame(game: GameEntity)

    @Query("DELETE FROM games")
    suspend fun deleteAllGames()

    @Insert
    suspend fun saveMove(move: MoveEntity)

    @Query("SELECT * FROM moves WHERE gameId = :gameId ORDER BY timestamp ASC")
    suspend fun getMovesForGame(gameId: String): List<MoveEntity>
}