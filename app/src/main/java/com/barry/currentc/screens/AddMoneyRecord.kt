package com.barry.currentc.screens

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.barry.currentc.common.composable.Title
import com.barry.currentc.model.MoneyRecord

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMoneyRecord(
    onClickUpload: suspend (movieID: Int?, record: MoneyRecord) -> Boolean,
    onUploadSucceeded: () -> Unit,
    movieId: Int?,
    modifier: Modifier = Modifier
) {
    var year by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var timestamp by remember { mutableStateOf("") }
    var record by remember { mutableStateOf<MoneyRecord?>(null) }

    val intOnlyPattern = remember { Regex("^\\d{0,4}\$") }
    val floatOnlyPattern = remember { Regex("^\\d+(\\.\\d{0,2})?$") }
    val timestampPattern = remember {
        Regex("^(?:(?:([01]?\\d|2[0-3]):)?([0-5]?\\d)?:)?([0-5]?\\d)?$")
    }


    Column(
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize()
    ) {
        Title(text = "New money record")
        Spacer(modifier = Modifier.height(32.dp))

        TextField(
            value = year,
            onValueChange = {
                if (it.isEmpty() || it.matches(intOnlyPattern)) {
                    year = it
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            label = { Text(text = "Year in scene") },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))

        TextField(
            value = amount,
            onValueChange = {
                if (it.isEmpty() || it.matches(floatOnlyPattern)) {
                    amount = it
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            label = { Text(text = "Amount") },
            leadingIcon = { Text("$", fontSize = 20.sp) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))

        TextField(
            value = timestamp,
            onValueChange = {
                if (it.isEmpty() || it.matches(timestampPattern)) {
                    timestamp = it
                }
            },
            label = { Text(text = "Timestamp") },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "SS or MM:SS or H:MM:SS", color = MaterialTheme.colorScheme.secondary)
        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = {
            if (validateInputs(year, amount, timestamp))
                record = createRecord(year, amount, timestamp)
            Log.d("addmoneyrecord", record.toString())
        }) {
            Text(text = "Upload record", fontSize = 20.sp)
        }

        LaunchedEffect(record) {
            if (record == null) return@LaunchedEffect
            val succeeded = try {
                onClickUpload(movieId, createRecord(year, amount, timestamp))
                true
            } catch (e: Exception) {
                false
            }
            if (succeeded) onUploadSucceeded()
        }
    }
}

fun validateInputs(year: String, amount: String, timestamp: String): Boolean {
    if (year.length < 4) return false
    if (amount.isNotEmpty() && amount.last() == '.') return false
    if (timestamp.isNotEmpty() && timestamp.last() == ':') return false

    return listOf(year, amount, timestamp).all { it.isNotEmpty() }
}

fun createRecord(year: String, amount: String, timestamp: String): MoneyRecord {
    return MoneyRecord(year, amount, timestamp)
}
