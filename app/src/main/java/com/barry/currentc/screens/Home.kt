package com.barry.currentc.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.barry.currentc.common.composable.ActionToolbar
import com.barry.currentc.common.composable.MovieCarousel
import com.barry.currentc.common.ext.toolbarActions
import info.movito.themoviedbapi.model.core.MovieResultsPage
import kotlinx.coroutines.runBlocking
import com.barry.currentc.R.drawable as AppIcon
import com.barry.currentc.R.string as AppText

@Composable
fun Home(
    getPopularMovies: suspend () -> MovieResultsPage?,
    getTopRatedMovies: suspend () -> MovieResultsPage?,
    getNowPlayingMovies: suspend () -> MovieResultsPage?,
    onClickMovie: (Int) -> Unit,
    onClickSearch: () -> Unit,
    onClickSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val popularMovies = remember { runBlocking { getPopularMovies() } }
    val topRatedMovies = remember { runBlocking { getTopRatedMovies() } }
    val nowPlayingMovies = remember { runBlocking { getNowPlayingMovies() } }

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        ActionToolbar(
            title = AppText.app_name,
            endActionIcon = AppIcon.ic_settings,
            modifier = Modifier.toolbarActions()
        ) {
            onClickSettings()
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                if (popularMovies != null) {
                    MovieCarousel(
                        title = "Popular today",
                        movieResultsPage = popularMovies,
                        onClickMovie = onClickMovie
                    )
                }

                if (topRatedMovies != null) {
                    MovieCarousel(
                        title = "Top rated",
                        movieResultsPage = topRatedMovies,
                        onClickMovie = onClickMovie
                    )
                }

                if (nowPlayingMovies != null) {
                    MovieCarousel(
                        title = "Now playing",
                        movieResultsPage = nowPlayingMovies,
                        onClickMovie = onClickMovie
                    )
                }

                Spacer(Modifier.height(64.dp))
            }

            Button(
                onClick = { onClickSearch() },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .shadow(16.dp)
                    .padding(bottom = 8.dp)
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
}