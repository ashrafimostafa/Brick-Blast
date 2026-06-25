package com.mostafa.brickblast.navigation

import kotlinx.serialization.Serializable

@Serializable object SplashRoute
@Serializable object MainMenuRoute
@Serializable data class GameRoute(
    val mode: String = "CLASSIC",
    val challengeLevel: Int = 1,
    val continueGame: Boolean = false
)
@Serializable object PauseRoute
@Serializable object SettingsRoute
@Serializable object UpgradeRoute
@Serializable object StatisticsRoute
@Serializable object ShopRoute
@Serializable data class GameOverRoute(
    val score: Int,
    val round: Int,
    val mode: String,
    val challengeLevel: Int = 1
)
@Serializable data class VictoryRoute(
    val score: Int,
    val round: Int,
    val mode: String,
    val challengeLevel: Int = 1
)
@Serializable object ChallengeSelectRoute
