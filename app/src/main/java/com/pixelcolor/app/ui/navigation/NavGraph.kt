package com.pixelcolor.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.pixelcolor.app.ui.screen.canvas.CanvasScreen
import com.pixelcolor.app.ui.screen.completion.CompletionScreen
import com.pixelcolor.app.ui.screen.daily.DailyPuzzleScreen
import com.pixelcolor.app.ui.screen.detail.PuzzleDetailScreen
import com.pixelcolor.app.ui.screen.home.HomeScreen
import com.pixelcolor.app.ui.screen.settings.SettingsScreen
import com.pixelcolor.app.ui.screen.splash.SplashScreen

object Routes {
    const val SPLASH = "splash"
    const val HOME = "home"
    const val PUZZLE_DETAIL = "puzzle_detail/{puzzleId}"
    const val CANVAS = "canvas/{puzzleId}"
    const val COMPLETION = "completion/{puzzleId}"
    const val DAILY_PUZZLE = "daily_puzzle"
    const val SETTINGS = "settings"

    fun puzzleDetail(puzzleId: String) = "puzzle_detail/$puzzleId"
    fun canvas(puzzleId: String) = "canvas/$puzzleId"
    fun completion(puzzleId: String) = "completion/$puzzleId"
}

@Composable
fun PixelColorNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Routes.SPLASH) {
        composable(Routes.SPLASH) {
            SplashScreen(
                onSplashComplete = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.HOME) {
            HomeScreen(
                onPuzzleClick = { puzzleId ->
                    navController.navigate(Routes.puzzleDetail(puzzleId))
                },
                onSettingsClick = {
                    navController.navigate(Routes.SETTINGS)
                },
                onDailyPuzzleClick = {
                    navController.navigate(Routes.DAILY_PUZZLE)
                }
            )
        }

        composable(
            route = Routes.PUZZLE_DETAIL,
            arguments = listOf(navArgument("puzzleId") { type = NavType.StringType })
        ) { backStackEntry ->
            val puzzleId = backStackEntry.arguments?.getString("puzzleId") ?: return@composable
            PuzzleDetailScreen(
                puzzleId = puzzleId,
                onStartClick = {
                    navController.navigate(Routes.canvas(puzzleId))
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.CANVAS,
            arguments = listOf(navArgument("puzzleId") { type = NavType.StringType })
        ) { backStackEntry ->
            val puzzleId = backStackEntry.arguments?.getString("puzzleId") ?: return@composable
            CanvasScreen(
                puzzleId = puzzleId,
                onComplete = {
                    navController.navigate(Routes.completion(puzzleId)) {
                        popUpTo(Routes.HOME)
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.COMPLETION,
            arguments = listOf(navArgument("puzzleId") { type = NavType.StringType })
        ) { backStackEntry ->
            val puzzleId = backStackEntry.arguments?.getString("puzzleId") ?: return@composable
            CompletionScreen(
                puzzleId = puzzleId,
                onBackToHome = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                },
                onNextPuzzle = { nextId ->
                    navController.navigate(Routes.puzzleDetail(nextId)) {
                        popUpTo(Routes.HOME)
                    }
                }
            )
        }

        composable(Routes.DAILY_PUZZLE) {
            DailyPuzzleScreen(
                onPlayClick = { puzzleId ->
                    navController.navigate(Routes.canvas(puzzleId))
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
