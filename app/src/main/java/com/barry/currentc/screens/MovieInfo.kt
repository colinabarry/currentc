package com.barry.currentc.screens

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.barry.currentc.R
import com.barry.currentc.SubTitle
import com.barry.currentc.Title
import com.barry.currentc.minsToHours
import com.barry.currentc.pxToDp
import info.movito.themoviedbapi.model.MovieDb
import kotlinx.coroutines.runBlocking
import kotlin.math.roundToInt

@Composable
fun MovieInfo(
    onLoad: suspend (id: Int?) -> MovieDb?,
    onBackButtonPressed: () -> Unit,
    movieId: Int?,
    modifier: Modifier = Modifier
) {
    var getMovieResult: MovieDb?
    runBlocking {
//        getMovieResult = onLoad(769) // goodfellas: money examples and dark colors
//        getMovieResult = onLoad(354912) // coco: light colors
//        getMovieResult = onLoad(1150537) // justice league: long super long title
        getMovieResult = onLoad(movieId)
    }

    if (getMovieResult == null) {
        Text(text = "Error retrieving movie from db")
        return
    }
    val movie = getMovieResult!!


    val backdropPath: String? = movie.backdropPath
    val posterPath: String? = movie.posterPath
    val fullBackdropPath =
        if (backdropPath != null)
            "${stringResource(R.string.image_base_path)}${movie.backdropPath}"
        else ""
    val fullPosterPath =
        if (posterPath != null)
            "${stringResource(R.string.image_base_path)}${movie.posterPath}"
        else ""

    var backdropHeightPx by remember { mutableIntStateOf(0) }
    var titleHeightPx by remember { mutableIntStateOf(0) }


    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(
                state = ScrollState(0),
                enabled = true
            )
    )
    {
        Box {
            if (fullBackdropPath.isNotEmpty()) AsyncImage(
                model = fullBackdropPath, contentDescription = "",
                modifier = Modifier.onSizeChanged { size -> backdropHeightPx = size.height },
                colorFilter = ColorFilter.tint(Color.LightGray, blendMode = BlendMode.Modulate),
            )
            AsyncImage(
                model = fullPosterPath, contentDescription = "",
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .height(if (fullBackdropPath.isNotEmpty()) backdropHeightPx.pxToDp() else 256.dp)
                    .padding(16.dp)
                    .shadow(elevation = 8.dp, shape = RoundedCornerShape(16.dp))
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(titleHeightPx.pxToDp())
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                MaterialTheme.colorScheme.background
                            )
                        )
                    )
            )
            Title(
                text = movie.title,
                alignRight = true,
                modifier = Modifier
                    .onSizeChanged { size -> titleHeightPx = size.height }
                    .align(Alignment.BottomEnd)
            )
        }
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .padding(horizontal = 16.dp)
        ) {
            if (movie.tagline != "") Text(
                text = "\"${movie.tagline}\"",
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.primary
            )
            Row {
                SubTitle(
                    text = "User Score: " +
                            if (movie.voteCount != 0)
                                "${(movie.voteAverage * 10).roundToInt()}% (${movie.voteCount})"
                            else "N/A",
                    modifier = Modifier.weight(1f),
                )
                SubTitle(
                    text = "${if (movie.runtime != 0) minsToHours(movie.runtime) else "N/A"} | ${
                        if (movie.releaseDate.isNotEmpty()) movie.releaseDate.substring(
                            0,
                            4
                        ) else "N/A"
                    }"
                )
            }
            Text(
                text = movie.overview,
                textAlign = TextAlign.Justify, // doesn't work?
            )
        }
    }
}