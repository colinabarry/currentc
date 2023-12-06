package com.barry.currentc.screens

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.barry.currentc.R
import com.barry.currentc.common.composable.PersonTile
import com.barry.currentc.common.composable.SubTitle
import com.barry.currentc.common.composable.Title
import com.barry.currentc.common.ext.pxToDp
import com.barry.currentc.common.utility.minsToHours
import com.barry.currentc.model.MoneyRecord
import com.google.firebase.storage.FirebaseStorage
import info.movito.themoviedbapi.model.Credits
import info.movito.themoviedbapi.model.MovieDb
import info.movito.themoviedbapi.model.ReleaseInfo
import kotlinx.coroutines.runBlocking
import java.text.DecimalFormat
import kotlin.math.roundToInt


@Composable
fun MovieInfo(
    getMovie: suspend (id: Int?) -> MovieDb?,
    getUserImages: suspend (Int?) -> List<String>,
    getReleaseInfo: suspend (id: Int?) -> List<ReleaseInfo>?,
    getCredits: suspend (id: Int?) -> Credits?,
    onAddMoneyRecord: (Int?) -> Unit,
    getMoneyRecords: suspend (Int?) -> List<MoneyRecord>,
    getPriceChange: suspend (MoneyRecord) -> String,
    onPickImagePressed: () -> Unit,
    onBackButtonPressed: () -> Unit,
    movieId: Int?,
    modifier: Modifier = Modifier
) {
    // (769)  goodfellas: money examples and dark colors
    // (354912)  coco: light colors
    // (1150537)  justice league: long super long title

    var movieResult by remember { mutableStateOf<MovieDb?>(null) }
    var userImages by remember { mutableStateOf<List<String>>(listOf()) }
    var moneyRecords by remember { mutableStateOf<List<MoneyRecord>>(listOf()) }
    val uriHandler = LocalUriHandler.current

    LaunchedEffect(movieId) {
        movieResult = getMovie(movieId)
        moneyRecords = getMoneyRecords(movieId)
        userImages = getUserImages(movieId)
    }

    if (movieResult == null) {
        Text(text = "Error retrieving movie info")
        return
    }

    val movie = movieResult!!

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
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(
                state = ScrollState(0),
                enabled = true
            )
    ) {
        // start poster, image, and title section
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
                    .onSizeChanged { titleHeightPx = it.height }
                    .align(Alignment.BottomEnd)
            )
        }
        // end poster, image, and title section

        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .padding(horizontal = 16.dp)
        ) {
            // start tagline, score, runtime, release year, and overview section
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
                    text = "${if (movie.runtime != 0) minsToHours(movie.runtime) else "N/A"} | " +
                            if (movie.releaseDate.isNotEmpty()) movie.releaseDate.substring(
                                0,
                                4
                            ) else "N/A"
//                            + " | ${releaseInfo?.releaseDates!![0].certification ?: "nothin"}"
                )
            }
            Text(
                text = movie.overview,
                textAlign = TextAlign.Justify, // doesn't work?
            )
            // end tagline, score, runtime, release year, and overview section

            // start crew section
            var getCreditsResult by remember { mutableStateOf<Credits?>(null) }
            LaunchedEffect(movieId) {
                getCreditsResult = getCredits(movieId)
            }

            if (getCreditsResult == null) {
                Text(text = "Error retrieving movie from db")
                return
            }
            val credits = getCreditsResult!!
            val cast = credits.cast

            Text(
                text = "Cast",
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                )
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                for (person in cast.subList(
                    0,
                    15.coerceAtMost(cast.size)
                )) item { PersonTile(person = person) }
            }

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (imageUrl in userImages) {
                    item {
                        Column {
                            val gsRef = FirebaseStorage.getInstance().getReference(imageUrl)
//                            var imageBitmap: ImageBitmap? = null
                            var imageBitmap by remember {
                                mutableStateOf(ImageBitmap(1, 1))
                            }
                            runBlocking {
                                gsRef.getBytes(1024 * 1024 * 10)
                                    .addOnSuccessListener {
                                        imageBitmap = BitmapFactory
                                            .decodeByteArray(it, 0, it.size)
                                            .asImageBitmap()
                                    }
                            }
                            Image(
                                bitmap = imageBitmap,
                                contentDescription = "",
                                modifier = Modifier.height(150.dp)
                            )
                        }
                    }
                }
            }
            Button(onClick = { runBlocking { onPickImagePressed() } }) {
                Text(text = "Upload image")
            }
            
            Button(onClick = { onAddMoneyRecord(movieId) }) {
                Text(text = "Add money record")
            }
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Year:",
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    for (record in moneyRecords)
                        Text(text = record.year)

                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Amount:",
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    val decimalFormat = DecimalFormat("#.00")
                    for (record in moneyRecords) {
                        val amountFormatted = decimalFormat.format(record.amount.toFloat())
                        Text(text = "$$amountFormatted")
                    }

                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Value Today:",
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    for (record in moneyRecords) {
                        var changedPrice by remember { mutableStateOf<String?>(null) }

                        LaunchedEffect(record) {
                            changedPrice = getPriceChange(record)
                        }
                        Text(text = "$changedPrice")
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Timestamp:",
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    for (record in moneyRecords)
                        Text(text = record.timestamp)
                }

            }

            Text(
                text = "www.statbureau.org",
                style = TextStyle(
                    color = MaterialTheme.colorScheme.primary,
                    shadow = Shadow(color = MaterialTheme.colorScheme.primary, blurRadius = 16f)
                ),
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .clickable {
                        uriHandler.openUri("https://www.statbureau.org")
                    }
            )
        }
    }
}