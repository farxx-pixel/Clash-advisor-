package com.vanta.clashadvisor

data class Card(
    val id: String,
    val name: String,
    val elixir: Int,
    val counters: Set<String>
)

data class Recommendation(
    val card: String,
    val score: Float,
    val reason: String
)

class StrategyEngine {

    private val cards = listOf(
        Card(
            "cannon",
            "Cannon",
            3,
            setOf(
                "hog_rider",
                "battle_ram",
                "ram_rider"
            )
        ),

        Card(
            "mini_pekka",
            "Mini P.E.K.K.A",
            4,
            setOf(
                "giant",
                "golem",
                "pekka",
                "mega_knight"
            )
        ),

        Card(
            "valkyrie",
            "Valkyrie",
            4,
            setOf(
                "skeleton_army",
                "witch",
                "wizard",
                "barbarians"
            )
        ),

        Card(
            "musketeer",
            "Musketeer",
            4,
            setOf(
                "baby_dragon",
                "balloon",
                "inferno_dragon"
            )
        ),

        Card(
            "knight",
            "Knight",
            3,
            setOf(
                "knight",
                "valkyrie",
                "wizard"
            )
        )
    )

    fun recommend(
        state: BattleState
    ): List<Recommendation> {

        val enemies = state.units
            .filter { it.team == Team.ENEMY }
            .filter { it.confidence >= 0.60f }

        if (enemies.isEmpty()) {
            return listOf(
                Recommendation(
                    card = "HOLD",
                    score = 0f,
                    reason = "No high-priority threat detected"
                )
            )
        }

        return cards
            .mapNotNull { card ->

                val matchedThreat =
                    enemies.firstOrNull {
                        it.cardId in card.counters
                    } ?: return@mapNotNull null

                if (card.elixir > state.friendlyElixir) {
                    return@mapNotNull null
                }

                val score =
                    100f +
                    matchedThreat.confidence * 20f -
                    card.elixir * 4f

                Recommendation(
                    card = card.name,
                    score = score,
                    reason =
                        "Counter ${matchedThreat.cardId}"
                )
            }
            .sortedByDescending { it.score }
            .take(3)
    }
}
