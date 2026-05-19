package com.p_soft.chess.domain.model

/**
 * Статус шахматной игры
 */
enum class GameStatus {
    /** Игра продолжается */
    ACTIVE,

    /** Объявлен шах */
    CHECK,

    /** Мат - конец игры, победа атакующей стороны */
    CHECKMATE,

    /** Пат - ничья, нет доступных ходов, но король не под шахом */
    STALEMATE,

    /** Ничья по соглашению */
    DRAW_AGREED,

    /** Ничья из-за троекратного повторения позиции */
    DRAW_THREEFOLD_REPETITION,

    /** Ничья из-за правила 50 ходов */
    DRAW_FIFTY_MOVE_RULE,

    /** Ничья из-за недостаточности материала */
    DRAW_INSUFFICIENT_MATERIAL,

    /** Игра не начата */
    NOT_STARTED,

    /** Игра приостановлена */
    PAUSED,

    /** Игра завершена (общий статус для окончания) */
    FINISHED;

    /**
     * Является ли статус финальным (игра окончена)
     */
    fun isGameOver(): Boolean {
        return this in listOf(
            CHECKMATE,
            STALEMATE,
            DRAW_AGREED,
            DRAW_THREEFOLD_REPETITION,
            DRAW_FIFTY_MOVE_RULE,
            DRAW_INSUFFICIENT_MATERIAL,
            FINISHED
        )
    }

    /**
     * Является ли статус ничьей
     */
    fun isDraw(): Boolean {
        return this in listOf(
            STALEMATE,
            DRAW_AGREED,
            DRAW_THREEFOLD_REPETITION,
            DRAW_FIFTY_MOVE_RULE,
            DRAW_INSUFFICIENT_MATERIAL
        )
    }

    /**
     * Является ли статус победой одной из сторон
     */
    fun isWin(): Boolean {
        return this == CHECKMATE
    }

    /**
     * Требуется ли действие от игрока
     */
    fun requiresPlayerAction(): Boolean {
        return this in listOf(ACTIVE, CHECK)
    }

    /**
     * Получить текстовое описание статуса
     */
    fun getDescription(winner: Player? = null): String {
        return when (this) {
            ACTIVE -> "Игра продолжается"
            CHECK -> "Шах!"
            CHECKMATE -> when (winner) {
                Player.WHITE -> "Мат! Белые победили"
                Player.BLACK -> "Мат! Чёрные победили"
                null -> "Мат!"
            }
            STALEMATE -> "Пат! Ничья"
            DRAW_AGREED -> "Ничья по соглашению"
            DRAW_THREEFOLD_REPETITION -> "Ничья. Троекратное повторение"
            DRAW_FIFTY_MOVE_RULE -> "Ничья. Правило 50 ходов"
            DRAW_INSUFFICIENT_MATERIAL -> "Ничья. Недостаточно материала"
            NOT_STARTED -> "Игра не начата"
            PAUSED -> "Игра приостановлена"
            FINISHED -> "Игра завершена"
        }
    }

    /**
     * Получить иконку для статуса (если нужно для UI)
     */
    fun getEmoji(): String {
        return when (this) {
            ACTIVE -> "♟️"
            CHECK -> "⚠️"
            CHECKMATE -> "👑"
            STALEMATE -> "🤝"
            DRAW_AGREED -> "🤝"
            DRAW_THREEFOLD_REPETITION -> "🔄"
            DRAW_FIFTY_MOVE_RULE -> "5️⃣0️⃣"
            DRAW_INSUFFICIENT_MATERIAL -> "📊"
            NOT_STARTED -> "🆕"
            PAUSED -> "⏸️"
            FINISHED -> "🏁"
        }
    }

    /**
     * Получить цвет для отображения статуса
     */
    fun getColor(): Long {
        return when (this) {
            ACTIVE -> 0xFF4CAF50     // Зелёный
            CHECK -> 0xFFFF9800      // Оранжевый
            CHECKMATE -> 0xFFF44336  // Красный
            STALEMATE,
            DRAW_AGREED,
            DRAW_THREEFOLD_REPETITION,
            DRAW_FIFTY_MOVE_RULE,
            DRAW_INSUFFICIENT_MATERIAL -> 0xFF2196F3  // Синий
            NOT_STARTED -> 0xFF9E9E9E  // Серый
            PAUSED -> 0xFFFFEB3B     // Жёлтый
            FINISHED -> 0xFF607D8B   // Сине-серый
        }
    }
}