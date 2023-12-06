package com.barry.currentc.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.barry.currentc.common.composable.MovieCarousel
import com.barry.currentc.common.composable.Title
import info.movito.themoviedbapi.model.core.MovieResultsPage
import kotlinx.coroutines.runBlocking

@Composable
fun Home(
    getPopularMovies: suspend () -> MovieResultsPage?,
    getTopRatedMovies: suspend () -> MovieResultsPage?,
    getNowPlayingMovies: suspend () -> MovieResultsPage?,
    onClickMovie: (Int) -> Unit,
    onClickSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    val popularMovies = remember { runBlocking { getPopularMovies() } }
    val topRatedMovies = remember { runBlocking { getTopRatedMovies() } }
    val nowPlayingMovies = remember { runBlocking { getNowPlayingMovies() } }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
        ) {
            item { Title(text = "Home") }
            if (popularMovies != null) item {
                MovieCarousel(
                    title = "Popular today",
                    movieResultsPage = popularMovies,
                    onClickMovie = onClickMovie
                )
            }

            if (topRatedMovies != null) item {
                MovieCarousel(
                    title = "Top rated",
                    movieResultsPage = topRatedMovies,
                    onClickMovie = onClickMovie
                )
            }

            if (nowPlayingMovies != null) item {
                MovieCarousel(
                    title = "Now playing",
                    movieResultsPage = nowPlayingMovies,
                    onClickMovie = onClickMovie
                )
            }

            item {
                Spacer(Modifier.height(64.dp))
            }
        }

        Button(
            onClick = { onClickSearch() },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .shadow(16.dp)
        ) {
            Text(
                text = "Search",
                style = TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            )

        }
    }
}