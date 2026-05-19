package com.p_soft.chess.di

import android.content.Context
import androidx.room.Room
import com.p_soft.chess.data.local.GameDao
import com.p_soft.chess.data.local.GameDatabase
import com.p_soft.chess.data.repository.GameRepositoryImpl
import com.p_soft.chess.domain.repository.GameRepository
import com.p_soft.chess.domain.usecase.GameInteractor
import com.p_soft.chess.engine.ChessEngine
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): GameDatabase {
        return Room.databaseBuilder(
            context,
            GameDatabase::class.java,
            "chess_database"
        ).build()
    }

    @Provides
    @Singleton
    fun provideGameDao(database: GameDatabase): GameDao {
        return database.gameDao()
    }

    @Provides
    @Singleton
    fun provideChessEngine(): ChessEngine {
        return ChessEngine()
    }

    @Provides
    @Singleton
    fun provideGameRepository(
        gameDao: GameDao,
        chessEngine: ChessEngine
    ): GameRepository {
        return GameRepositoryImpl(gameDao)
    }

    @Provides
    @Singleton
    fun provideGameInteractor(
        gameRepository: GameRepository,
        chessEngine: ChessEngine
    ): GameInteractor {
        return GameInteractor(gameRepository, chessEngine)
    }
}