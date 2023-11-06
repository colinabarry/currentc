package com.barry.currentc

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.barry.currentc.screens.Home
import com.barry.currentc.screens.Login
import com.barry.currentc.screens.MovieInfo
import com.barry.currentc.screens.Search


enum class Screen {
    Login,
    Home,
    Search,
    MovieInfo,
}

@Composable
fun MainView(
    viewModel: MainViewModel,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Search.name,
    ) {
        composable(route = Screen.Login.name) {
            Login(
                onLogin = { navController.navigate(Screen.Home.name) },
            )
        }

        composable(route = Screen.Home.name) {
            Home()
        }

        composable(route = Screen.Search.name) {
            Search(
                onSearch = { searchTerm: String, page: Int ->
                    viewModel.searchMovies(searchTerm, page)
                },
                onClickResult = { movieId: Int ->
                    navController.navigate("${Screen.MovieInfo.name}/$movieId")
                }
            )
        }

        composable(
            route = "${Screen.MovieInfo.name}/{movie}",
            arguments = listOf(navArgument("movie") { type = NavType.IntType })
        ) { backStackEntry ->
            MovieInfo(
                onLoad = { viewModel.getMovie(it) },
                onBackButtonPressed = {
                    navController.popBackStack(
                        route = Screen.Search.name,
                        inclusive = false
                    )
                },
                movieId = backStackEntry.arguments?.getInt("movie")
            )
        }
    }
}