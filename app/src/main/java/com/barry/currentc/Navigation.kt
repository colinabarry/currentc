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
import com.barry.currentc.screens.Login
import com.barry.currentc.screens.MovieInfo
import com.barry.currentc.screens.Search
import com.barry.currentc.screens.Settings
import com.barry.currentc.screens.SignUp
import kotlinx.coroutines.runBlocking

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
//        startDestination = Screen.Settings.name,
//        startDestination = Screen.SignUp.name,
//        startDestination = Screen.Search.name,
//        startDestination = "${Screen.MovieInfo.name}/{movie}",
//        startDestination = Screen.AddMoneyRecord.name
//        startDestination = Screen.AddAnnotatedImage.name
    ) {
        val appState = AppState(navController)

        composable(route = Screen.Login.name) {
            Login(
                openAndPopUp = { route, popUp -> appState.navigateAndPopUp(route, popUp) },
                onClickSignIn = { openAndPopUp ->
                    viewModel.onSignInClick(
                        openAndPopUp,
//                        Screen.Settings.name,
//                        Screen.Login.name
                    )
                },
                onClickForgotPassword = viewModel::onForgotPasswordClick,
                getEmail = viewModel::getEmail,
                getPassword = viewModel::getPassword,
                setEmail = { viewModel.setEmail(it) },
                setPassword = { viewModel.setPassword(it) }
            )
        }

        composable(route = Screen.SignUp.name) {
            SignUp(
                openAndPopUp = { route, popUp -> appState.navigateAndPopUp(route, popUp) },
                onClickSignUp = viewModel::onSignUpClick,
                getEmail = viewModel::getEmail,
                getPassword = viewModel::getPassword,
                getRepeatPassword = viewModel::getRepeatPassword,
                setEmail = { viewModel.setEmail(it) },
                setPassword = { viewModel.setPassword(it) },
                setRepeatPassword = { viewModel.setRepeatPassword(it) }
            )
        }

        composable(route = Screen.Settings.name) {
            Settings(
                restartApp = { appState.clearAndNavigate(Screen.Home.name) },
                getIsAnonymous = viewModel::isAnonymousAccount,
                getUserEmail = viewModel::getCurrentUserEmail,
                onLoginClick = { appState.navigate(Screen.Login.name) },
                onSignUpClick = { appState.navigate(Screen.SignUp.name) },
                onSignOutClick = { runBlocking { viewModel.signOut() } },
                onDeleteMyAccountClick = { runBlocking { viewModel.deleteAccount() } })
        }

        composable(route = Screen.AddMoneyRecord.name) {
            AddMoneyRecord(
                onClickUpload = { movieId, record -> viewModel.addMoneyRecord(movieId, record) },
                onUploadSucceeded = appState::popUp,
                movieId = viewModel.currentMovieId,
            )
        }

        composable(route = Screen.AddAnnotatedImage.name) {
            AddAnnotatedImage(
                onClickPickImage = viewModel::pickImage,
                onClickUpload = { annotation ->
                    viewModel.handleImageUploadResult(annotation)
                },
                onBackPressed = { appState.popUp() }
            )
        }

        composable(route = Screen.Home.name) {
            Home(
                getPopularMovies = viewModel::getPopularMovies,
                getTopRatedMovies = viewModel::getTopRatedMovies,
                getNowPlayingMovies = viewModel::getNowPlayingMovies,
                onClickSearch = { appState.navigate(Screen.Search.name) },
                onClickSettings = { appState.navigate(Screen.Settings.name) },
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
                onPickImagePressed = { appState.navigate(Screen.AddAnnotatedImage.name) },
                getImagesAndAnnotations = { viewModel.getImagesAndAnnotations(it) },
                onBackButtonPressed = appState::popUp,
                getIsAnonymous = viewModel::isAnonymousAccount,
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