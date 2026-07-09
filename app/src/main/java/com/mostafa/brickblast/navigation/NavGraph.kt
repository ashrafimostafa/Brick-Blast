package com.mostafa.brickblast.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.mostafa.brickblast.domain.model.GameMode
import com.mostafa.brickblast.ui.screens.achievements.AchievementsScreen
import com.mostafa.brickblast.ui.screens.challenge.ChallengeSelectScreen
import com.mostafa.brickblast.ui.viewmodel.ChallengeViewModel
import com.mostafa.brickblast.ui.screens.game.GameOverScreen
import com.mostafa.brickblast.ui.screens.game.GameScreen
import com.mostafa.brickblast.ui.screens.game.PauseScreen
import com.mostafa.brickblast.ui.screens.game.VictoryScreen
import com.mostafa.brickblast.ui.screens.menu.MainMenuScreen
import com.mostafa.brickblast.ui.screens.menu.SplashScreen
import com.mostafa.brickblast.ui.screens.settings.SettingsScreen
import com.mostafa.brickblast.ui.screens.shop.ShopScreen
import com.mostafa.brickblast.ui.screens.statistics.StatisticsScreen
import com.mostafa.brickblast.ui.screens.upgrade.UpgradeScreen
import com.mostafa.brickblast.ui.viewmodel.GameViewModel
import com.mostafa.brickblast.ui.viewmodel.MenuViewModel
import kotlinx.coroutines.launch

@Composable
fun BrickBlastNavGraph(navController: NavHostController) {
    val animDuration = 300
    NavHost(
        navController = navController,
        startDestination = SplashRoute,
        enterTransition = { fadeIn(tween(animDuration)) + slideInHorizontally { it / 4 } },
        exitTransition = { fadeOut(tween(animDuration)) + slideOutHorizontally { -it / 4 } },
        popEnterTransition = { fadeIn(tween(animDuration)) + slideInHorizontally { -it / 4 } },
        popExitTransition = { fadeOut(tween(animDuration)) + slideOutHorizontally { it / 4 } }
    ) {
        composable<SplashRoute> {
            SplashScreen(onFinished = {
                navController.navigate(MainMenuRoute) {
                    popUpTo(SplashRoute) { inclusive = true }
                }
            })
        }

        composable<MainMenuRoute> {
            val vm: MenuViewModel = hiltViewModel()
            val hasSave by vm.hasActiveSave.collectAsState()
            val coins by vm.coins.collectAsState()
            val scope = rememberCoroutineScope()
            MainMenuScreen(
                hasActiveSave = hasSave,
                coins = coins,
                onPlay = { navController.navigate(GameRoute(mode = "CLASSIC")) },
                onChallenge = { navController.navigate(ChallengeSelectRoute) },
                onTimeAttack = { navController.navigate(GameRoute(mode = "TIME_ATTACK")) },
                onHardcore = { navController.navigate(GameRoute(mode = "HARDCORE")) },
                onShop = { navController.navigate(ShopRoute) },
                onSettings = { navController.navigate(SettingsRoute) },
                onStatistics = { navController.navigate(StatisticsRoute) },
                onAchievements = { navController.navigate(AchievementsRoute) },
                onContinue = {
                    scope.launch {
                        val save = vm.getRecentSave() ?: return@launch
                        navController.navigate(
                            GameRoute(
                                mode = save.mode.name,
                                challengeLevel = save.challengeLevel,
                                continueGame = true
                            )
                        )
                    }
                }
            )
        }

        composable<ChallengeSelectRoute> {
            val vm: ChallengeViewModel = hiltViewModel()
            val progress by vm.progress.collectAsState()
            ChallengeSelectScreen(
                progress = progress,
                onSelectLevel = { level ->
                    navController.navigate(GameRoute(mode = "CHALLENGE", challengeLevel = level))
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable<GameRoute> { entry ->
            val route = entry.toRoute<GameRoute>()
            val mode = GameMode.valueOf(route.mode)
            val vm: GameViewModel = hiltViewModel()
            GameScreen(
                viewModel = vm,
                mode = mode,
                challengeLevel = route.challengeLevel,
                continueGame = route.continueGame,
                onPause = { navController.navigate(PauseRoute) },
                onGameOver = { score, round, coinsEarned ->
                    navController.navigate(
                        GameOverRoute(score, round, route.mode, route.challengeLevel, coinsEarned)
                    ) {
                        popUpTo<GameRoute> { inclusive = true }
                    }
                },
                onVictory = { score, round, coinsEarned ->
                    navController.navigate(
                        VictoryRoute(score, round, route.mode, route.challengeLevel, coinsEarned)
                    ) {
                        popUpTo<GameRoute> { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable<PauseRoute> {
            val parentEntry = navController.previousBackStackEntry
            val vm: GameViewModel = if (parentEntry != null) hiltViewModel(parentEntry) else hiltViewModel()
            PauseScreen(
                onResume = { navController.popBackStack() },
                onQuit = {
                    vm.saveAndQuit()
                    navController.popBackStack(MainMenuRoute, inclusive = false)
                }
            )
        }

        composable<SettingsRoute> {
            SettingsScreen(onBack = { navController.popBackStack() })
        }

        composable<UpgradeRoute> {
            UpgradeScreen(onBack = { navController.popBackStack() })
        }

        composable<StatisticsRoute> {
            StatisticsScreen(
                onBack = { navController.popBackStack() },
                onAchievements = { navController.navigate(AchievementsRoute) }
            )
        }

        composable<AchievementsRoute> {
            AchievementsScreen(onBack = { navController.popBackStack() })
        }

        composable<ShopRoute> {
            ShopScreen(onBack = { navController.popBackStack() })
        }

        composable<GameOverRoute> { entry ->
            val route = entry.toRoute<GameOverRoute>()
            GameOverScreen(
                score = route.score,
                round = route.round,
                mode = route.mode,
                coinsEarned = route.coinsEarned,
                onRetry = {
                    navController.navigate(GameRoute(route.mode, route.challengeLevel)) {
                        popUpTo(MainMenuRoute)
                    }
                },
                onBack = { navController.popBackStack(MainMenuRoute, inclusive = false) }
            )
        }

        composable<VictoryRoute> { entry ->
            val route = entry.toRoute<VictoryRoute>()
            VictoryScreen(
                score = route.score,
                round = route.round,
                mode = route.mode,
                challengeLevel = route.challengeLevel,
                coinsEarned = route.coinsEarned,
                onRetry = {
                    navController.navigate(
                        GameRoute(route.mode, route.challengeLevel)
                    ) { popUpTo(MainMenuRoute) }
                },
                onNextLevel = {
                    navController.navigate(
                        GameRoute("CHALLENGE", route.challengeLevel + 1)
                    ) { popUpTo(MainMenuRoute) }
                },
                onBack = { navController.popBackStack(MainMenuRoute, inclusive = false) }
            )
        }
    }
}
