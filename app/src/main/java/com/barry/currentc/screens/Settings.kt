package com.barry.currentc.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.barry.currentc.R
import com.barry.currentc.common.composable.BasicToolbar
import com.barry.currentc.common.composable.DangerousCardEditor
import com.barry.currentc.common.composable.DialogCancelButton
import com.barry.currentc.common.composable.DialogConfirmButton
import com.barry.currentc.common.composable.RegularCardEditor
import com.barry.currentc.common.ext.card
import kotlinx.coroutines.runBlocking
import com.barry.currentc.R.drawable as AppIcon
import com.barry.currentc.R.string as AppText

@Composable
fun Settings(
    restartApp: () -> Unit,
    getIsAnonymous: () -> Boolean,
    getUserEmail: () -> String,
    onLoginClick: () -> Unit,
    onSignUpClick: () -> Unit,
    onSignOutClick: () -> Unit,
    onDeleteMyAccountClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isAnonymous by remember { mutableStateOf(getIsAnonymous()) }
    val userEmail by remember { mutableStateOf(getUserEmail()) }

    Column(Modifier.fillMaxSize()) {
        BasicToolbar(title = R.string.settings)

        if (isAnonymous) {
            Spacer(modifier = Modifier.height(16.dp))
            RegularCardEditor(
                title = AppText.sign_in,
                icon = AppIcon.ic_sign_in,
                content = "",
                modifier = Modifier.card()
            ) {
                onLoginClick()
            }

            RegularCardEditor(
                title = AppText.create_account,
                icon = AppIcon.ic_create_account,
                content = "",
                modifier = Modifier.card()
            ) {
                onSignUpClick()
            }
        } else {
            Text(
                text = "Hello $userEmail",
                style = TextStyle(
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 20.sp,

                    ),
                modifier = Modifier
                    .padding(16.dp)
            )
            SignOutCard(onSignOutClick, restartApp)
            DeleteMyAccountCard(onDeleteMyAccountClick, restartApp)
        }
    }
}

@Composable
private fun SignOutCard(signOut: () -> Unit, restartApp: () -> Unit) {
    var showWarningDialog by remember { mutableStateOf(false) }

    RegularCardEditor(AppText.sign_out, AppIcon.ic_exit, "", Modifier.card()) {
        showWarningDialog = true
    }

    if (showWarningDialog) {
        AlertDialog(
            title = { Text(stringResource(AppText.sign_out_title)) },
            text = { Text(stringResource(AppText.sign_out_description)) },
            dismissButton = { DialogCancelButton(AppText.cancel) { showWarningDialog = false } },
            confirmButton = {
                DialogConfirmButton(AppText.sign_out) {
                    runBlocking { signOut() }
                    showWarningDialog = false
                    restartApp()
                }
            },
            onDismissRequest = { showWarningDialog = false }
        )
    }
}

@Composable
private fun DeleteMyAccountCard(deleteMyAccount: () -> Unit, restartApp: () -> Unit) {
    var showWarningDialog by remember { mutableStateOf(false) }

    DangerousCardEditor(
        AppText.delete_my_account,
        AppIcon.ic_delete_my_account,
        "",
        Modifier.card()
    ) {
        showWarningDialog = true
    }

    if (showWarningDialog) {
        AlertDialog(
            title = { Text(stringResource(AppText.delete_account_title)) },
            text = { Text(stringResource(AppText.delete_account_description)) },
            dismissButton = { DialogCancelButton(AppText.cancel) { showWarningDialog = false } },
            confirmButton = {
                DialogConfirmButton(AppText.delete_my_account) {
                    deleteMyAccount()
                    restartApp()
                    showWarningDialog = false
                }
            },
            onDismissRequest = { showWarningDialog = false }
        )
    }
}