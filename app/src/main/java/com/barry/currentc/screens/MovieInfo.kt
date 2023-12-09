package com.barry.currentc.screens

import android.graphics.BitmapFactory
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.barry.currentc.R
import com.barry.currentc.common.composable.ActionToolbar
import com.barry.currentc.common.composable.PersonTile
import com.barry.currentc.common.composable.RatingBar
import com.barry.currentc.common.composable.SubTitle
import com.barry.currentc.common.composable.Title
import com.barry.currentc.common.ext.pxToDp
import com.barry.currentc.common.ext.remap
import com.barry.currentc.common.utility.minsToHours
import com.barry.currentc.model.MoneyRecord
import com.google.firebase.storage.FirebaseStorage
import info.movito.themoviedbapi.model.Credits
import info.movito.themoviedbapi.model.MovieDb
import info.movito.themoviedbapi.model.ReleaseInfo
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import java.text.DecimalFormat


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MovieInfo(
    getMovie: suspend (id: Int?) -> MovieDb?,
    getImagesAndAnnotations: suspend (Int?) -> Map<String, String>,
    getReleaseInfo: suspend (id: Int?) -> List<ReleaseInfo>?,
    getCredits: suspend (id: Int?) -> Credits?,
    onAddMoneyRecord: (Int?) -> Unit,
    getMoneyRecords: suspend (Int?) -> List<MoneyRecord>,
    getPriceChange: suspend (MoneyRecord) -> String,
    onPickImagePressed: () -> Unit,
    onBackButtonPressed: () -> Unit,
    getIsAnonymous: () -> Boolean,
    movieId: Int?,
    modifier: Modifier = Modifier
) {
    // (769)  goodfellas: money examples and dark colors
    // (354912)  coco: light colors
    // (1150537)  justice league: long super long title

    var movieResult by remember { mutableStateOf<MovieDb?>(null) }
    var imagesAndAnnotations by remember { mutableStateOf(emptyMap<String, String>()) }
    var moneyRecords by remember { mutableStateOf<List<MoneyRecord>>(listOf()) }
    val isAnonymous by remember { mutableStateOf(getIsAnonymous()) }


    val uriHandler = LocalUriHandler.current
    val moneyFormat = DecimalFormat("$#,###.##")
//    val percentFormat = DecimalFormat("#.##%")

    LaunchedEffect(movieId) {
        movieResult = getMovie(movieId)
        moneyRecords = getMoneyRecords(movieId)
        imagesAndAnnotations = getImagesAndAnnotations(movieId)
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
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        ActionToolbar(
            title = R.string.app_name,
            endActionIcon = R.drawable.ic_arrow_left,
            modifier = Modifier
        ) {
            onBackButtonPressed()
        }
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
                color = Color.White,
                modifier = Modifier
                    .onSizeChanged { titleHeightPx = it.height }
                    .align(Alignment.BottomEnd)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
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
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                val context = LocalContext.current
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f)
                        .combinedClickable(
                            onClick = {},
                            onLongClick = {
                                Toast
                                    .makeText(
                                        context,
                                        "User rating: ${movie.voteAverage}/10 from ${movie.voteCount} votes",
                                        Toast.LENGTH_LONG
                                    )
                                    .show()
                            }
                        )
                ) {
                    RatingBar(
                        rating = if (movie.voteCount != 0) (movie.voteAverage).remap(
                            0f,
                            10f,
                            0f,
                            5f
                        ) else 0f,
                        spaceBetween = 2.dp,
                        starSizeDp = 32.dp,
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    SubTitle(
                        text = "(${movie.voteCount})",
                    )
                }
//                SubTitle(
//                    text = "User Score: " +
//                            if (movie.voteCount != 0)
//                                "${(movie.voteAverage * 10).roundToInt()}% (${movie.voteCount})"
//                            else "N/A",
//                    modifier = Modifier.weight(1f),
//                )
                SubTitle(
                    text = "${if (movie.runtime != 0) minsToHours(movie.runtime) else "N/A"} | " +
                            if (movie.releaseDate.isNotEmpty()) movie.releaseDate.substring(
                                0,
                                4
                            ) else "N/A"
//                            + " | ${releaseInfo?.releaseDates!![0].certification ?: "nothin"}" // TODO: implement ratings
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

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.horizontalScroll(rememberScrollState())
            ) {
                for (person in cast.subList(
                    0,
                    15.coerceAtMost(cast.size)
                )) PersonTile(person = person)
            }

            if (!isAnonymous) Button(onClick = { runBlocking { onPickImagePressed() } }) {
                Text(text = "Upload image")
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
            ) {
                for (imageAndAnnotation in imagesAndAnnotations) {
                    val annotation by remember { mutableStateOf(imageAndAnnotation.value) }
                    var showFullAnnotation by remember { mutableStateOf(false) }

                    Column(
                        modifier = Modifier
                            .clickable { showFullAnnotation = !showFullAnnotation }
                    ) {
                        val gsRef = FirebaseStorage.getInstance()
                            .getReference("/movies/$movieId/${imageAndAnnotation.key}.jpg")
                        var imageExists by remember { mutableStateOf(false) }
                        LaunchedEffect(null) {
                            val items = gsRef.listAll().await().prefixes
                            imageExists = items.isNotEmpty()
                            Log.d(
                                "imageExists",
                                "\"/movies/$movieId/${imageAndAnnotation.key}.jpg\" exists: " + imageExists.toString()
                                        + "\nitems: $items"
                            )
                        }

//                        if (true) { // should actually check if image exists to allow plain annotations, but it's not working rn
                        var imageBitmap by remember {
                            mutableStateOf(ImageBitmap(1, 1))
                        }
                        var imageWidth by remember { mutableStateOf(0) }
                        LaunchedEffect(imageExists) {
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
                            contentScale = ContentScale.FillHeight,
                            modifier = Modifier
                                .height(200.dp)
                                .onSizeChanged { imageWidth = it.width }
                                .clip(RoundedCornerShape(16.dp)),
                        )
//                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = annotation,
                            maxLines = if (showFullAnnotation) Int.MAX_VALUE else 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .width(imageWidth.pxToDp())
                        )
                    }
                }
            }

            if (!isAnonymous) Button(onClick = { onAddMoneyRecord(movieId) }) {
                Text(text = "Add money record")
            }
            if (moneyRecords.isNotEmpty()) Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
            ) {
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "Year:",
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    for (record in moneyRecords)
                        Text(text = record.year)

                }
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "Amount:",
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    for (record in moneyRecords) {
                        val amountFormatted = moneyFormat.format(record.amount.toFloat())
                        Text(text = amountFormatted)
                    }
                }

                Column(
                    horizontalAlignment = Alignment.End
                ) {

                    Text(
                        text = "Value today:",
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    for (record in moneyRecords) {
                        var changedPrice by remember { mutableStateOf<String?>(null) }

                        LaunchedEffect(record) {
                            changedPrice = getPriceChange(record)
                        }
                        val amountFormatted = moneyFormat.format(changedPrice?.toFloat() ?: 0f)
                        Text(text = amountFormatted)
                    }
                }
                Column(
                    horizontalAlignment = Alignment.End
                ) {
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
//                Column( // TODO: implement this correctly
//                    horizontalAlignment = Alignment.End
//                ) {
//                    Text(
//                        text = "% change:",
//                        fontWeight = FontWeight.Black,
//                        fontSize = 16.sp,
//                        color = MaterialTheme.colorScheme.primary
//                    )
//                    for (record in moneyRecords) {
//                        val percent =
//                            percentFormat.format(changedPriceFloat / record.amount.toFloat())
//                        Text(text = percent)
//                    }
//                }
//            }
            if (moneyRecords.isNotEmpty()) Text(
                text = "www.statbureau.org",
                style = TextStyle(
                    color = MaterialTheme.colorScheme.primary,
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