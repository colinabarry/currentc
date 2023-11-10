package com.barry.currentc

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.graphics.Color.Companion.Blue
import androidx.compose.ui.graphics.Color.Companion.Cyan
import androidx.compose.ui.graphics.Color.Companion.LightGray
import androidx.compose.ui.graphics.Color.Companion.Magenta
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.barry.currentc.utility.dpToPx
import com.barry.currentc.utility.pxToDp
import com.barry.currentc.utility.remap
import info.movito.themoviedbapi.model.MovieDb
import info.movito.themoviedbapi.model.people.Person

val gradientColors = listOf(Cyan, Blue, Magenta /*...*/)

@Composable
fun Title(
    text: String,
    modifier: Modifier = Modifier,
    alignRight: Boolean = false,
    hasShadow: Boolean = true,
) {
    Text(
        text = text,
        style = TextStyle(
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            textAlign = if (alignRight) TextAlign.Right else TextAlign.Left,
            shadow = Shadow(Black, blurRadius = if (hasShadow) 16f else 0f),
            color = White,
//            brush = Brush.linearGradient(colors = gradientColors)
        ),
        modifier = modifier
            .padding(8.dp)
    )
}


@Composable
fun SubTitle(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.secondary,
        fontWeight = FontWeight.Bold,
        modifier = modifier
    )
}


@Composable
fun PersonTile(
    person: Person,
//    onClickPerson: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val fullImagePath = "${stringResource(R.string.cast_image_base_path)}${person.profilePath}"

    var nameHeightPx by remember { mutableIntStateOf(0) }

    Box(
        modifier = modifier
            .size(90.dp, 150.dp)
            .clip(shape = RoundedCornerShape(8.dp))
//            .clickable { onClickPerson(person.id) }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            AsyncImage(
                model = fullImagePath,
                contentDescription = "",
                contentScale = ContentScale.FillHeight,
                modifier = Modifier
                    .fillMaxWidth()
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(nameHeightPx.pxToDp())
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            MaterialTheme.colorScheme.background,
                        )
                    )
                )
        )
        Text(
            text = person.name,
            style = TextStyle(
                fontWeight = FontWeight.Bold,
                shadow = Shadow(Black, blurRadius = 12f),
                color = White
            ),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .onSizeChanged { nameHeightPx = it.height }
        )
    }
}


@Composable
fun SearchResult(
    movie: MovieDb,
    onClickResult: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val backdropPath: String? = movie.backdropPath
    val posterPath: String? = movie.posterPath
    val imagePath: String? = backdropPath ?: posterPath
    val fullImagePath = "${stringResource(R.string.image_base_path)}$imagePath"

    val screenHeight = LocalConfiguration.current.screenHeightDp.dp.dpToPx()
    var imageOffset by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = modifier
            .height(128.dp)
            .padding(horizontal = 8.dp)
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(32.dp),
                clip = true,
            )
//            .background(MaterialTheme.colorScheme.onSecondary)
            .clickable { onClickResult(movie.id) }
            .onGloballyPositioned { coordinates ->
                imageOffset = (coordinates.positionInWindow().y / screenHeight)
                    .remap(fromMin = 0f, fromMax = 1f, toMin = -1f, toMax = 1f)
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            AsyncImage(
                model = fullImagePath,
                contentDescription = "",
                colorFilter = ColorFilter.tint(LightGray, blendMode = BlendMode.Modulate),
                alignment = BiasAlignment(
                    verticalBias = imageOffset,
                    horizontalBias = 0f
                ),
                contentScale = ContentScale.FillWidth,
                modifier = Modifier
                    .fillMaxWidth()
            )
        }
        val brush = Brush.linearGradient(listOf(White, White))
        Text(
            text = buildAnnotatedString {
                withStyle(
                    SpanStyle(
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        shadow = Shadow(Black, blurRadius = 16f),
                        color = White,
                    )
                ) {
                    append(movie.title)
                }
                if (movie.releaseDate.isNotEmpty())
                    withStyle(
                        SpanStyle(
                            brush = brush,
                            fontSize = 16.sp,
                            alpha = 0.5f
                        )
                    ) {
                        append(" (${movie.releaseDate.substring(0, 4)})")
                    }
            },
            modifier = Modifier
                .padding(start = 16.dp)
                .align(Alignment.CenterStart)
        )
    }
}

@Composable
fun PageControllerRow(
    currentPageIndex: Int,
    totalPages: Int,
    onClickPrev: () -> Unit,
    onClickNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
    ) {
        Button(onClick = onClickPrev) {
            Text(text = "Prev")
        }
        Text(text = "    $currentPageIndex / $totalPages    ")
        Button(onClick = onClickNext) {
            Text(text = "Next")
        }
    }
}