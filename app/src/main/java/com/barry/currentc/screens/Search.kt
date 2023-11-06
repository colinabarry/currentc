package com.barry.currentc.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.unit.dp
import com.barry.currentc.SearchResult
import info.movito.themoviedbapi.model.core.MovieResultsPage
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Search(
    onSearch: suspend (searchTerm: String, page: Int) -> MovieResultsPage?,
    onClickResult: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchTerm by rememberSaveable { mutableStateOf("") }
    var currentPageIndex by rememberSaveable { mutableIntStateOf(0) }

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
            modifier = Modifier.fillMaxWidth(),
        )

        LaunchedEffect(searchTerm, currentPageIndex) {
            if (searchTerm.isNotEmpty()) {
                launch { movieResultsPage = onSearch(searchTerm, currentPageIndex) }
            }
        }

        if (movieResultsPage == null || searchTerm.isEmpty()) return@Column

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            content = {
                for (result in movieResultsPage!!.results) item {
                    SearchResult(
                        movie = result,
                        onClickResult = onClickResult
                    )
                }

                item {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Button(onClick = {
                            currentPageIndex = (currentPageIndex - 1).coerceAtLeast(0)
                        }) {
                            Text(text = "Prev")
                        }
                        Text(text = "$currentPageIndex/${movieResultsPage!!.totalPages}")
                        Button(onClick = {
                            currentPageIndex =
                                (currentPageIndex + 1).coerceAtMost(movieResultsPage!!.totalPages)
                        }) {
                            Text(text = "Next")
                        }
                    }
                }

            }
        )

    }
}