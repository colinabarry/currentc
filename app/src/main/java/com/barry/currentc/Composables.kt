package com.barry.currentc

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.graphics.Color.Companion.Blue
import androidx.compose.ui.graphics.Color.Companion.Cyan
import androidx.compose.ui.graphics.Color.Companion.Magenta
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import info.movito.themoviedbapi.model.MovieDb
import kotlin.math.pow
import kotlin.math.roundToInt

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
            color = MaterialTheme.colorScheme.primary,

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
fun SearchResult(
    movie: MovieDb,
    onClickResult: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(shape = RoundedCornerShape(32.dp))
            .background(MaterialTheme.colorScheme.primary)
            .clickable { onClickResult(movie.id) }
    )
    {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxWidth(),
        ) {
            AsyncImage(
                model = "${stringResource(R.string.image_base_path)}${movie.posterPath}",
                contentDescription = "",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(128.dp)
                    .clip(shape = RoundedCornerShape(16.dp)),
            )
            Text(
                text = movie.title,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.background
            )
        }
    }
}


// These don't belong here, but here they are
@Composable
fun Dp.dpToPx() = with(LocalDensity.current) { this@dpToPx.toPx() }

@Composable
fun Int.pxToDp() = with(LocalDensity.current) { this@pxToDp.toDp() }

fun Float.roundToPlaces(places: Int): Float {
    val multiplier = 10.0.pow(places.toDouble())
    return (this * multiplier).roundToInt() / multiplier.toFloat()
}

fun minsToHours(minutes: Int): String {
    val hours: Int = (minutes / 60)
    val minutesRemaining: Int = minutes - (hours * 60)
    return "${hours}h ${minutesRemaining}m"
}