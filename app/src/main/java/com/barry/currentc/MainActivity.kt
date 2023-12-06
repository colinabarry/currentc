package com.barry.currentc

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.barry.currentc.ui.theme.CurrentCTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class MainActivity : ComponentActivity(), CoroutineScope {
    private val mainViewModel: MainViewModel by viewModels()
    lateinit var job: Job

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                Log.d("pickImageLauncher", "data.data: ${data?.data}")
                mainViewModel.handleImageUploadResult(data)
                Log.d("pickImageLauncher", "called hangleImageUploadResult")
            }
        }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        mainViewModel.setContext(this)
        mainViewModel.pickImageLauncher = pickImageLauncher
        job = Job()

        launch { mainViewModel.createAnonymousAccount() }

        super.onCreate(savedInstanceState)

        setContent {
            CurrentCTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Box {
                        MainView(mainViewModel)
                    }
                }
            }
        }
    }
}
