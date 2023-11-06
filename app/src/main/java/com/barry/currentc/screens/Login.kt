package com.barry.currentc.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.barry.currentc.Title

@Composable
fun Login(
    onLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
    ) {
        Title(text = "This is the login screen")
        Button(
            onClick = onLogin,
        ) {
            Text(
                text = "Login",
            )
        }
    }
}