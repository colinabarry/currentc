package com.barry.currentc.utility

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import com.barry.currentc.R
import com.barry.currentc.data.TmdbConfig
import com.google.gson.Gson
import kotlin.math.pow
import kotlin.math.roundToInt

val gson = Gson()

@Composable
fun getConfiguration(): TmdbConfig {
    return gson.fromJson(stringResource(R.string.configuration), TmdbConfig::class.java)
}


fun Float.remap(fromMin: Float, fromMax: Float, toMin: Float, toMax: Float): Float {
    val fromAbs = this - fromMin
    val fromMaxAbs = fromMax - fromMin

    val normal = fromAbs / fromMaxAbs

    val toMaxAbs = toMax - toMin
    val toAbs = toMaxAbs * normal

    return toAbs + toMin
}

@Composable
fun Dp.dpToPx() = with(LocalDensity.current) { this@dpToPx.toPx() }

@Composable
fun Int.pxToDp() = with(LocalDensity.current) { this@pxToDp.toDp() }

fun Float.roundToPlaces(places: Int): Float {
    val multiplier = 10.0.pow(places.toDouble())
    return (this * multiplier).roundToInt() / multiplier.toFloat()
}

fun minsToHours(minutes: Int): String {
    val hours: Int = (minutes / 60)
    val minutesRemaining: Int = minutes - (hours * 60)
    return "${hours}h ${minutesRemaining}m"
}