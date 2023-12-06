package com.barry.currentc.common.utility

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.barry.currentc.R
import com.barry.currentc.data.TmdbConfig
import com.google.gson.Gson
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

val gson = Gson()

@Composable
fun getConfiguration(): TmdbConfig {
    return gson.fromJson(stringResource(R.string.configuration), TmdbConfig::class.java)
}

fun minsToHours(minutes: Int): String {
    val hours: Int = (minutes / 60)
    val minutesRemaining: Int = minutes - (hours * 60)
    return "${hours}h ${minutesRemaining}m"
}

fun saveBitmapToFile(
    bitmap: Bitmap,
    file: File,
    format: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG,
    quality: Int = 100
) {
    try {
        FileOutputStream(file).use { fos ->
            bitmap.compress(format, quality, fos)
        }
    } catch (e: IOException) {
        // Handle the exception
        e.printStackTrace()
    }
}

fun normalizeBitmap(bitmap: Bitmap, largestDimension: Int): Bitmap {
    val aspectRatio = bitmap.width.toFloat() / bitmap.height.toFloat()
    val isPortrait = aspectRatio < 1
    val targetWidth = (largestDimension * if (isPortrait) aspectRatio else 1f).toInt()
    val targetHeight = (largestDimension / if (!isPortrait) aspectRatio else 1f).toInt()

    return Bitmap.createScaledBitmap(
        bitmap,
        targetWidth,
        targetHeight,
        true
    )
}

fun normalizeBitmap(uri: Uri, largestDimension: Int, context: Context): Bitmap {
    return normalizeBitmap(createBitmapFromUri(uri, context), largestDimension)
}

fun createBitmapFromUri(uri: Uri, context: Context): Bitmap {
    val source = ImageDecoder.createSource(context.contentResolver, uri)
    return ImageDecoder.decodeBitmap(source)
}

fun createDirectory(path: String): Boolean {
    val folder = File(path)
    return if (!folder.exists()) {
        folder.mkdirs()
    } else {
        false
    }
}