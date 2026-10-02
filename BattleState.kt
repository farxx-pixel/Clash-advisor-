package com.vanta.clashadvisor

data class Vec2(
    val x: Float,
    val y: Float
)

enum class Team {
    FRIENDLY,
    ENEMY
}

data class DetectedUnit(
    val cardId: String,
    val team: Team,
    val position: Vec2,
    val confidence: Float
)

data class BattleState(
    val timestamp: Long,
    val units: List<DetectedUnit>,
    val friendlyElixir: Float,
    val enemyElixir: Float
)
