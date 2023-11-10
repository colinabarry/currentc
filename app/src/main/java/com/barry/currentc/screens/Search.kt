package com.barry.currentc.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.barry.currentc.PageControllerRow
import com.barry.currentc.SearchResult
import info.movito.themoviedbapi.model.core.MovieResultsPage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Search(
    onSearch: suspend (searchTerm: String, page: Int) -> MovieResultsPage?,
    onClickResult: (Int) -> Unit,
    modifier: Modifier = Modifier
) {


    var searchTerm by rememberSaveable { mutableStateOf("") }
    var currentPageIndex by rememberSaveable { mutableIntStateOf(1) }

    var movieResultsPage: MovieResultsPage? by remember { mutableStateOf(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp),
    ) {
        LaunchedEffect(searchTerm, currentPageIndex) {
            delay(150)
            if (searchTerm.isNotEmpty()) {
                launch { movieResultsPage = onSearch(searchTerm, currentPageIndex) }
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
        ) {
            if (movieResultsPage != null && searchTerm.isNotEmpty()) {
                val scrollState = rememberLazyListState()

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    state = scrollState,
                    modifier = Modifier
                        .padding(top = 4.dp)
                ) {
                    for (result in movieResultsPage!!.results) item(key = result.id) {
                        SearchResult(
                            movie = result,
                            onClickResult = onClickResult,
                        )
                    }
                    item {
                        Box(Modifier.height(128.dp))
                    }
                }
            } else {
                Text(
                    text = "Search for movies",
                    Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp,
                        brush = Brush.linearGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.onBackground
                            )
                        ),
                        shadow = Shadow(color = Color(1f, 1f, 1f, 0.65f), blurRadius = 32f)
                    )
                )
            }

        }
        Column(
            verticalArrangement = Arrangement.spacedBy((-128).dp),
            modifier = Modifier
        ) {
            TextField(
                value = searchTerm,
                onValueChange = {
                    searchTerm = it
                },
                label = { Text(text = "Search") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            )

            if (movieResultsPage != null && searchTerm.isNotEmpty()) {
                PageControllerRow(
                    currentPageIndex = currentPageIndex,
                    totalPages = movieResultsPage!!.totalPages,
                    onClickPrev = {
                        currentPageIndex = (currentPageIndex - 1)
                            .coerceAtLeast(1)
                    },
                    onClickNext = {
                        currentPageIndex = (currentPageIndex + 1)
                            .coerceAtMost(movieResultsPage!!.totalPages)
                    },
                )
            }

        }


    }
}