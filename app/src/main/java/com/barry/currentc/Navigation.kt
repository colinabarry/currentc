package com.barry.currentc

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.barry.currentc.model.MoneyRecord
import com.barry.currentc.screens.AddAnnotatedImage
import com.barry.currentc.screens.AddMoneyRecord
import com.barry.currentc.screens.Home
import com.barry.currentc.screens.MovieInfo
import com.barry.currentc.screens.Search

enum class Screen {
    Login,
    SignUp,
    Home,
    Settings,
    Search,
    MovieInfo,
    AddMoneyRecord,
    AddAnnotatedImage
}

@Composable
fun MainView(
    viewModel: MainViewModel,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.name,
//        startDestination = Screen.Login.name,
//        startDestination = Screen.Search.name,
//        startDestination = "${Screen.MovieInfo.name}/{movie}",
//        startDestination = Screen.AddMoneyRecord.name
    ) {
        val appState = AppState(navController)
//        composable(route = Screen.Login.name) {
//            Login(
//                onLogin = { navController.navigate(Screen.Home.name) },
//            )
//        }
//        composable(route = Screen.Login.name) {
//            LoginScreen(openAndPopUp = { route, popUp -> appState.navigateAndPopUp(route, popUp) })
//        }
//
//        composable(route = Screen.SignUp.name) {
//            SignUpScreen(openAndPopUp = { route, popUp -> appState.navigateAndPopUp(route, popUp) })
//        }

//        composable(route = Screen.Settings.name) {
//            SettingsScreen(
//                restartApp = { route -> appState.navigate(route) },
//                openScreen = { route -> appState.navigate(route) }
//            )
//        }

        composable(route = Screen.AddMoneyRecord.name) {
            AddMoneyRecord(
                onClickUpload = { movieId, record -> viewModel.addMoneyRecord(movieId, record) },
                onUploadSucceeded = appState::popUp,
                movieId = viewModel.currentMovieId,
            )
        }

        composable(route = Screen.AddAnnotatedImage.name) {
            AddAnnotatedImage(
//                onClickUpload = viewModel::pickImage(),
//                onUploadSucceeded = appState::popUp(),
//                movieId = viewModel.currentMovieId,
            )
        }

        composable(route = Screen.Home.name) {
            Home(
                getPopularMovies = viewModel::getPopularMovies,
                getTopRatedMovies = viewModel::getTopRatedMovies,
                getNowPlayingMovies = viewModel::getNowPlayingMovies,
                onClickSearch = { appState.navigate(Screen.Search.name) },
                onClickMovie = { movieId: Int ->
                    appState.navigate("${Screen.MovieInfo.name}/$movieId")
                }
            )
        }

        composable(route = Screen.Search.name) {
            Search(
                onSearch = { searchTerm: String, page: Int ->
                    viewModel.searchMovies(searchTerm, page)
                },
                onClickResult = { movieId: Int ->
                    appState.navigate("${Screen.MovieInfo.name}/$movieId")
                }
            )
        }

        composable(
            route = "${Screen.MovieInfo.name}/{movie}",
            arguments = listOf(navArgument("movie") {
                type = NavType.IntType
                defaultValue = 769
            })
        ) { backStackEntry ->
            MovieInfo(
                getMovie = { viewModel.getMovie(it) },
                getReleaseInfo = { viewModel.getReleaseInfo(it) },
                getCredits = { viewModel.getCredits(it) },
                onAddMoneyRecord = { appState.navigate(Screen.AddMoneyRecord.name) },
                getMoneyRecords = { viewModel.getMoneyRecords(it) },
                getPriceChange = { record: MoneyRecord ->
                    viewModel.getPriceChangeToToday(record)
                },
                onPickImagePressed = viewModel::pickImage,
                getUserImages = { viewModel.getImages(it) },
                onBackButtonPressed = appState::popUp,
                movieId = backStackEntry.arguments?.getInt("movie")
            )
        }
    }
}

class AppState(
    val navController: NavHostController
) {
    fun popUp() {
        navController.popBackStack()
    }

    fun navigate(route: String) {
        navController.navigate(route) { launchSingleTop = true }
    }

    fun navigateAndPopUp(route: String, popUp: String) {
        navController.navigate(route) {
            launchSingleTop = true
            popUpTo(popUp) { inclusive = true }
        }
    }

    fun clearAndNavigate(route: String) {
        navController.navigate(route) {
            launchSingleTop = true
            popUpTo(0) { inclusive = true }
        }
    }
}