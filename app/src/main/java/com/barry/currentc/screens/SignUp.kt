package com.barry.currentc.screens

import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.barry.currentc.R
import com.barry.currentc.common.composable.Title
import kotlinx.coroutines.runBlocking

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUp(
    openAndPopUp: (String, String) -> Unit,
    onClickSignUp: suspend ((String, String) -> Unit) -> Unit,
    getEmail: () -> String,
    getPassword: () -> String,
    getRepeatPassword: () -> String,
    setEmail: (String) -> Unit,
    setPassword: (String) -> Unit,
    setRepeatPassword: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var repeatPassword by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var showRepeatPassword by remember { mutableStateOf(false) }
    var passwordIcon =
        if (showPassword) R.drawable.ic_visibility_off else R.drawable.ic_visibility_on
    var repeatPasswordIcon =
        if (showRepeatPassword) R.drawable.ic_visibility_off else R.drawable.ic_visibility_on

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Title(text = "CurrentC", hasShadow = false)
        Spacer(modifier = Modifier.height(32.dp))
        TextField(
            value = email,
            label = { Text(text = "Email") },
            onValueChange = {
                email = it
                setEmail(email)
            },
            modifier = Modifier
                .fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = password,
            label = { Text(text = "Password") },
            onValueChange = {
                password = it
                setPassword(password)
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                Icon(
                    painter = painterResource(id = passwordIcon),
                    contentDescription = "",
                    Modifier.clickable { showPassword = !showPassword })
            },
            modifier = Modifier
                .fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = repeatPassword,
            label = { Text(text = "Repeat password") },
            onValueChange = {
                repeatPassword = it
                setRepeatPassword(repeatPassword)
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = if (showRepeatPassword) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                Icon(
                    painter = painterResource(id = repeatPasswordIcon),
                    contentDescription = "",
                    Modifier.clickable { showRepeatPassword = !showRepeatPassword })
            },
            modifier = Modifier
                .fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { runBlocking { onClickSignUp(openAndPopUp) } },
        ) {
            Text(
                text = "Login",
            )
        }
    }
}