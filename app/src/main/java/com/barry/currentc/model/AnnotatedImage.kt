package com.barry.currentc.model

import android.net.Uri

data class AnnotatedImage(
    val storageUri: Uri,
    val annotation: String = ""
)