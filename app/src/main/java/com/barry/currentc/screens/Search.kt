package com.barry.currentc.screens

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
            .fillMaxSize(),
    ) {
        TextField(
            value = searchTerm,
            onValueChange = {
                searchTerm = it
            },
            label = { Text(text = "Search") },
//            maxLines = 1,
            modifier = Modifier.fillMaxWidth(),
        )

//        LaunchedEffect(searchTerm, currentPageIndex) {
//            if (searchTerm.isNotEmpty()) {
//                launch { movieResultsPage = onSearch(searchTerm, currentPageIndex) }
//            }
//        }
//        LaunchedEffect(searchTerm, currentPageIndex) {
//            delay(150)
//            if (searchTerm.isNotEmpty()) {
//                launch { movieResultsPage = onSearch(searchTerm, currentPageIndex) }
//            }
//        }
        LaunchedEffect(searchTerm) {
            delay(150)
            if (searchTerm.isNotEmpty()) {
                launch { movieResultsPage = onSearch(searchTerm, currentPageIndex) }
            }
        }

        if (movieResultsPage == null || searchTerm.isEmpty()) return

        val scrollState = rememberLazyListState()
        val firstVisibleItemIndex by remember { derivedStateOf { scrollState.firstVisibleItemIndex } }
        val visibleItemsCount by remember { derivedStateOf { scrollState.layoutInfo.visibleItemsInfo.size } }
        val scrollPercent = firstVisibleItemIndex /
                (movieResultsPage!!.results.size - visibleItemsCount).toFloat()



        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            state = scrollState,
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
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
                    })

                LaunchedEffect(currentPageIndex) {
                    if (searchTerm.isNotEmpty()) {
                        launch {
                            movieResultsPage = onSearch(searchTerm, currentPageIndex)
                            Log.d("DEBUG", "trying to go to next page")
                        }
                    }
                }
            }


            for (result in movieResultsPage!!.results) item(key = result.id) {
                SearchResult(
                    movie = result,
                    onClickResult = onClickResult,
                )
            }

            item {
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
                    })


            }
        }


    }
}